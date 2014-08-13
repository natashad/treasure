package endee.fried.treasure;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import endee.fried.treasure.Bluetooth.BluetoothConnection;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothLounge extends Activity {

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
    // List of Connected devices in the UI
    private ArrayAdapter<BluetoothConnection> _connectedListAdapter = null;
    // The seed used for starting a game.
    private long _seed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        setContentView(R.layout.main);

        // Get local Bluetooth adapter
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (_bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setUpUI();

    }

    private void setUpUI() {
        // Set the OnClick listener on the INVITE Button.
        findViewById(R.id.inviteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _seed = new Random().nextLong();

                boolean failed = false;

                // Check for inconsistencies in the invited list. i.e. Check if a device that
                // was marked invited is no longer connected and fail.
                for (int i = 0; i < _invitedDevices.size(); i++) {
                    if(!_bluetoothManager.connectedToDevice(_invitedDevices.get(i))) {

                        Toast.makeText(BluetoothLounge.this, "Error no longer connected to " +
                                        _connectedDevices.get(_invitedDevices.get(i)).getName(),
                                Toast.LENGTH_SHORT).show();

                        _connectedDevices.remove(_invitedDevices.get(i));
                        _invitedDevices.remove(i);
                        i--;

                        failed = true;
                    }
                }

                if(failed) {
                    // Repopulate the connected List in the UI
                    _connectedListAdapter.clear();
                    _connectedListAdapter.addAll(_connectedDevices.values());
                    _invitedDevices.clear();
                    return;
                }


                ArrayList<String> allPlayers = _invitedDevices;
                // add yourself to the all players list.
                allPlayers.add(_bluetoothAdapter.getAddress());

                for (int i = 0; i < _invitedDevices.size(); i++) {
                    Log.d(TAG, _invitedDevices.get(i));
                    JSONObject json = new JSONObject();
                    try {
                        json.put(BluetoothManager.GAME_INVITATION, true);
                        json.put(InviteeLounge.GAME_SEED_PRE, _seed);
                        JSONArray invitedPlayers = new JSONArray(allPlayers);
                        json.put(InviteeLounge.INITIAL_INVITED_LIST_PRE, invitedPlayers);
                        // i+1 so that the host is always player 0.
                        json.put(InviteeLounge.PLAYER_NUMBER_PRE, i+1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sendMessage(json, _invitedDevices.get(i));
                }

                Intent intent = new Intent(BluetoothLounge.this, InviteeLounge.class);
                intent.putExtra(GameInvitationFragment.GAME_SEED, _seed);
                // Host is always Player 0.
                intent.putExtra(InviteeLounge.PLAYER_NUMBER_PRE, 0);
                intent.putExtra(InviteeLounge.INITIAL_INVITED_LIST_PRE, allPlayers);

                BluetoothLounge.this.startActivity(intent);
            }
        });



        ListView connectedDevicesList = (ListView) findViewById(R.id.connectedList);
        _connectedListAdapter = new ArrayAdapter<BluetoothConnection>(this, R.layout.connected_devices);
        connectedDevicesList.setAdapter(_connectedListAdapter);

        // Set an onClick Listener to Check and Uncheck items for invitation
        connectedDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckedTextView ctv = (CheckedTextView) view;
                String address = ((BluetoothConnection)adapterView.getItemAtPosition(i)).getAddress();
                if (ctv.isChecked()) {
                    ctv.setChecked(false);
                    if (_invitedDevices.contains(address)) {
                        _invitedDevices.remove(address);
                    }
                }
                else {
                    ctv.setChecked(true);
                    if (!_invitedDevices.contains(address)) {
                        _invitedDevices.add(address);
                    }
                }
            }
        });
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
                            _connectedListAdapter.add(_connectedDevices.get(address));
                            Toast.makeText(getApplicationContext(), "Connected to "
                                    + name, Toast.LENGTH_SHORT).show();
                            break;
                        case Failed:
                            Toast.makeText(getApplicationContext(), "Failed to connect to " + name,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case Dropped:
                            _connectedListAdapter.remove(_connectedDevices.get(address));
                            _connectedDevices.remove(address);
                            _invitedDevices.remove(address);
                            Toast.makeText(getApplicationContext(), "Connection to " + name + " was dropped",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = _bluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    _bluetoothManager.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a BluetoothManager
                    _bluetoothManager = BluetoothManager.getInstance();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }

}
