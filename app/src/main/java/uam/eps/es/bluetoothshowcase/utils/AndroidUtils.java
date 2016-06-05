package uam.eps.es.bluetoothshowcase.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Ari on 03/06/2016.
 */
public class AndroidUtils {

    public static void makeAndShowLongToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
}
