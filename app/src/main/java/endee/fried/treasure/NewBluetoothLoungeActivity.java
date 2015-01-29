package endee.fried.treasure;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import endee.fried.treasure.Bluetooth.BluetoothConnection;
import endee.fried.treasure.UI.BluetoothDeviceListAdapter;
import endee.fried.treasure.UI.InvitedDeviceListAdapter;

public class NewBluetoothLoungeActivity extends Activity {

    // CONSTANTS
    private static final String TAG = NewBluetoothLoungeActivity.class.getName();
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

    private BluetoothAdapter _bluetoothAdapter;
    // Member object for the chat services
    private BluetoothManager _bluetoothManager = null;
    // Connected devices in the form of Address:Name pairs.
    private HashMap<String, BluetoothConnection> _connectedDevices = new HashMap<String, BluetoothConnection>();
    // List of Connected devices in the UI
    private BluetoothDeviceListAdapter _availableListAdapter;
    // List of all connected devices selected for invitation.
    private HashMap<String, BluetoothConnection> _invitedDeviceList = new HashMap<String, BluetoothConnection>();
    private InvitedDeviceListAdapter _invitedDevicesAdapter;
    private HashMap<String, Boolean> _invitedPlayersReadyState = new HashMap();

    private ProgressDialog _scanningDialogue;
    private boolean _isSearching = false;
    private long _seed;

