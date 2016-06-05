package uam.eps.es.bluetoothshowcase;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import uam.eps.es.bluetoothshowcase.utils.AndroidUtils;

/**
 * Created by Ari on 04/06/2016.
 */
public class BluetoothServerFragment extends Fragment {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_SELECT_DEVICE = 2;

    private final String TAG = this.getClass().getSimpleName();

    private TextView mBtDeviceInfoTextView;
    private SeekBar mRedLEDSeekbar;
    private SeekBar mYellowLEDSeekbar;
    private SeekBar mGreenLEDSeekbar;
    private BluetoothService mBluetoothService;

    private BluetoothAdapter mBluetoothAdapter;
    private EventBus mThreadsEventBus;
    private Button mReconnectButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThreadsEventBus = new EventBus("thread_bus");
        mThreadsEventBus.register(this);

        mBluetoothService = new BluetoothService(mThreadsEventBus);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            AndroidUtils.makeAndShowLongToast(getActivity() , getString(R.string.bt_not_supported));
            Log.w(TAG, "Bluetooth not supported by device.");
            getActivity().finish();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            startBluetoothDevicesSetup();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bt_server, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mBtDeviceInfoTextView = (TextView) view.findViewById(R.id.bt_device_info);
        mReconnectButton = (Button) view.findViewById(R.id.attempt_connect_device_button);
        mRedLEDSeekbar = (SeekBar) view.findViewById(R.id.red_led_brightness_seekbar);
        mYellowLEDSeekbar = (SeekBar) view.findViewById(R.id.yellow_led_brightness_seekbar);
        mGreenLEDSeekbar = (SeekBar) view.findViewById(R.id.green_led_brightness_seekbar);
    }

    @Override
    public void onStart() {
        super.onStart();

        mReconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBluetoothDevicesSetup();
            }
        });

        SeekBar.OnSeekBarChangeListener ledBrightnessSeekbarListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (mBluetoothService.state() != BluetoothServiceEvent.STATE_CONNECTED)
                    return;

                int seekbarId = seekBar.getId();
                String command = "";
                switch (seekbarId) {
                    case R.id.red_led_brightness_seekbar:
                        command += "r";
                        break;
                    case R.id.yellow_led_brightness_seekbar:
                        command += "y";
                        break;
                    case R.id.green_led_brightness_seekbar:
                        command += "g";
                        break;
                }
                command += progress;
                Log.d(TAG, "Brigthness command: [" + command + "]");
                mBluetoothService.write(command.getBytes());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        mRedLEDSeekbar.setOnSeekBarChangeListener(ledBrightnessSeekbarListener);
        mYellowLEDSeekbar.setOnSeekBarChangeListener(ledBrightnessSeekbarListener);
        mGreenLEDSeekbar.setOnSeekBarChangeListener(ledBrightnessSeekbarListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    AndroidUtils.makeAndShowLongToast(getActivity(), getString(R.string.bt_enabled));
                    Log.i(TAG, "Bluetooth Adapter enabled by user.");
                    startBluetoothDevicesSetup();
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    AndroidUtils.makeAndShowLongToast(getActivity(), getString(R.string.bt_must_be_enabled));
                    Log.e(TAG, "Bluetooth Adapter failed to get enabled.");
                }
                break;
            case REQUEST_SELECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getStringExtra(DevicesListDialogActivity.EXTRA_DEVICE_ADDRESS);
                    connectToBtDevice(address);
                } else {
                    mBtDeviceInfoTextView.setText("You should connect to a device");
                }
                break;
            default:
                break;
        }
    }

    @Subscribe
    public void listenForThreadEvents(BluetoothServiceEvent event) {
        String message = event.message;
        if (message == BluetoothServiceEvent.STATE_CONNECTED) {
            setActivityTitleState(getString(R.string.connected_to_title) + event.extra);
        }
    }

    private void connectToBtDevice(String address) {
        Log.d(TAG, "connectToBtDevice()");
        BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(address);
        String deviceInfo = "Name: " + remoteDevice.getName() + "\n";
        deviceInfo += "Address: " + remoteDevice.getAddress() + "\n";
        deviceInfo += "Bluetooth class: " + remoteDevice.getBluetoothClass() + "\n";
        deviceInfo += "Type: " + remoteDevice.getType();
        mBtDeviceInfoTextView.setText(deviceInfo);

        setActivityTitleState(getString(R.string.connecting_to_title)+remoteDevice.getName());
        mBluetoothService.connectToDevice(address);
    }

    private void setActivityTitleState(String title) {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setSubtitle(title);

    }

    private void startBluetoothDevicesSetup() {
        Intent devicesListIntent = new Intent(getActivity(), DevicesListDialogActivity.class);
        startActivityForResult(devicesListIntent, REQUEST_SELECT_DEVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThreadsEventBus.unregister(this);
        mBluetoothService.closeBluetoothService();
    }
}
