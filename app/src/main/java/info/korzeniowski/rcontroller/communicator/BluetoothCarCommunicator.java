package info.korzeniowski.rcontroller.communicator;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import info.korzeniowski.rcontroller.ControlData;
import info.korzeniowski.rcontroller.utils.Prefs;

public class BluetoothCarCommunicator extends CarCommunicator {

    private BluetoothAdapter bluetoothAdapter;

    private ConnectedThread connectedThread;

    private Prefs prefs;

    public static BluetoothCarCommunicator instance() {
        return new BluetoothCarCommunicator();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        prefs = new Prefs(context);
//
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetoothAdapter == null) {
//            throw new RuntimeException("Unable to find bluetooth adapter.");
//        }
//
//        // Enable bluetooth if not enabled
//        if (!bluetoothAdapter.isEnabled()) {
//            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBluetooth, 0);
//        }
    }

    public void connect() {
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//        String bluetoothDeviceAddress = prefs.getBluetoothDevice();
//
//        BluetoothDevice foundDevice = null;
//        for (BluetoothDevice device : pairedDevices) {
//            if (bluetoothDeviceAddress.equals(device.getAddress())) {
//                foundDevice = device;
//                break;
//            }
//        }
//        if (foundDevice == null) {
//            buildBluetoothDevicesDialog(pairedDevices).show();
//        } else {
//            new ConnectThread(foundDevice).start();
//        }
    }

    @Override
    public void disconnect() {
        if (connectedThread != null) {
            connectedThread.cancel();
        }
    }

    @Override
    public void onPause() {
        disconnect();
        super.onPause();
    }

    private AlertDialog buildBluetoothDevicesDialog(Set<BluetoothDevice> pairedDevices) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setTitle("Select device:");

        final ArrayAdapter<BluetoothDevice> bluetoothDevicesAdapter =
                new ArrayAdapter<BluetoothDevice>(
                        getActivity(),
                        android.R.layout.select_dialog_singlechoice,
                        new ArrayList<>(pairedDevices)) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = LayoutInflater.from(this.getContext()).inflate(android.R.layout.select_dialog_item, parent, false);
                        }
                        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
                        textView.setText(getItem(position).getName());
                        return convertView;
                    }
                };

        builderSingle.setAdapter(bluetoothDevicesAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BluetoothDevice bluetoothDevice = bluetoothDevicesAdapter.getItem(which);
                prefs.setBluetoothDevice(bluetoothDevice.getAddress());
                new ConnectThread(bluetoothDevice).start();
            }
        });
        return builderSingle.create();
    }

    public void write(ControlData data) {
//        connectedThread.write(getBluetoothMsg(data).getBytes());
    }

    private String getBluetoothMsg(ControlData data) {
        return "^" +
                data.getDirection().getValue() + " " + data.getSpeed() + "|" +
                data.getSide().getValue() + " " + data.getAngle() + " " + data.getServoFix() + "$";
    }

    class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        }
    }

    class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            if (onConnected != null) {
                onConnected.apply();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
//        while (true) {
//            try {
//                // Read from the InputStream
//                bytes = mmInStream.read(buffer);
//                // Send the obtained bytes to the UI activity
//                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                        .sendToTarget();
//            } catch (IOException e) {
//                break;
//            }
//        }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                throw new RuntimeException("Error while writing via bluetooth", e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