    private boolean _isHost = true;
    private int _playerNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bluetooth_lounge);
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


         if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(BluetoothManager.GAME_INVITATION)) {
             _isHost = false;
         }

        // If the adapter is null, then Bluetooth is not supported
        if (_bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(_receiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(_receiver, filter);

        _seed = new Random().nextLong();

        setUpUI();
        if (_isHost) {
            doDiscovery();
        } else {
            _seed = getIntent().getExtras().getLong(GameInvitationFragment.GAME_SEED);

            ArrayList<String> tempInvitedList = getIntent().getExtras().getStringArrayList(InviteeLounge.INITIAL_INVITED_LIST_PRE );
            for (String invited : tempInvitedList) {
                String address = invited.substring(0, invited.indexOf("@"));
                if (address.equals(_bluetoothAdapter.getAddress())) {
                    continue;
                    //don't add ourselves to the invited list.
                }
                String name = invited.substring(invited.indexOf("@") + 1);
                _invitedDeviceList.put(address, new BluetoothConnection(name, address));
            }
            _invitedDevicesAdapter.addAll(_invitedDeviceList.values());

            _playerNumber = getIntent().getExtras().getInt(InviteeLounge.PLAYER_NUMBER_PRE);
        }

    }

    private void setUpUI() {

        _invitedDevicesAdapter = new InvitedDeviceListAdapter(this, R.layout.invited_device_listitem,
                new ArrayList<BluetoothConnection>());

        ListView invitedListView = (ListView)findViewById(R.id.invitedDeviceListView);

        TextView invitedTitle = new TextView(this);
        invitedTitle.setText("Invited Devices");
        invitedListView.addHeaderView(invitedTitle, "", false);

        invitedListView.setAdapter(_invitedDevicesAdapter);

        // Only do the rest of the setup if you are the host.
        if (!_isHost) {
            findViewById(R.id.scanButton).setVisibility(View.INVISIBLE);
            return;
        }

        _scanningDialogue = new ProgressDialog(this);
        _scanningDialogue.setMessage("Scanning...");


        // PAIRED DEVICES
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = _bluetoothAdapter.getBondedDevices();

        ArrayList<BluetoothConnection> pairedList = new ArrayList<BluetoothConnection>();
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                BluetoothConnection c = new BluetoothConnection(device.getName(), device.getAddress());
                pairedList.add(c);
            }
        }

        BluetoothDeviceListAdapter adapter = new BluetoothDeviceListAdapter(this,
                R.layout.bluetooth_devices_listitem, pairedList);
        ListView pairedListView = (ListView)findViewById(R.id.pairedDeviceListView);

        TextView pairedTitle = new TextView(this);
        pairedTitle.setText("Paired Devices");
        pairedListView.addHeaderView(pairedTitle, "", false);

        pairedListView.setAdapter(adapter);

        pairedListView.setOnItemClickListener(new DeviceOnItemClickListener());


        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDiscovery();
            }
        });


        _availableListAdapter = new BluetoothDeviceListAdapter(this,
                R.layout.bluetooth_devices_listitem, new ArrayList<BluetoothConnection>());
        ListView availableListView = (ListView)findViewById(R.id.availableDeviceListView);

        TextView availableTitle = new TextView(this);
        availableTitle.setText("Available Devices");
        availableListView.addHeaderView(availableTitle, "", false);

        availableListView.setAdapter(_availableListAdapter);

        availableListView.setOnItemClickListener(new DeviceOnItemClickListener());


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



    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        _availableListAdapter.clear();
        _isSearching = true;
        _scanningDialogue.show();

        // If we're already discovering, stop it
        if (_bluetoothAdapter.isDiscovering()) {
            _bluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        _bluetoothAdapter.startDiscovery();
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

                            if(_invitedDeviceList.containsKey(address)) break;
                            sendInvitation(name, address);
                            Toast.makeText(getApplicationContext(), "Connected to "
                                    + name, Toast.LENGTH_SHORT).show();
                            break;
                        case Failed:
                            if (_invitedDeviceList.containsKey(address)) {
                                _invitedDeviceList.remove(address);
                                _invitedPlayersReadyState.remove(address);
                                invitationsUpdated();
                            }
                            Toast.makeText(getApplicationContext(), "Failed to connect to " + name,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case Dropped:
                            _availableListAdapter.remove(_connectedDevices.get(address));
                            if (_invitedDeviceList.containsKey(address)) {
                                _invitedDeviceList.remove(address);
                                _invitedPlayersReadyState.remove(address);
                                invitationsUpdated();
                            }
                            Toast.makeText(getApplicationContext(), "Connection to " + name + " was dropped",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;

                //TODO: Test all of this
                case BluetoothLounge.MESSAGE_READ:
                    Log.i(TAG, "RECEIVING A MESSAGE" + new String((byte[])msg.obj));

                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    try {
                        JSONObject json = new JSONObject(readMessage);

                        if (json.has(InviteeLounge.READY_STRING)) {
                            // a player is ready! Update state and send update to everyone else
                            _invitedPlayersReadyState.put(address, true);
                        }
                        if (json.has(InviteeLounge.LEFT_OR_DECLINED_INVITATION)) {
                            _invitedDeviceList.remove(json.getString(InviteeLounge.LEFT_OR_DECLINED_INVITATION));
                        }
                        if (json.has(InviteeLounge.INITIAL_INVITED_LIST_PRE)) {
                            // The invited list has changed.
                            JSONArray jsonArray = ((JSONArray)json.get(InviteeLounge.INITIAL_INVITED_LIST_PRE));

                            _invitedDeviceList.clear();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                BluetoothConnection con = convertStringToConnection(jsonArray.getString(i));
                                _invitedDeviceList.put(con.getAddress(), con);
                            }
                        }
                        if (json.has(InviteeLounge.PLAYER_NUMBER_PRE)) {
                            // the player list has changed.
                            _playerNumber = json.getInt(InviteeLounge.PLAYER_NUMBER_PRE);
                        }
                        if (json.has(InviteeLounge.ACCEPTED_LIST_PRE)) {
                            JSONArray jsonArray = ((JSONArray)json.get(InviteeLounge.ACCEPTED_LIST_PRE));

                            _invitedPlayersReadyState.clear();

                            for (int i = 0; i < jsonArray.length(); i++) {

                                _invitedPlayersReadyState.put(jsonArray.getString(i), true);

                            }
                        }

                        _invitedDevicesAdapter.clear();
                        _invitedDevicesAdapter.addAll(_invitedDeviceList.values());

                        if (_isHost) {
                            invitationsUpdated();
                        }

                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
            }
        }
    };

    private void sendInvitation(String name, String address) {

        BluetoothConnection c = new BluetoothConnection(name, address);
        _invitedDeviceList.put(address, c);
        _invitedDevicesAdapter.add(c);
        _invitedPlayersReadyState.put(address, false);

        ArrayList<String> allPlayers = getStringListOfAllPlayers();

        JSONObject json = new JSONObject();
        try {
            json.put(BluetoothManager.GAME_INVITATION, true);
            json.put(InviteeLounge.GAME_SEED_PRE, _seed);

            JSONArray invitedPlayers = new JSONArray(allPlayers);
            json.put(InviteeLounge.INITIAL_INVITED_LIST_PRE, invitedPlayers);
            int playerNum = allPlayers.indexOf(c.getAddress() + "@" + c.getName());
            json.put(InviteeLounge.PLAYER_NUMBER_PRE, playerNum);
            Log.d(TAG, "Sending invitation to player number: " + playerNum);

            sendMessage(json, address);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        invitationsUpdated();
    }

    private void invitationsUpdated() {

        ArrayList<String> allPlayers = getStringListOfAllPlayers();
        for (String address : _invitedDeviceList.keySet()) {
            //TODO: Encapsulate this. Being used in sendInvitation as well
            JSONObject json = new JSONObject();
            try {
                json.put(BluetoothManager.GAME_INVITATION, true);
                json.put(InviteeLounge.GAME_SEED_PRE, _seed);

                JSONArray invitedPlayers = new JSONArray(allPlayers);
                json.put(InviteeLounge.INITIAL_INVITED_LIST_PRE, invitedPlayers);
                int playerNum = allPlayers.indexOf(address + "@" + _invitedDeviceList.get(address).getName());
                json.put(InviteeLounge.PLAYER_NUMBER_PRE, playerNum);
                Log.d(TAG, "Sending invitation to player number: " + playerNum);

                JSONArray readyPlayers = new JSONArray();
                for (String add : _invitedPlayersReadyState.keySet()) {
                    if (_invitedPlayersReadyState.get(add)) {
                        readyPlayers.put(add);
                    }
                }
                json.put(InviteeLounge.ACCEPTED_LIST_PRE, readyPlayers);
                sendMessage(json, address);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

    }

    private ArrayList<String> getStringListOfAllPlayers() {
        // This needs to be a string array so it can be easily passed around in bundles and json.
        // The format i'm using is Address@Name since address can never have an '@' and so we can
        // simply split from the first instance of @ to separate them.
        ArrayList<String> allPlayers = new ArrayList<String>();
        for (BluetoothConnection conn : _invitedDeviceList.values()) {
            allPlayers.add(conn.getAddress() + "@" + conn.getName());
        }
        // Add the host to this list.
        allPlayers.add(0, _bluetoothAdapter.getAddress() + "@" + _bluetoothAdapter.getName());
        return allPlayers;
    }

    private BluetoothConnection convertStringToConnection(String longString) {
        String address = longString.substring(0, longString.indexOf("@"));
        String name = longString.substring(longString.indexOf("@") + 1);
        return new BluetoothConnection(name, address);
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

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver _receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                Log.d(TAG, "discovered something");

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    // devices that are already paired, in case we care.
                }

                final String name = device.getName();
                final String address = device.getAddress();

                BluetoothConnection conn = new BluetoothConnection(name, device.getAddress());
                _availableListAdapter.add(conn);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Log.d(TAG, "Discovery finished");

                if (_availableListAdapter.isEmpty()) {
                    Toast.makeText(NewBluetoothLoungeActivity.this,
                            "No Connections are available. Ensure that other devices are discoverable.",
                            Toast.LENGTH_LONG).show();
                }

                _isSearching = false;
                _scanningDialogue.dismiss();

            }
        }
    };

    private class DeviceOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(TAG, "Touched button at position " + i);
            BluetoothConnection device = (BluetoothConnection) adapterView.getAdapter().getItem(i);

            BluetoothDevice btDevice = _bluetoothAdapter.getRemoteDevice(device.getAddress());
            if (_invitedDeviceList.containsKey(btDevice)) {
                Toast.makeText(NewBluetoothLoungeActivity.this,
                        "Already connected to " + device.getName(), Toast.LENGTH_LONG).show();
            } else {
                // Attempt to connect to the device
                _bluetoothManager.connect(btDevice);
            }
        }
    }


}
