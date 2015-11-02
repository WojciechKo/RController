package info.korzeniowski.rcontroller.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        return view;
    }

    public ControlData getControlData() {
        ControlData.Direction direction = directionSwitch.isChecked()
                ? ControlData.Direction.FORWARD
                : ControlData.Direction.BACKWARD;

        int speed = speedSlider.getProgress();

        ControlData.Side side;
        int angle;
        if (turnSlider.getProgress() < 50) {
            side = ControlData.Side.LEFT;
            angle = 50 - turnSlider.getProgress();
        } else {
            side = ControlData.Side.RIGHT;
            angle = turnSlider.getProgress() - 50;
        }

        ControlData controlData = new ControlData(direction, speed, side, angle * 2, pref.getServoFix());
        text.setText(controlData.toString());
        return controlData;
    }

    public void stop() {
        directionSwitch.setChecked(true);
        speedSlider.setProgress(0);
        turnSlider.setProgress(50);
    }
}
