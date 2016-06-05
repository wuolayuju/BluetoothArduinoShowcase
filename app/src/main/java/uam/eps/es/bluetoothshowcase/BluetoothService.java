package uam.eps.es.bluetoothshowcase;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.google.common.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Ari on 05/06/2016.
 */
public class BluetoothService {

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String TAG = this.getClass().getSimpleName();

    private String mCurrentState = BluetoothServiceEvent.STATE_DISCONNECTED;

    private final BluetoothAdapter mBluetoothAdapter;
    private final EventBus mEventbus;
    private ConnectionThread mConnectionThread;
    private CommunicationThread mCommunicationThread;

    BluetoothService(EventBus eventBus) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mEventbus = eventBus;
    }

    public String state() {
        return mCurrentState;
    }

    public void connectToDevice(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        mConnectionThread = new ConnectionThread(device);
        mConnectionThread.start();
    }

    public void write(byte[] data) {
        mCommunicationThread.write(data);
    }

    private class ConnectionThread extends Thread{

        private final BluetoothSocket mmBTSocket;
        private final BluetoothDevice mmBTDevice;

        ConnectionThread(BluetoothDevice remoteDevice) {
            BluetoothSocket tmpSocket = null;
            mmBTDevice = remoteDevice;
            try {
                tmpSocket = mmBTDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Failed to create() communication socket", e);
            }
            mmBTSocket = tmpSocket;
        }

        public void run() {
            Log.d(TAG, "Started ConnectionThread w/" + mmBTSocket.getRemoteDevice().getName());
            mCurrentState = BluetoothServiceEvent.STATE_CONNECTING;
            try {
                mmBTSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, "Unable to connect() to BT socket", e);
                try {
                    mCurrentState = BluetoothServiceEvent.STATE_DISCONNECTED;
                    mmBTSocket.close();
                } catch (IOException e1) {
                    Log.e(TAG, "Failed to close() BT socket during connection failure", e1);
                }
                return;
            }

            if (mmBTSocket != null) {
                Log.d(TAG, "Connection w/ " + mmBTSocket.getRemoteDevice().getName() + " established");
                BluetoothServiceEvent msgToUIThread =
                        new BluetoothServiceEvent(BluetoothServiceEvent.STATE_CONNECTED, mmBTDevice.getName());
                mEventbus.post(msgToUIThread);
                beginBTCommunication(mmBTSocket);
            }
        }

        public void cancel() {
            try {
                mmBTSocket.close();
                Log.d(TAG, "BT connection socket closed()");
            } catch (IOException e) {
                Log.e(TAG, "BT server socket failed to close()", e);
            }
        }
    }

    private void beginBTCommunication(BluetoothSocket mmBTSocket) {
        mCommunicationThread = new CommunicationThread(mmBTSocket);
        mCommunicationThread.start();
    }

    private class CommunicationThread extends Thread {

        private String mDeviceName;

        private final BluetoothSocket mmBtSocket;
        private OutputStream mmOutputStream;
        private InputStream mmInputStream;

        CommunicationThread(BluetoothSocket socket) {
            mmBtSocket = socket;
            try {
                mmInputStream = mmBtSocket.getInputStream();
                mmOutputStream = mmBtSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Unable to get socket streams", e);
            }
            mDeviceName = mmBtSocket.getRemoteDevice().getName();
            mCurrentState = BluetoothServiceEvent.STATE_CONNECTED;
        }

        @Override
        public void run() {
            Log.d(TAG, "Started BTCommunicationThread w/" + mDeviceName);
            byte[] inputBuffer = new byte[1024];

            while(true) {
                try {
                    mmInputStream.read(inputBuffer);
                    String inputString = new String(inputBuffer);
                    System.out.println(inputString);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to read from socket w/" + mDeviceName, e);
                    break;
                }
            }
        }

        public void write(byte[] data) {
            try {
                mmOutputStream.write(data);
            } catch (IOException e) {
                Log.e(TAG, "Failed to write() data to " + mDeviceName);
            }
        }

        public void cancel() {
            try {
                mmBtSocket.close();
                Log.d(TAG, "BT communication socket closed()");
            } catch (IOException e) {
                Log.e(TAG, "Failed to close() BT socket w/ " + mDeviceName, e);
            }
        }
    }

    public void closeBluetoothService() {
        mCommunicationThread.cancel();
        mConnectionThread.cancel();
    }
}
