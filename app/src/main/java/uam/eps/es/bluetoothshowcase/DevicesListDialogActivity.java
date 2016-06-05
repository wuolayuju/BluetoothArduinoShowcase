package uam.eps.es.bluetoothshowcase;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by Ari on 03/06/2016.
 */
public class DevicesListDialogActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ListView mPairedDevicesListView;
    private ListView mNewDevicesListView;
    private ArrayAdapter<Object> mNewDevicesArrayAdapter;
    private Button mScanForDevicesButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_dialog_devices_list);

        setResult(Activity.RESULT_CANCELED);

        initializeLayoutVariables();

        mScanForDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDeviceDiscovery();
                v.setEnabled(false);
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Query for known devices into an ArrayAdapter
        mPairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayAdapter<String> pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.bt_device_name);
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice btDevice :
                    mPairedDevices) {
                pairedDevicesArrayAdapter.add(btDevice.getName() + "\n" + btDevice.getAddress());
            }
        }
        mPairedDevicesListView.setAdapter(pairedDevicesArrayAdapter);
        mPairedDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Separated ArrayAdapter for discovered devices
        mNewDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.bt_device_name);
        mNewDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        mNewDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register both broadcast filters for device discovery
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mBroadCastReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mBroadCastReceiver, filter);
    }

    private void initializeLayoutVariables() {
        mPairedDevicesListView = (ListView) findViewById(R.id.paired_devices_listview);
        mNewDevicesListView = (ListView) findViewById(R.id.new_devices_listview);
        mScanForDevicesButton = (Button) findViewById(R.id.scan_devices_button);
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mBluetoothAdapter.cancelDiscovery();

            String deviceString = ((TextView)view).getText().toString();
            String deviceAddress = deviceString.split("\n")[1];

            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, deviceAddress);

            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private BroadcastReceiver mBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice btDeviceFound = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mNewDevicesArrayAdapter.add(btDeviceFound.getName() + "\n" + btDeviceFound.getAddress());
                    mNewDevicesListView.setVisibility(View.VISIBLE);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    setProgressBarIndeterminateVisibility(false);
                    setTitle(getString(R.string.select_device_label));
                    if (mNewDevicesArrayAdapter.isEmpty()) {
                        mNewDevicesArrayAdapter.add(getString(R.string.no_bt_devices_found));
                    }
                    mScanForDevicesButton.setEnabled(true);
            }
        }
    };

    private void startDeviceDiscovery() {
        setProgressBarIndeterminateVisibility(true);
        setTitle(getString(R.string.scanning_title));

        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();

        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadCastReceiver);
    }
}
