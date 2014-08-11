package endee.fried.treasure;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * 
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connectionSuccessful.
 *
 * This class has a private constructor and should be used as a singleton
 * across the application. Use BluetoothManager.getInstance().
 * Handlers should also be registered and unregistered in each activity
 * to subscribe to notifications for UI events.
 * 
 */
public class BluetoothManager {

    public enum ConnectionState {
        Dropped,
        Failed,
        Connected
    }



    // CONSTANTS
    private static final String TAG = "BluetoothManager";
    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothConnection";
    public static final String GAME_INVITATION = "__invitation_to_treasure__";
    public static final String IS_HOST = "IAmTheHost";
    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 0x1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 0x2; // now initiating an outgoing connection

    // Singleton instance of Self.
    private static BluetoothManager _self;

    // MEMBER FIELDS

    private final BluetoothAdapter _bluetoothAdapter;
    private List<Handler> _handlers = new ArrayList<Handler>();
    private AcceptThread _acceptThread;
    private ConnectingThread _connectingThread;
    private HashMap<String, ConnectedThread> _connectedThreads = new HashMap<String, ConnectedThread>();

    private int _state;


    /**
     * Constructor. Prepares a new BluetoothManager session.
     */
    private BluetoothManager() {
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        _state = STATE_NONE;
    }

    /**
     * Get a singleton instance of the BluetoothManager.
     * Create one if none exists.
     * @return
     */
    public static BluetoothManager getInstance() {
        if (_self == null) {
            _self = new BluetoothManager();
        }
        return _self;
    }


    /**
     * Register a handler to respond to Bluetooth Notifications.
     * Do this in every Activity class that uses bluetooth in the onResume().
     * @param handler
     */
    public synchronized void registerHandler(Handler handler) {
        _handlers.add(handler);
    }

