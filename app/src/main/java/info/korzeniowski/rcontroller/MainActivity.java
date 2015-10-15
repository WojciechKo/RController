package info.korzeniowski.rcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.fab1)
    FloatingActionButton fab1;
    @Bind(R.id.text)
    TextView text;
    @Bind(R.id.slider)
    SeekBar slider;

    private SensorManager mSensorManager;

    private GyroscopeValues initialRotationVectors;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mmDevice;
    private BluetoothSocket mmSocket;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SensorEventListener initRotationVectors = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        initialRotationVectors = new GyroscopeValues(event.values);
                        mSensorManager.unregisterListener(this);
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                mSensorManager.registerListener(initRotationVectors, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-05")) {
                    mmDevice = device;
                    break;
                }
            }
        }

        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    String msg = getBluetoothMsg(progress);
                    text.setText(msg);
                    mmOutputStream.write(msg.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @NonNull
    private String getBluetoothMsg(int progress) {
        StringBuilder sb = new StringBuilder("^");
        if (progress < 50) {
            sb.append(2).append(" ").append((50 - progress) * 2);
        } else if (progress <= 100) {
            sb.append(1).append(" ").append((progress - 50) * 2);
        }

        sb.append("|").append(50).append("$");
        return sb.toString();
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        GyroscopeValues values = new GyroscopeValues(event.values);


        if (initialRotationVectors == null) {
            initialRotationVectors = values;
        }

        float deltaAxisZ = values.axisZ - initialRotationVectors.axisZ;
        int pwmFillness = convertToPwm(deltaAxisZ);
    }

    private int convertToPwm(float delta) {
        double rage = 0.9;
        double a = 900;

        if (delta >= -1 && delta < -rage) {
            return (int) (delta * a + 990);
        } else if (delta > -rage && delta < 0) {
            return 180;
        } else if (delta > 0 && delta < rage) {
            return 0;
        } else if (delta >= rage && delta < 1) {
            return (int) (delta * a - 810);
        }
        return 90;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class GyroscopeValues {
        private float axisX;
        private float axisY;
        private float axisZ;

        public GyroscopeValues(float[] values) {
            axisX = values[0];
            axisY = values[1];
            axisZ = values[2];
        }

        public float getAxisX() {
            return axisX;
        }

        public float getAxisY() {
            return axisY;
        }

        public float getAxisZ() {
            return axisZ;
        }
    }
}