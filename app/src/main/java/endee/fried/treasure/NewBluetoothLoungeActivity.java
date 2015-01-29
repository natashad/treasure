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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import endee.fried.treasure.Bluetooth.BluetoothConnection;
import endee.fried.treasure.UI.BluetoothDeviceListAdapter;
import endee.fried.treasure.UI.InvitedDeviceListAdapter;
import endee.fried.treasure.UI.InvitedPlayer;

public class NewBluetoothLoungeActivity extends Activity {

    // CONSTANTS
    private static final String TAG = NewBluetoothLoungeActivity.class.getName();
    // Message types sent from the BluetoothManager
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;


    public static final String HOST_KEY = "Host";
    public static final String READY_KEY = "Ready";
    public static final String GAME_SEED_KEY = "Seed";
    public static final String JSON_KEY = "JSON";
    // Intent request codes
    protected static final int REQUEST_CONNECT_DEVICE = 1;
    protected static final int REQUEST_ENABLE_BT = 2;

    public static final String PLAYERS_KEY = "Players";

    private BluetoothAdapter _bluetoothAdapter;
    // Member object for the chat services
    private BluetoothManager _bluetoothManager = null;
    // Connected devices in the form of Address:Name pairs.
    private final HashMap<String, BluetoothConnection> _connectedDevices = new HashMap<String, BluetoothConnection>();
    // List of Connected devices in the UI
    private BluetoothDeviceListAdapter _availableListAdapter;
    // List of all connected devices selected for invitation.
    private final List<InvitedPlayer>_invitedDeviceList = new ArrayList<InvitedPlayer>();
    private InvitedDeviceListAdapter _invitedDevicesAdapter;

    private ProgressDialog _scanningDialogue;
    private boolean _isSearching = false;
    private long _seed;

