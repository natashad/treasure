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

    //Debug
    private static final boolean D = true;
    public static final String TAG = "InviteeLounge";
    public static final String READY_STRING = "ReadyToStart";
    public static final String ACCEPTED_LIST_PRE = "accptedPeople";
    public static final String WAITING_LIST_PRE = "waitingPeople";
    public static final String GAME_SEED_PRE = "StartingSeed";
    public static final String INITIAL_INVITED_LIST_PRE = "InvitedPlayers";
    public static final String PLAYER_NUMBER_PRE = "PlayerNum";
    public static final String NUMBER_OF_PLAYERS  = "NumberOfPlayers";


    private boolean mIsHost = false;
    private long mGameSeed;
    private int mPlayerNumber;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager mBluetoothManager = null;
    private ArrayAdapter<String> mAcceptedAdapter;
    private ArrayAdapter<String> mWaitingAdapter;
    private ArrayList<String> mAcceptedList = new ArrayList<String>();
    private ArrayList<String> mWaitingList = new ArrayList<String>();
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitee_lounge);

        mIsHost = getIntent().getExtras().getBoolean(BluetoothManager.IS_HOST);
        mGameSeed = getIntent().getExtras().getLong(GameInvitationFragment.GAME_SEED);
        mPlayerNumber = getIntent().getExtras().getInt(InviteeLounge.PLAYER_NUMBER_PRE);
        mWaitingList = getIntent().getExtras().getStringArrayList(INITIAL_INVITED_LIST_PRE);

        ListView acceptedDevicesList = (ListView) findViewById(R.id.acceptedPlayersList);
        mAcceptedAdapter = new ArrayAdapter<String>(this,
                R.layout.invitee_list_element);
        acceptedDevicesList.setAdapter(mAcceptedAdapter);

        ListView waitingDevicesList = (ListView) findViewById(R.id.waitingPlayersList);
        mWaitingAdapter = new ArrayAdapter<String>(this,
                R.layout.invitee_list_element);
        waitingDevicesList.setAdapter(mWaitingAdapter);
        mWaitingAdapter.addAll(mWaitingList);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        final ToggleButton readyButton = ((ToggleButton) findViewById(R.id.readyButton));
        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (readyButton.isChecked()) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put(READY_STRING, true);
                        sendMessage(json, "");

                        mAcceptedList.add(mBluetoothAdapter.getAddress());
                        mWaitingList.remove(mBluetoothAdapter.getAddress());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                    try {
                        JSONObject json = new JSONObject();
                        json.put(READY_STRING, false);
                        sendMessage(json, "");

                        mWaitingList.add(mBluetoothAdapter.getAddress());
                        mAcceptedList.remove(mBluetoothAdapter.getAddress());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                updateArrayAdapters();
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
            startActivityForResult(enableIntent, BluetoothLounge.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }
        else {
            if (mBluetoothManager == null) mBluetoothManager = BluetoothManager.getInstance();;
            mBluetoothManager.startListening();
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

    @Override
    protected void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothManager != null) {
            mBluetoothManager.unregisterHandler(mHandler);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mBluetoothManager.stopListening();
    }

    public static String convertStringArrayToBigString(String[] input, String prefix) {
        String ret = prefix;
        for (String in : input) {
            ret += in + "||";
        }
        return ret;
    }

    public static ArrayList<String> convertBigStringToStringList(String input, String prefix) {
        String in = input;
        ArrayList<String> output = new ArrayList<String>();
        if (input.startsWith(prefix)) {
            in = input.substring(prefix.length());
        }
        while(in.indexOf("||") != -1) {
            output.add(in.substring(0, in.indexOf("||")));
            in = in.substring(in.indexOf("||") + 2);
        }

        return output;
    }

    /**
     * Sends a message.
     * @param json  A JSONObject to send.
     */
    private void sendMessage(JSONObject json, String except) {
        String message = json.toString();

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothManager.writeToEveryone(send, except);
        }
    }

    private void updateArrayAdapters() {
        mAcceptedAdapter.clear();
        for (String accepted : mAcceptedList) {
            mAcceptedAdapter.add(accepted);
        }
        mWaitingAdapter.clear();
        for (String waiting : mWaitingList) {
            mWaitingAdapter.add(waiting);
        }
        if (mWaitingList.isEmpty()) {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(GameInvitationFragment.GAME_SEED, mGameSeed);
            intent.putExtra(InviteeLounge.PLAYER_NUMBER_PRE, mPlayerNumber);
            intent.putExtra(InviteeLounge.NUMBER_OF_PLAYERS, mAcceptedList.size());
            this.startActivity(intent);
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothLounge.MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE");
                    // TODO: display connection error messages
                    break;
                case BluetoothLounge.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

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
                                mWaitingList.remove(address);
                                mAcceptedList.add(address);

                            } else {
                                mWaitingList.add(address);
                                mAcceptedList.remove(address);
                            }
                            JSONObject newJson = new JSONObject();
                            newJson.put(WAITING_LIST_PRE, new JSONArray(mWaitingList));
                            newJson.put(ACCEPTED_LIST_PRE, new JSONArray(mAcceptedList));
                            InviteeLounge.this.sendMessage(newJson, address);
                        }

                        if (json.has(WAITING_LIST_PRE)) {

                            JSONArray jsonArray = ((JSONArray)json.get(WAITING_LIST_PRE));

                            mWaitingList.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mWaitingList.add(jsonArray.getString(i));
                            }
                        }

                        if (json.has(ACCEPTED_LIST_PRE)) {

                            JSONArray jsonArray = ((JSONArray)json.get(ACCEPTED_LIST_PRE));

                            mAcceptedList.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mAcceptedList.add(jsonArray.getString(i));
                            }
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
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case BluetoothLounge.REQUEST_CONNECT_DEVICE:
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
            case BluetoothLounge.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    mBluetoothManager = BluetoothManager.getInstance();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
}
