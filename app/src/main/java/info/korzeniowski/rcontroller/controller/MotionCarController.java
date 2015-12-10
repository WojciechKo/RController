package info.korzeniowski.rcontroller.controller;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.korzeniowski.rcontroller.ControlData;
import info.korzeniowski.rcontroller.R;
import info.korzeniowski.rcontroller.utils.Prefs;

public class MotionCarController extends CarController {
    @Bind(R.id.text)
    TextView text;

    @Bind(R.id.turn)
    SeekBar turnSlider;

    @Bind(R.id.speed)
    SeekBar speedSlider;

    @Bind(R.id.direction)
    Switch directionSwitch;

    private Prefs pref;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SteeringWheelListener steeringWheelListener;
    private ControlData.SteeringWheel steeringWheel;

    public static MotionCarController instance(Bundle arguments) {
        MotionCarController newInstance = new MotionCarController();
        newInstance.setArguments(arguments);
        return newInstance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manual_controller, container, false);
        ButterKnife.bind(this, view);
        pref = new Prefs(getActivity());
        speedSlider.setOnSeekBarChangeListener(new UpdateTextOnSeekBarChange());
        directionSwitch.setOnCheckedChangeListener(new UpdateTextOnSwitch());
        turnSlider.setVisibility(View.GONE);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        steeringWheelListener = new SteeringWheelListener();
        mSensorManager.registerListener(steeringWheelListener, mSensor, 100);

        return view;
    }

    @Override
    public void onDestroyView() {
        mSensorManager.unregisterListener(steeringWheelListener);
        super.onDestroyView();
    }

    public ControlData getControlData() {
        return new ControlData(getEngine(), steeringWheel, pref.getServoFix());
    }

    private ControlData.Engine getEngine() {
        ControlData.Direction direction = directionSwitch.isChecked()
                ? ControlData.Direction.FORWARD
                : ControlData.Direction.BACKWARD;

        return new ControlData.Engine(direction, speedSlider.getProgress());
    }

    private void onSteeringWheelMove(float axisY) {
        steeringWheel = getSteeringWheel(axisY);
        text.setText(getControlData().toString());
    }

    private ControlData.SteeringWheel getSteeringWheel(float axisY) {
        ControlData.Side side;
        if (axisY < 0) {
            side = ControlData.Side.LEFT;
        } else {
            side = ControlData.Side.RIGHT;
        }
        int angle = getAngle(axisY);
        return new ControlData.SteeringWheel(side, angle);
    }

    private int getAngle(float gravity) {
        gravity = Math.abs(gravity);
        float MIN = 1;
        float MAX = 7;
        if (gravity < MIN) {
            return 0;
        } else if (gravity > MAX) {
            return 100;
        } else {
            float a = 100 / (MAX - MIN);
            float b = -a * MIN;
            return (int) (a * gravity + b);
        }
    }

    public void stop() {
        directionSwitch.setChecked(true);
        speedSlider.setProgress(0);
        turnSlider.setProgress(turnSlider.getMax() / 2);
    }

    class UpdateTextOnSeekBarChange implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            text.setText(getControlData().toString());
        }
    }

    class UpdateTextOnSwitch implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            text.setText(getControlData().toString());
        }
    }

    private class SteeringWheelListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];
            onSteeringWheelMove(axisY);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
