package endee.fried.treasure;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import endee.fried.treasure.Bluetooth.BluetoothConnection;
import endee.fried.treasure.UI.LoungeView;

public class TemporaryActivity extends Activity {

    // CONSTANTS
    private static final String TAG = BluetoothLounge.class.getName();
    // Message types sent from the BluetoothManager
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    // Key names received from the BluetoothManager
    public static final String CONNECTION_STATE = "connection state";
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";
    // Intent request codes
    protected static final int REQUEST_CONNECT_DEVICE = 1;
    protected static final int REQUEST_ENABLE_BT = 2;

    // MEMBER VARIABLES
    // Local Bluetooth adapter
    private BluetoothAdapter _bluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothManager _bluetoothManager = null;
    // Connected devices in the form of Address:Name pairs.
    private HashMap<String, BluetoothConnection> _connectedDevices = new HashMap<String, BluetoothConnection>();
    // List of all connected devices selected for invitation.
    private ArrayList<String> _invitedDevices = new ArrayList<String>();
    // The seed used for starting a game.
    private long _seed;

    private LoungeView view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        view = new LoungeView(this);
        setContentView(view);

        // Get local Bluetooth adapter
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (_bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


    }



    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        if (!_bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (_bluetoothManager == null) {
                _bluetoothManager = BluetoothManager.getInstance();
            }
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.d(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (_bluetoothManager != null) {
            _bluetoothManager.registerHandler(mHandler);
        }
    }


    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.d(TAG, "- ON PAUSE -");
        if (_bluetoothManager != null) {
            _bluetoothManager.unregisterHandler(mHandler);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        _bluetoothManager.dropAllConnections();
    }

    private void ensureDiscoverable() {
        Log.d(TAG, "ensure discoverable");
        if (_bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param json  A JSONObject to send.
     */
    private void sendMessage(JSONObject json, String deviceAddress) {

        // Check that we're actually connected before trying anything
        if (!_bluetoothManager.connectedToDevice(deviceAddress)) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        String message = json.toString();

        // Get the message bytes and tell the BluetoothManager to write
        byte[] send = message.getBytes();
        _bluetoothManager.write(send, deviceAddress);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String address = msg.getData().getString(DEVICE_ADDRESS);
            String name = msg.getData().getString(DEVICE_NAME);

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE");

                    BluetoothManager.ConnectionState state = (BluetoothManager.ConnectionState)msg.getData().
                            getSerializable(BluetoothLounge.CONNECTION_STATE);

                    switch (state) {
                        case Connected:
                            if(_connectedDevices.containsKey(address)) break;
                            _connectedDevices.put(address, new BluetoothConnection(name, address));
                            Toast.makeText(getApplicationContext(), "Connected to "
                                    + name, Toast.LENGTH_SHORT).show();
                            break;
                        case Failed:
                            Toast.makeText(getApplicationContext(), "Failed to connect to " + name,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case Dropped:
                            _connectedDevices.remove(address);
                            _invitedDevices.remove(address);
                            Toast.makeText(getApplicationContext(), "Connection to " + name + " was dropped",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }

            view.invalidate();
        }
    };

    // CUSTOM TO THIS CLASS:
    public void connectToDevice(BluetoothDevice device) {
        _bluetoothManager.connect(device);
    }

    public HashMap<String, BluetoothConnection> getConnectedDevices() {
        return _connectedDevices;
    }
}
