package info.korzeniowski.rcontroller;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.korzeniowski.rcontroller.communicator.BluetoothCommunicator;
import info.korzeniowski.rcontroller.controller.ManualController;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.fab)
    FloatingActionButton fab;

    private BluetoothCommunicator bluetoothCommunicator;
    private ManualController manualController;
    private Runnable sendData;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        bluetoothCommunicator = BluetoothCommunicator.instance();

        manualController = new ManualController();
        manualController.setArguments(getIntent().getExtras());

        getSupportFragmentManager()
                .beginTransaction()
                .add(bluetoothCommunicator, BluetoothCommunicator.TAG)
                .add(R.id.controller, manualController)
                .commit();

        handler = new Handler();
        sendData = new Runnable() {
            public void run() {
                bluetoothCommunicator.send(manualController.getControllData());
                handler.postDelayed(sendData, 100); // 1 second
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(sendData);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(sendData);
        super.onPause();
    }
}