    /**
     * Unregister a handler.
     * Do this in every Activity class that uses bluetooth in the onPause().
     * @param handler
     */
    public synchronized void unregisterHandler(Handler handler) {
        _handlers.remove(handler);
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void sendConnectionState(ConnectionState state, String deviceAddress, String deviceName) {
        // Give the new state to the Handler so the UI Activity can update
        for (Handler h : _handlers) {
            Message msg = h.obtainMessage(BluetoothLounge.MESSAGE_STATE_CHANGE);
            if (deviceAddress != "") {
                Bundle bundle = new Bundle();
                bundle.putSerializable(BluetoothLounge.CONNECTION_STATE, state);
                bundle.putString(BluetoothLounge.DEVICE_ADDRESS, deviceAddress);
                bundle.putString(BluetoothLounge.DEVICE_NAME, deviceName);
                msg.setData(bundle);
            }
            msg.sendToTarget();
        }
    }

    /**
     * Start the bluetooth manager. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void startListening() {
        Log.d(TAG, "start");

        if(_acceptThread != null) {
            _acceptThread.cancel();
        }

        _acceptThread = new AcceptThread();
        _acceptThread.start();

        _state = _state | STATE_LISTEN;
    }

    public synchronized void stopListening() {

        if(_acceptThread != null) {
            _acceptThread.cancel();
            _acceptThread = null;
        }

        _state = _state & ~STATE_LISTEN;
    }

    public synchronized void dropAllConnections() {
        if (_connectingThread != null) {
            _connectingThread.cancel(); _connectingThread = null;
        }

        for (ConnectedThread ct : _connectedThreads.values()) {
            ct.cancel();
        }

        _connectedThreads.clear();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.e(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if ((_state & STATE_CONNECTING) > 0) {
            if (_connectingThread != null) {
                _connectingThread.cancel();
                _connectingThread = null;
            }
        }

        // Start the thread to connect with the given device
        _connectingThread = new ConnectingThread(device);
        _connectingThread.start();
        _state = _state | STATE_CONNECTING;
    }

    /**
     * Stop all threads
     */
    public synchronized void stopEverything() {
        Log.d(TAG, "stop");

        if (_acceptThread != null) {
            _acceptThread.cancel();
            _acceptThread = null;
        }

        if (_connectingThread != null) {
            _connectingThread.cancel(); _connectingThread = null;
        }

        for (ConnectedThread ct : _connectedThreads.values()) {
            ct.cancel();
        }

        _connectedThreads.clear();

        _state = STATE_NONE;
    }

    public boolean connectedToDevice(String deviceAddress) {
        return _connectedThreads.containsKey(deviceAddress);
    }

    /**
     * Write a message to everyone connectionSuccessful
     * @param out message to send
     * @param except Device address for a device we don't want to send to.
     */
    public void writeToEveryone(byte[] out, String except) {

        for (String deviceAddress : _connectedThreads.keySet()) {

            if (deviceAddress.equals(except)) {
                continue;
            }

            write(out, deviceAddress);
        }

    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out, String deviceAddress) {

        if(out.length == 0) {
            throw new RuntimeException("Error sending empty message");
        }

        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (!_connectedThreads.containsKey(deviceAddress)) return;
            r = _connectedThreads.get(deviceAddress);
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected Successfully
     */
    private synchronized void connectionSuccessful(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (_connectingThread != null) {
            _connectingThread.cancel();
            _connectingThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        ConnectedThread connectedThread = new ConnectedThread(socket, device);
        _connectedThreads.put(device.getAddress(), connectedThread);
        connectedThread.start();

        sendConnectionState(ConnectionState.Connected, device.getAddress(), device.getName());
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private synchronized void connectionFailed(BluetoothDevice device) {
        sendConnectionState(ConnectionState.Failed, device.getAddress(), device.getName());
        _state = _state & ~STATE_CONNECTING;
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private synchronized void connectionLost(BluetoothDevice device) {
        sendConnectionState(ConnectionState.Dropped, device.getAddress(), device.getName());
        _connectedThreads.remove(device.getAddress());
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket __serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = _bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            __serverSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "BEGIN acceptThread" + this);

            setName("AcceptThread");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connectionSuccessful
            while (true) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = __serverSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothManager.this) {
                        if((_state | STATE_LISTEN) == 0 || _connectedThreads.containsKey(socket.getRemoteDevice().getAddress())) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                        } else {
                            connectionSuccessful(socket, socket.getRemoteDevice());
                        }
                    }
                }
            }
            Log.i(TAG, "END _acceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel " + this);
            try {
                __serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectingThread extends Thread {
        private final BluetoothSocket __socket;
        private final BluetoothDevice __device;

        public ConnectingThread(BluetoothDevice device) {
            __device = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            __socket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN _connectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            _bluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                __socket.connect();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                connectionFailed(__device);
                // Close the socket
                try {
                    __socket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }

                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothManager.this) {
                _connectingThread = null;
            }

            // Start the connectionSuccessful thread
            connectionSuccessful(__socket, __device);
        }

        public void cancel() {
            try {
                __socket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket __socket;
        private final InputStream __inStream;
        private final OutputStream __outStream;
        private final BluetoothDevice __bluetoothDevice;

        public ConnectedThread(BluetoothSocket socket, BluetoothDevice bluetoothDevice) {
            Log.d(TAG, "create ConnectedThread");

            __socket = socket;
            __bluetoothDevice = bluetoothDevice;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            __inStream = tmpIn;
            __outStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connectionSuccessful
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = __inStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    for (Handler h : _handlers) {
                        Message message = h.obtainMessage(BluetoothLounge.MESSAGE_READ, bytes, -1, buffer);
                        Bundle bundle = new Bundle();
                        bundle.putString(BluetoothLounge.DEVICE_ADDRESS, __bluetoothDevice.getAddress());
                        message.setData(bundle);
                        message.sendToTarget();
                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost(__bluetoothDevice);
                    break;
                }
            }
        }

        /**
         * Write to the connectionSuccessful OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                __outStream.write(buffer);

                // Share the sent message back to the UI Activity
                for (Handler h : _handlers) {
                    h.obtainMessage(BluetoothLounge.MESSAGE_WRITE, -1, -1, buffer)
                            .sendToTarget();
                }

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                __socket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
