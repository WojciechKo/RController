package info.korzeniowski.rcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.korzeniowski.rcontroller.communicator.CarCommunicator;
import info.korzeniowski.rcontroller.communicator.CarCommunicatorFactory;
import info.korzeniowski.rcontroller.controller.CarController;
import info.korzeniowski.rcontroller.controller.CarControllerFactory;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private CarCommunicator carCommunicator;
    private CarController carController;

    private Handler handler = new Handler();
    private Runnable sendData = new Runnable() {
        public void run() {
            carCommunicator.write(carController.getControlData());
            handler.postDelayed(sendData, 100); // 0.1 second
        }
    };

    @OnClick(R.id.fab)
    public void fabClicked() {
        carCommunicator.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        carCommunicator = CarCommunicatorFactory.getCommunicator();
        carController = CarControllerFactory.getController(getIntent().getExtras());

        getSupportFragmentManager()
                .beginTransaction()
                .add(carCommunicator, CarCommunicator.TAG)
                .add(R.id.controller, carController)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carCommunicator.setOnConnected(new CarCommunicator.OnConnected() {
            @Override
            public void apply() {
                handler.post(sendData);
            }
        });
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(sendData);
        carCommunicator.disconnect();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                carController.stop();
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
