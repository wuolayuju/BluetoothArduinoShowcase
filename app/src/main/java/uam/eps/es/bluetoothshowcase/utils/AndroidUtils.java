package uam.eps.es.bluetoothshowcase.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Ari on 03/06/2016.
 */
public class AndroidUtils {

    public static void makeAndShowLongToast(Context context, String text) {
        makeAndShowToast(context, text, Toast.LENGTH_LONG);
    }

    public static void makeAndShowShortToast(Context context, String text) {
        makeAndShowToast(context, text, Toast.LENGTH_SHORT);
    }

    private static void makeAndShowToast(Context context, String text, int duration) {
        Toast.makeText(context, text, duration).show();
    }
}
