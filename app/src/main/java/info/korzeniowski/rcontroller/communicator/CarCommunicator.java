package info.korzeniowski.rcontroller.communicator;

import android.support.v4.app.Fragment;

import info.korzeniowski.rcontroller.ControlData;

public abstract class CarCommunicator extends Fragment {
    public static String TAG = CarCommunicator.class.getSimpleName();

    protected OnConnected onConnected;

    public abstract void connect();

    public abstract void disconnect();

    public abstract void write(ControlData controlData);


    public void setOnConnected(OnConnected onConnected) {
        this.onConnected = onConnected;
    }

    public interface OnConnected {
        void apply();
    }
}
