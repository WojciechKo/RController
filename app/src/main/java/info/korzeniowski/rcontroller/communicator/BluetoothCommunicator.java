package info.korzeniowski.rcontroller.communicator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import info.korzeniowski.rcontroller.ControlData;

public class BluetoothCommunicator extends Fragment {
    public static String TAG = BluetoothCommunicator.class.getSimpleName();

    private BluetoothDevice bluetoothDevice;
    private OutputStream bluetoothOutputStream;
    private InputStream bluetoothInputStream;

    public static BluetoothCommunicator instance() {
        BluetoothCommunicator newInstance = new BluetoothCommunicator();
        return newInstance;
    }

    @Override
    public void onResume() {
        super.onResume();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            throw new RuntimeException("Unable to find bluetooth adapter.");
        }

        // Enable bluetooth if not enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        // Find device
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-05")) {
                    bluetoothDevice = device;
                    break;
                }
            }
        }

        if (bluetoothDevice == null) {
            throw new RuntimeException("Unable to find bluetooth device.");
        }

        // Connect to device
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
            BluetoothSocket bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            bluetoothOutputStream = bluetoothSocket.getOutputStream();
            bluetoothInputStream = bluetoothSocket.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException("Error while connecting via bluetooth", e);
        }
    }

    public void send(ControlData data) {
        try {
            String msg = getBluetoothMsg(data);
            bluetoothOutputStream.write(msg.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error while writing via bluetooth", e);
        }
    }

    private String getBluetoothMsg(ControlData data) {
        return "^" +
                data.getDirection().getValue() + " " + data.getSpeed() + "|" +
                data.getSide().getValue() + " " + data.getAngle() + "$";
    }
}
