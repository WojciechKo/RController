package info.korzeniowski.rcontroller;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.text)
    TextView text;

    private SensorManager mSensorManager;

    private static final float NS2S = 1.0f / 1000000000.0f;
    protected float[] vGyroscope = new float[3];
    private double timestamp;
    private long timeStampGyroscope = 0;
    private float[] deltaRotationVector = new float[4];
    private float[] sum = new float[3];
    private GyroscopeValues initialRotationVectors;

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

                mSensorManager.registerListener(initRotationVectors, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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

        text.setText("" + deltaAxisZ + " ; " + pwmFillness);
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