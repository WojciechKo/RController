package info.korzeniowski.rcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

    @Bind(R.id.controller_spinner)
    Spinner controllerSpinner;

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

        controllerSpinner.setAdapter(getControllerSpinnerAdapter());
        controllerSpinner.setOnItemSelectedListener(getControllerSpinnerListener());

        carCommunicator = CarCommunicatorFactory.getCommunicator();
        carController = CarControllerFactory.getController(getIntent().getExtras());

        getSupportFragmentManager()
                .beginTransaction()
                .add(carCommunicator, CarCommunicator.TAG)
                .add(R.id.controller, carController)
                .commit();
    }

    private ArrayAdapter<CarControllerFactory.Type> getControllerSpinnerAdapter() {
        ArrayAdapter<CarControllerFactory.Type> controllerSpinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                CarControllerFactory.Type.values());
        controllerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        return controllerSpinnerAdapter;
    }

    private AdapterView.OnItemSelectedListener getControllerSpinnerListener() {
        return new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CarControllerFactory.Type selectedType = (CarControllerFactory.Type) parent.getItemAtPosition(position);
                CarController controllerFragment =
                        CarControllerFactory.getController(getIntent().getExtras(), selectedType);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.controller, controllerFragment)
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
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