    private boolean _isHost = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bluetooth_lounge);
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


         if (getIntent().getExtras() != null && getIntent().getExtras().getString(JSON_KEY) != null) {
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

        setUpUI();

        if (_isHost) {
            _seed = new Random().nextLong();
            _invitedDeviceList.add(new InvitedPlayer(new BluetoothConnection(_bluetoothAdapter.getName(), _bluetoothAdapter.getAddress()), true));
            doDiscovery();
        } else {
            // second argument is null because we don't know from player, but it wont be used in this case
            updateModelFromJSON(getIntent().getExtras().getString(JSON_KEY), null);
        }

        _invitedDevicesAdapter.notifyDataSetInvalidated();

    }

    private void setUpUI() {

        _invitedDevicesAdapter = new InvitedDeviceListAdapter(this, R.layout.invited_device_listitem,
                _invitedDeviceList, _bluetoothAdapter.getAddress());

        ListView invitedListView = (ListView)findViewById(R.id.invitedDeviceListView);

        TextView invitedTitle = new TextView(this);
        invitedTitle.setText("Invited Devices");
        invitedListView.addHeaderView(invitedTitle, "", false);

        invitedListView.setAdapter(_invitedDevicesAdapter);

        ToggleButton toggle = (ToggleButton)findViewById(R.id.readyToggleButton);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                getInvitedPlayer(_bluetoothAdapter.getAddress()).setReady(isChecked);

                JSONObject json = new JSONObject();
                try {
                    json.put(InviteeLounge.READY_KEY, isChecked);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // If not host, this will only send to host
                // If we are the host it sends to everyone
                sendMessageToAll(json, "");
            }
        });

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

    private InvitedPlayer getInvitedPlayer(String address) {
        if(address == null) return null;

        for(InvitedPlayer player : _invitedDeviceList) {
            if(address.equals(player.getConnection().getAddress())) {
                return player;
            }
        }
        return null;
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String address = msg.getData().getString(BluetoothManager.DEVICE_ADDRESS_KEY);
            String name = msg.getData().getString(BluetoothManager.DEVICE_NAME_KEY);

            InvitedPlayer player = getInvitedPlayer(address);

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE");

                    BluetoothManager.ConnectionState state = (BluetoothManager.ConnectionState)msg.getData().
                            getSerializable(BluetoothManager.CONNECTION_STATE);

                    switch (state) {
                        case Connected:

                            if(player != null) break;
                            sendInvitation(name, address);
                            Toast.makeText(getApplicationContext(), "Connected to "
                                    + name, Toast.LENGTH_SHORT).show();
                            break;
                        case Failed:
                            if (player != null) {
                                _invitedDeviceList.remove(player);
                                sendUpdatedPlayerInfo();
                            }
                            Toast.makeText(getApplicationContext(), "Failed to connect to " + name,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case Dropped:
                            _availableListAdapter.remove(_connectedDevices.get(address));
                            if (player != null) {
                                _invitedDeviceList.remove(player);
                                sendUpdatedPlayerInfo();
                            }
                            Toast.makeText(getApplicationContext(), "Connection to " + name + " was dropped",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;

                //TODO: Test all of this
                case BluetoothLounge.MESSAGE_READ:
                    Log.i(TAG, "RECEIVING A MESSAGE" + new String((byte[])msg.obj));

                    updateModelFromJSON(new String((byte[]) msg.obj, 0, msg.arg1), player);

                    _invitedDevicesAdapter.notifyDataSetInvalidated();

                    if (_isHost) {
                        sendUpdatedPlayerInfo();
                    }
            }
        }
    };

    private void updateModelFromJSON(String message, InvitedPlayer fromPlayer) {
        try {
            JSONObject json = new JSONObject(message);

            if (json.has(InviteeLounge.READY_KEY)) {
                // fromPlayer is null if this is called when invited
                // but this step cant happen
                fromPlayer.setReady(json.getBoolean(InviteeLounge.READY_KEY));
            }
            if (json.has(InviteeLounge.LEFT_OR_DECLINED_INVITATION)) {
                _invitedDeviceList.remove(json.getString(InviteeLounge.LEFT_OR_DECLINED_INVITATION));
            }
            if (json.has(PLAYERS_KEY)) {
                // The invited list has changed.
                updatePlayersFromJSON((JSONArray)json.get(PLAYERS_KEY));
            }
            if (json.has(GAME_SEED_KEY)) {
                // The invited list has changed.
                _seed = json.getLong(GAME_SEED_KEY);
            }

        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendInvitation(String name, String address) {

        BluetoothConnection c = new BluetoothConnection(name, address);
        _invitedDeviceList.add(new InvitedPlayer(c, false));
        _invitedDevicesAdapter.notifyDataSetInvalidated();

        // This will send info to new player, and send info about new player to everyone else
        sendUpdatedPlayerInfo();
    }

    private void sendUpdatedPlayerInfo() {
        JSONObject json = new JSONObject();
        JSONArray allPlayers = getJSONOfPlayers();

        try {
            json.put(BluetoothManager.GAME_INVITATION, true);
            json.put(GAME_SEED_KEY, _seed);
            json.put(PLAYERS_KEY, allPlayers);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Sending updated info to players");
        sendMessageToAll(json, "");
    }

    private JSONArray getJSONOfPlayers() {
        JSONArray allPlayers = new JSONArray();
        for (InvitedPlayer player : _invitedDeviceList) {
            JSONObject json = new JSONObject();
            try {
                json.put(BluetoothManager.DEVICE_NAME_KEY, player.getConnection().getName());
                json.put(BluetoothManager.DEVICE_ADDRESS_KEY, player.getConnection().getAddress());
                json.put(READY_KEY, player.isReady());
                json.put(HOST_KEY, player.isHost());
            }catch (JSONException e) {
                e.printStackTrace();
            }

            allPlayers.put(json);
        }

        return allPlayers;
    }

    private void updatePlayersFromJSON(JSONArray playersJSON) {
        _invitedDeviceList.clear();

        for (int i = 0; i < playersJSON.length(); i++) {
            try {
                JSONObject player = playersJSON.getJSONObject(i);

                _invitedDeviceList.add(new InvitedPlayer(
                        new BluetoothConnection(
                                player.getString(BluetoothManager.DEVICE_NAME_KEY), player.getString(BluetoothManager.DEVICE_ADDRESS_KEY)),
                        player.getBoolean(HOST_KEY),
                        player.getBoolean(READY_KEY)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessageToAll(JSONObject json, String except) {
        _bluetoothManager.writeToEveryone(json.toString().getBytes(), except);
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

        // Get the message bytes and tell the BluetoothManager to write
        _bluetoothManager.write(json.toString().getBytes(), deviceAddress);
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

                // TODO check if we already have the device in the list

                BluetoothConnection conn = new BluetoothConnection(device.getName(), device.getAddress());
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

            Log.d(TAG, "Trying to connect with " + device.getName());
            if (getInvitedPlayer(device.getAddress()) != null) {
                Toast.makeText(NewBluetoothLoungeActivity.this,
                        "Already connected to " + device.getName(), Toast.LENGTH_LONG).show();
            } else {
                // Attempt to connect to the device
                _bluetoothManager.connect(_bluetoothAdapter.getRemoteDevice(device.getAddress()));
            }
        }
    }


}
