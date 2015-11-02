package info.korzeniowski.rcontroller.controller;

import android.os.Bundle;

public class CarControllerFactory {
    public static CarController getController(Bundle extras) {
        return ManualCarController.instance(extras);
    }
}
