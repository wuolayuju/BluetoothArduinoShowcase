package uam.eps.es.bluetoothshowcase;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import uam.eps.es.bluetoothshowcase.utils.AndroidUtils;
import uam.eps.es.bluetoothshowcase.utils.Constants;

/**
 * Created by Ari on 06/06/2016.
 */
public class BluetoothServiceMessageHandler extends Handler {

    private final Activity mActivity;

    public BluetoothServiceMessageHandler(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void handleMessage(Message msg) {
        int msgType = msg.what;
        Bundle msgData = msg.getData();
        switch (msgType) {
            case Constants.MESSAGE_BT_STATE_CHANGED:
                String btState = msgData.getString(Constants.MESSAGE_BT_STATE);
                String deviceName = msgData.getString(Constants.DEVICE_NAME);
                switch (btState) {
                    case Constants.STATE_CONNECTED:
                        AndroidUtils.makeAndShowShortToast(mActivity, "Connected to " + deviceName);
                        break;
                    case Constants.STATE_CONNECTING:
                        AndroidUtils.makeAndShowShortToast(mActivity, "Connecting to " + deviceName + "...");
                        break;
                    case Constants.STATE_DISCONNECTED:
                        AndroidUtils.makeAndShowLongToast(mActivity, "Connection lost");
                        break;
                }
                break;
        }
    }
}
