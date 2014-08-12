package endee.fried.treasure;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import endee.fried.treasure.UI.GameActivity;

public class InviteeLounge extends Activity {

    // CONSTANTS
    public static final String TAG = InviteeLounge.class.getName();
    public static final String READY_STRING = "ReadyToStart";
    public static final String ACCEPTED_LIST_PRE = "accptedPeople";
    public static final String WAITING_LIST_PRE = "waitingPeople";
    public static final String GAME_SEED_PRE = "StartingSeed";
    public static final String INITIAL_INVITED_LIST_PRE = "InvitedPlayers";
    public static final String PLAYER_NUMBER_PRE = "PlayerNum";
    public static final String NUMBER_OF_PLAYERS  = "NumberOfPlayers";
    public static final String JOINED_INVITEE_LOUNGE = "JoinedInviteeLounge";
    public static final String LEFT_OR_DECLINED_INVITATION = "LeftOrDeclinedInvitation";


    // MEMBER VARIABLES
    private long _gameSeed;
    private int _playerNumber;
    private BluetoothAdapter _bluetoothAdapter = null;
    private BluetoothManager _bluetoothManager = null;
    private ArrayAdapter<String> _acceptedAdapter;
    private ArrayAdapter<String> _waitingAdapter;
    private ArrayList<String> _acceptedList = new ArrayList<String>();
    private ArrayList<String> _waitingList = new ArrayList<String>();
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invitee_lounge);

        _gameSeed = getIntent().getExtras().getLong(GameInvitationFragment.GAME_SEED);
        _playerNumber = getIntent().getExtras().getInt(InviteeLounge.PLAYER_NUMBER_PRE);
        _waitingList = getIntent().getExtras().getStringArrayList(INITIAL_INVITED_LIST_PRE);

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
        // Set up the list of Accepted Players.
        ListView acceptedDevicesList = (ListView) findViewById(R.id.acceptedPlayersList);
        _acceptedAdapter = new ArrayAdapter<String>(this,
                R.layout.invitee_list_element);
        acceptedDevicesList.setAdapter(_acceptedAdapter);

        // Set up the list of players that we are waiting on.
        ListView waitingDevicesList = (ListView) findViewById(R.id.waitingPlayersList);
        _waitingAdapter = new ArrayAdapter<String>(this,
                R.layout.invitee_list_element);
        waitingDevicesList.setAdapter(_waitingAdapter);
        _waitingAdapter.addAll(_waitingList);

        // Handle the onClick of the READY button.
        final ToggleButton readyButton = ((ToggleButton) findViewById(R.id.readyButton));
        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (readyButton.isChecked()) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put(READY_STRING, true);
                        sendMessage(json, "");

                        _acceptedList.add(_bluetoothAdapter.getAddress());
                        _waitingList.remove(_bluetoothAdapter.getAddress());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                    try {
                        JSONObject json = new JSONObject();
                        json.put(READY_STRING, false);
                        sendMessage(json, "");

                        _waitingList.add(_bluetoothAdapter.getAddress());
                        _acceptedList.remove(_bluetoothAdapter.getAddress());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                updateArrayAdapters();
            }
        });

    }

    // Based on the acceptedList and waitingList update the UI.
    // Call this on any changes to those 2 lists and if the waiting list
    // is empty, start the game!
    private void updateArrayAdapters() {
        _acceptedAdapter.clear();
        for (String accepted : _acceptedList) {
            _acceptedAdapter.add(accepted);
        }

        _waitingAdapter.clear();
        for (String waiting : _waitingList) {
            _waitingAdapter.add(waiting);
        }

        if (_waitingList.isEmpty()) {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(GameInvitationFragment.GAME_SEED, _gameSeed);
            intent.putExtra(InviteeLounge.PLAYER_NUMBER_PRE, _playerNumber);
            intent.putExtra(InviteeLounge.NUMBER_OF_PLAYERS, _acceptedList.size());
            this.startActivity(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!_bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothLounge.REQUEST_ENABLE_BT);
        }
        else {
            if (_bluetoothManager == null) _bluetoothManager = BluetoothManager.getInstance();
            _bluetoothManager.startListening();

            // send a message to notify connections that you are in the lounge so that you can
            // receive an updated list of the members and their status!
            JSONObject json = new JSONObject();
            try {
                json.put(JOINED_INVITEE_LOUNGE, true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendMessage(json, "");

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
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (_bluetoothManager != null) {
            _bluetoothManager.unregisterHandler(mHandler);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "+ ON DESTROY +");

        // send a message to notify connections that you are leaving the lounge.
        JSONObject json = new JSONObject();
        try {
            json.put(LEFT_OR_DECLINED_INVITATION, _bluetoothAdapter.getAddress());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(json, "");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "+ ON BACK PRESSED +");

        _bluetoothManager.stopListening();
    }

    /**
     * Sends a message to everyone except one person.
     * @param json  A JSONObject to send.
     * @param except A device to not send the message to.
     */
    private void sendMessage(JSONObject json, String except) {
        String message = json.toString();
        byte[] send = message.getBytes();
        _bluetoothManager.writeToEveryone(send, except);

    }

    /**
     * Send a message to just one recipient.
     * @param json
     * @param deviceAddress
     */
    private void sendMessageToOne(JSONObject json, String deviceAddress) {
        String message = json.toString();
        byte[] send = message.getBytes();
        _bluetoothManager.write(send, deviceAddress);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothLounge.MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE");
                    // TODO: display connection error messages
                    break;
                case BluetoothLounge.MESSAGE_READ:
                    Log.i(TAG, "RECEIVING A MESSAGE" + new String((byte[])msg.obj));

                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    String address = msg.getData().getString(BluetoothLounge.DEVICE_ADDRESS);

                    try {
                        JSONObject json = new JSONObject(readMessage);

                        if (json.has(READY_STRING)) {
                            if (json.getBoolean(READY_STRING)) {
                                _waitingList.remove(address);
                                _acceptedList.add(address);

                            } else {
                                _waitingList.add(address);
                                _acceptedList.remove(address);
                            }

                            JSONObject newJson = new JSONObject();
                            newJson.put(WAITING_LIST_PRE, new JSONArray(_waitingList));
                            newJson.put(ACCEPTED_LIST_PRE, new JSONArray(_acceptedList));
                            InviteeLounge.this.sendMessage(newJson, address);
                        }

                        if (json.has(WAITING_LIST_PRE)) {

                            JSONArray jsonArray = ((JSONArray)json.get(WAITING_LIST_PRE));

                            _waitingList.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                _waitingList.add(jsonArray.getString(i));
                            }
                        }

                        if (json.has(ACCEPTED_LIST_PRE)) {

                            JSONArray jsonArray = ((JSONArray)json.get(ACCEPTED_LIST_PRE));

                            _acceptedList.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                _acceptedList.add(jsonArray.getString(i));
                            }
                        }

                        if (json.has(JOINED_INVITEE_LOUNGE)) {
                            // Send someone who has just accepted an invitation an update on the state.
                            JSONObject newJson = new JSONObject();
                            newJson.put(WAITING_LIST_PRE, new JSONArray(_waitingList));
                            newJson.put(ACCEPTED_LIST_PRE, new JSONArray(_acceptedList));
                            InviteeLounge.this.sendMessageToOne(newJson, address);
                        }

                        if (json.has(LEFT_OR_DECLINED_INVITATION)) {
                            // Send everyone a notice that this guy has left the invitation lounge.
                            // Pull him out of the game.
                            _waitingList.remove(address);
                            _acceptedList.remove(address);

                            JSONObject newJson = new JSONObject();
                            newJson.put(LEFT_OR_DECLINED_INVITATION, address);

                            InviteeLounge.this.sendMessage(newJson, address);

                            Toast.makeText(InviteeLounge.this, address + " has quit the game." ,
                                    Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    updateArrayAdapters();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case BluetoothLounge.REQUEST_CONNECT_DEVICE:
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
            case BluetoothLounge.REQUEST_ENABLE_BT:
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
}
