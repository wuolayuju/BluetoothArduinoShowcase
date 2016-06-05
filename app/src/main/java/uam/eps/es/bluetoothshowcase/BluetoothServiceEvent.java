package uam.eps.es.bluetoothshowcase;

/**
 * Created by Ari on 05/06/2016.
 */
public class BluetoothServiceEvent {

    public static final String STATE_CONNECTED = "connected";
    public static final String STATE_DISCONNECTED = "disconnected";
    public static final String STATE_CONNECTING = "connecting";

    public String message;
    public String extra;

    public BluetoothServiceEvent(String message, String extra) {
        this.message = message;
    }
}
