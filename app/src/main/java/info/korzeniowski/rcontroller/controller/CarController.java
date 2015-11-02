package info.korzeniowski.rcontroller.controller;

import android.support.v4.app.Fragment;

import info.korzeniowski.rcontroller.ControlData;

public abstract class CarController extends Fragment {
    public abstract ControlData getControlData();

    public abstract void stop();
}
