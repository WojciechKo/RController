package info.korzeniowski.rcontroller.controller;

import android.os.Bundle;
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

public class ManualCarController extends CarController {

    @Bind(R.id.text)
    TextView text;

    @Bind(R.id.turn)
    SeekBar turnSlider;

    @Bind(R.id.speed)
    SeekBar speedSlider;

    @Bind(R.id.direction)
    Switch directionSwitch;

    private Prefs pref;

    public static ManualCarController instance(Bundle arguments) {
        ManualCarController newInstance = new ManualCarController();
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
        turnSlider.setOnSeekBarChangeListener(new UpdateTextOnSeekBarChange());
        directionSwitch.setOnCheckedChangeListener(new UpdateTextOnSwitch());
        return view;
    }

    public ControlData getControlData() {
        return new ControlData(getEngine(), getSteeringWheel(), pref.getServoFix());
    }

    private ControlData.Engine getEngine() {
        ControlData.Direction direction = directionSwitch.isChecked()
                ? ControlData.Direction.FORWARD
                : ControlData.Direction.BACKWARD;

        return new ControlData.Engine(direction, speedSlider.getProgress());
    }

    private ControlData.SteeringWheel getSteeringWheel() {
        if (turnSlider.getProgress() < turnSlider.getMax() / 2) {
            return new ControlData.SteeringWheel(
                    ControlData.Side.LEFT,
                    turnSlider.getMax() / 2 - turnSlider.getProgress());
        } else {
            return new ControlData.SteeringWheel(
                    ControlData.Side.RIGHT,
                    turnSlider.getProgress() - turnSlider.getMax() / 2);
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
}
