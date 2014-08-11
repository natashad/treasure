package endee.fried.treasure;

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothLounge extends Activity {
    // Debugging
    private static final String TAG = "BluetoothLounge";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;

    // Key names received from the BluetoothChatService Handler
    public static final String CONNECTION_STATE = "connection state";
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String TOAST = "toast";

    // Intent request codes
    protected static final int REQUEST_CONNECT_DEVICE = 1;
    protected static final int REQUEST_ENABLE_BT = 2;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothManager mBluetoothManager = null;

    // Connected devices in the form of Address:Name pairs.
    private HashMap<String, BluetoothConnection> mConnectedDevices = new HashMap<String, BluetoothConnection>();

    private ArrayList<String> mInvitedDevices = new ArrayList<String>();

    // List of Connected devices in the UI
    private ArrayAdapter<BluetoothConnection> mConnectedListAdapter = null;

    // The seed used for starting a game.
    private long mSeed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        setContentView(R.layout.main);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        findViewById(R.id.inviteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSeed = new Random().nextLong();
//                ((Context)BluetoothLounge.this).startActivity(new Intent(BluetoothLounge.this, GameActivity.class));

                boolean failed = false;

                for (int i = 0; i < mInvitedDevices.size(); i++) {
                    if(!mBluetoothManager.connectedToDevice(mInvitedDevices.get(i))) {


                        Toast.makeText(BluetoothLounge.this, "Error no longer connected to " + mConnectedDevices.get(mInvitedDevices.get(i)).getName(), Toast.LENGTH_SHORT).show();

                        mConnectedDevices.remove(mInvitedDevices.get(i));
                        mInvitedDevices.remove(i);
                        i--;

                        failed = true;
                    }
                }

                if(failed) {
                    mConnectedListAdapter.clear();
                    mConnectedListAdapter.addAll(mConnectedDevices.values());
                    return;
                }


                // What happens when you hit the invite button.
                for (int i = 0; i < mInvitedDevices.size(); i++) {
                    Log.d(TAG, mInvitedDevices.get(i));
                    JSONObject json = new JSONObject();
                    try {
                        json.put(BluetoothManager.GAME_INVITATION, true);
                        json.put(InviteeLounge.GAME_SEED_PRE, mSeed);
                        JSONArray invitedPlayers = new JSONArray(mInvitedDevices);
                        // Add your own address
                        invitedPlayers.put(mBluetoothAdapter.getAddress());
                        json.put(InviteeLounge.INITIAL_INVITED_LIST_PRE, invitedPlayers);
                        // i+1 so that the host is always player 0.
                        json.put(InviteeLounge.PLAYER_NUMBER_PRE, i+1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sendMessage(json.toString(), mInvitedDevices.get(i));
                }

                Intent intent = new Intent(BluetoothLounge.this, InviteeLounge.class);
                intent.putExtra(BluetoothManager.IS_HOST, true);
                intent.putExtra(GameInvitationFragment.GAME_SEED, mSeed);
                intent.putExtra(InviteeLounge.PLAYER_NUMBER_PRE, 0);
                ArrayList<String> allMembers = mInvitedDevices;
                allMembers.add(mBluetoothAdapter.getAddress());
                intent.putExtra(InviteeLounge.INITIAL_INVITED_LIST_PRE, allMembers);

                BluetoothLounge.this.startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mBluetoothManager == null) setupBluetoothManager();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothManager != null) {
            mBluetoothManager.registerHandler(mHandler);
        }
    }

    private void setupBluetoothManager() {
        Log.d(TAG, "setupBluetoothManager()");

//        // Initialize the array adapter for the conversation thread
//        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
//        mConversationView = (ListView) findViewById(R.id.in);
//        mConversationView.setAdapter(mConversationArrayAdapter);
//
//        // Initialize the compose field with a listener for the return key
//        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
//        mOutEditText.setOnEditorActionListener(mWriteListener);
//
//        // Initialize the send button with a listener that for click events
//        mSendButton = (Button) findViewById(R.id.button_send);
//        mSendButton.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                // Send a message using content of the edit text widget
//                TextView view = (TextView) findViewById(R.id.edit_text_out);
//                String message = view.getText().toString();
//                sendMessage(message);
//            }
//        });
        ListView connectedDevicesList = (ListView) findViewById(R.id.connectedList);
        mConnectedListAdapter = new ArrayAdapter<BluetoothConnection>(this, R.layout.connected_devices);
        connectedDevicesList.setAdapter(mConnectedListAdapter);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mBluetoothManager = BluetoothManager.getInstance();

        // Set an onClick Listener to Check and Uncheck items for invitation
        connectedDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckedTextView ctv = (CheckedTextView) view;
                String address = ((BluetoothConnection)adapterView.getItemAtPosition(i)).getAddress();
                if (ctv.isChecked()) {
                    ctv.setChecked(false);
                    if (mInvitedDevices.contains(address)) {
                        mInvitedDevices.remove(address);
                    }
                }
                else {
                    ctv.setChecked(true);
                    if (!mInvitedDevices.contains(address)) {
                        mInvitedDevices.add(address);
                    }
                }
            }
        });

//        // Initialize the buffer for outgoing messages
//        mOutStringBuffer = new StringBuffer("");
    }


    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
        if (mBluetoothManager != null) {
            mBluetoothManager.unregisterHandler(mHandler);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mBluetoothManager.dropAllConnections();
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message, String deviceAddress) {
        // Check that we're actually connected before trying anything
        if (!mBluetoothManager.connectedToDevice(deviceAddress)) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothManager.write(send, deviceAddress);
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String address = msg.getData().getString(DEVICE_ADDRESS);
            String name = msg.getData().getString(DEVICE_NAME);

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE");

                    BluetoothManager.ConnectionState state = (BluetoothManager.ConnectionState)msg.getData().getSerializable(BluetoothLounge.CONNECTION_STATE);

                    switch (state) {
                        case Connected:
                            if(mConnectedDevices.containsKey(address)) break;
                            mConnectedDevices.put(address, new BluetoothConnection(name, address));
                            mConnectedListAdapter.add(mConnectedDevices.get(address));
                            Toast.makeText(getApplicationContext(), "Connected to "
                                    + name, Toast.LENGTH_SHORT).show();
                            break;
                        case Failed:
                            Toast.makeText(getApplicationContext(), "Failed to connect to " + name,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case Dropped:
                            mConnectedListAdapter.remove(mConnectedDevices.get(address));
                            mConnectedDevices.remove(address);
                            mInvitedDevices.remove(address);
                            Toast.makeText(getApplicationContext(), "Connection to " + name + " was dropped",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mBluetoothManager.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupBluetoothManager();
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

    public class BluetoothConnection {

        private String name;
        private String address;

        public BluetoothConnection(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return this.name;
        }

        public String getAddress() {
            return this.address;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

}
