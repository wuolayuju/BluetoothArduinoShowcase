package uam.eps.es.bluetoothshowcase;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.google.common.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import uam.eps.es.bluetoothshowcase.utils.Constants;

/**
 * Created by Ari on 05/06/2016.
 */
public class BluetoothService {

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String TAG = this.getClass().getSimpleName();

    private String mCurrentState = Constants.STATE_DISCONNECTED;

    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothServiceMessageHandler mBtMessageHandler;

    private ConnectionThread mConnectionThread;
    private CommunicationThread mCommunicationThread;

    BluetoothService(BluetoothServiceMessageHandler handler) {
        mBtMessageHandler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
            Message stateMessage = buildStateMessage(mmBTDevice.getName());

            mCurrentState = Constants.STATE_CONNECTING;
            stateMessage.getData().putString(Constants.MESSAGE_BT_STATE, Constants.STATE_CONNECTING);
            mBtMessageHandler.sendMessage(stateMessage);

            try {
                mmBTSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, "Unable to connect() to BT socket", e);
                try {
                    mCurrentState = Constants.STATE_DISCONNECTED;
                    mmBTSocket.close();
                } catch (IOException e1) {
                    Log.e(TAG, "Failed to close() BT socket during connection failure", e1);
                }
                return;
            }

            if (mmBTSocket != null) {
                Log.d(TAG, "Connection w/ " + mmBTSocket.getRemoteDevice().getName() + " established");

                stateMessage = buildStateMessage(mmBTDevice.getName());
                stateMessage.getData().putString(Constants.MESSAGE_BT_STATE, Constants.STATE_CONNECTED);
                mBtMessageHandler.sendMessage(stateMessage);

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

    private Message buildStateMessage(String deviceName) {
        Message msg = mBtMessageHandler.obtainMessage(Constants.MESSAGE_BT_STATE_CHANGED);
        Bundle msgData = new Bundle();
        msgData.putString(Constants.DEVICE_NAME, deviceName);
        msg.setData(msgData);
        return msg;
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
            mCurrentState = Constants.STATE_CONNECTED;
        }

        @Override
        public void run() {
            Log.d(TAG, "Started BTCommunicationThread w/" + mDeviceName);
            byte[] inputBuffer = new byte[1024];

            while(true) {
                try {
                    int bytesRead = mmInputStream.read(inputBuffer);
                    String inputString = new String(inputBuffer, 0, bytesRead);
                    Log.d(TAG, "Bytes read = [" + bytesRead + "], String = '"+inputString + "'");
                } catch (IOException e) {
                    Log.e(TAG, "Failed to read from socket w/" + mDeviceName, e);
                    Message stateMessage = buildStateMessage(null);
                    stateMessage.getData().putString(Constants.MESSAGE_BT_STATE, Constants.STATE_DISCONNECTED);
                    mBtMessageHandler.sendMessage(stateMessage);
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
