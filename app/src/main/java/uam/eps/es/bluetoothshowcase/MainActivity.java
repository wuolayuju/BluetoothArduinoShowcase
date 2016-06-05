package uam.eps.es.bluetoothshowcase;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);

        initializeLayoutVariables();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        BluetoothServerFragment btServerFragment = new BluetoothServerFragment();
        transaction.replace(R.id.content_fragment, btServerFragment);
        transaction.commit();
        Log.i(TAG, "commited BluetoothServerFragment");

        if (getActionBar() == null) {
            System.out.println("Action bar null");
        }
        else {
            System.out.println("Action bar PRESENT");
        }
    }

    private void initializeLayoutVariables() {

    }
}
