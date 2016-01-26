package info.korzeniowski.rcontroller.controller;

import android.os.Bundle;

public class CarControllerFactory {
    public enum Type {MOTION, MANUAL}

    public static CarController getController(Bundle extras) {
        return MotionCarController.instance(extras);
    }

    public static CarController getController(Bundle extras, Type type) {
        switch (type) {
            case MOTION:
                return MotionCarController.instance(extras);
            case MANUAL:
                return ManualCarController.instance(extras);
            default:
                return null;
        }
    }
}
