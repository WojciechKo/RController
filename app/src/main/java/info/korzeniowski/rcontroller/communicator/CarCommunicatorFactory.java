package info.korzeniowski.rcontroller.communicator;

public class CarCommunicatorFactory {
    public static CarCommunicator getCommunicator() {
        return BluetoothCarCommunicator.instance();
    }
}
