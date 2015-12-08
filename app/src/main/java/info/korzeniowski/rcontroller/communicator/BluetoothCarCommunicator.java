package info.korzeniowski.rcontroller.communicator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import info.korzeniowski.rcontroller.ControlData;
import info.korzeniowski.rcontroller.utils.Prefs;

public class BluetoothCarCommunicator extends CarCommunicator {

    private ConnectedThread connectedThread;

    private Prefs prefs;

    private ArrayAdapter<BluetoothDevice> discoveredDevicesAdapter;

    private final BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                discoveredDevicesAdapter.add(device);
                if (prefs.getBluetoothDevice().equals(device.getAddress())) {
                    // STOP DISCOVERING
                    new ConnectThread(device).start();
                }
            }
        }
    };

    public static BluetoothCarCommunicator instance() {
        return new BluetoothCarCommunicator();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefs = new Prefs(getContext());
    }

    public void connect() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            throw new RuntimeException("Unable to find bluetooth adapter.");
        }

        // Enable bluetooth if not enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        discoveredDevicesAdapter = new ArrayAdapter<BluetoothDevice>(
                getActivity(),
                android.R.layout.select_dialog_singlechoice) {
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

        // Discovery devices
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(deviceFoundReceiver, filter); // Don't forget to unregister during onDestroy

        BluetoothDevice foundDevice = findBluetoothDevice(bluetoothAdapter.getBondedDevices(), prefs.getBluetoothDevice());
        if (foundDevice != null) {
            new ConnectThread(foundDevice).start();
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buildBluetoothDevicesDialog().show();
                }
            });
        }
    }

    private BluetoothDevice findBluetoothDevice(Set<BluetoothDevice> devices, String deviceAddress) {
        for (BluetoothDevice device : devices) {
            if (deviceAddress.equals(device.getAddress())) {
                return device;
            }
        }
        return null;
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

    @Override
    public void onDestroy() {
        if (discoveredDevicesAdapter != null) {
            getActivity().unregisterReceiver(deviceFoundReceiver);
        }
        super.onDestroy();
    }

    private AlertDialog buildBluetoothDevicesDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setTitle("Select device:");

        builderSingle.setAdapter(discoveredDevicesAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BluetoothDevice bluetoothDevice = discoveredDevicesAdapter.getItem(which);
                prefs.setBluetoothDevice(bluetoothDevice.getAddress());
                new ConnectThread(bluetoothDevice).start();
            }
        });
        return builderSingle.create();
    }

    public void write(ControlData data) {
        if (connectedThread != null) {
            connectedThread.write(getBluetoothMsg(data).getBytes());
        }
    }

    private String getBluetoothMsg(ControlData data) {
        return "^" +
                data.getDirection().getValue() + " " + data.getSpeed() + "|" +
                data.getSide().getValue() + " " + data.getAngle() + " " + data.getServoFix() + "$";
    }

    class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket socket = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
                socket = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                throw new RuntimeException("Unable to connect via Bluetooth", e);
            }

            mmSocket = socket;
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
                } catch (IOException e) {
                    throw new RuntimeException("Unable to close bluetooth socket", e);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buildBluetoothDevicesDialog().show();
                    }
                });
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
            InputStream tmpIn;
            OutputStream tmpOut;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException("Can't get socket streams", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            if (onConnected != null) {
                onConnected.apply();
            }
        }

        public void run() {
//            byte[] buffer = new byte[1024];  // buffer store for the stream
//            int bytes; // bytes returned from read()

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
                throw new RuntimeException("Can't close bluetooth socket", e);
            }
        }
    }
}
