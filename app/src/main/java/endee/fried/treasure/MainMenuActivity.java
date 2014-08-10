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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import endee.fried.treasure.UI.MenuView;

public class MainMenuActivity extends Activity {

    //Debug
    private static final String TAG = "MainMenuActivity";
    private static final boolean D = true;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager mBluetoothManager = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MenuView view = new MenuView(this);
        setContentView(view);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
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
            if (mBluetoothManager == null) setupBluetoothManager();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothManager != null) {

            mBluetoothManager.registerHandler(mHandler);
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothManager.getState() == BluetoothManager.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothManager.start();
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothManager != null) {
            mBluetoothManager.removeHandler(mHandler);
        }
    }

    private void setupBluetoothManager() {
        Log.d(TAG, "setupBluetoothManager()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mBluetoothManager = BluetoothManager.getInstance();

    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothLounge.MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothManager.STATE_CONNECTED:

                            break;
                        case BluetoothManager.STATE_CONNECTING:

                            break;
                        case BluetoothManager.STATE_LISTEN:
                        case BluetoothManager.STATE_NONE:
                            break;
                    }
                    break;
                case BluetoothLounge.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    break;
                case BluetoothLounge.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    try {
                        JSONObject json = new JSONObject(readMessage);
                        if (json.has(BluetoothManager.GAME_INVITATION)) {

                            if (D) Log.d(TAG, "Received a game invitation");

                            long seed = json.getLong(InviteeLounge.GAME_SEED_PRE);
                            int playerNumber = json.getInt(InviteeLounge.PLAYER_NUMBER_PRE);
                            ArrayList<String> invitedList =  new ArrayList<String>();
                            JSONArray jsonArray = json.getJSONArray(InviteeLounge.INITIAL_INVITED_LIST_PRE);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                invitedList.add((String)jsonArray.get(i));
                            }

                            GameInvitationFragment invitation = new GameInvitationFragment();
                            Bundle bundle = new Bundle();
                            bundle.putLong(GameInvitationFragment.GAME_SEED, seed);
                            bundle.putInt(InviteeLounge.PLAYER_NUMBER_PRE, playerNumber);
                            bundle.putStringArrayList(InviteeLounge.INITIAL_INVITED_LIST_PRE, invitedList);
                            invitation.setArguments(bundle);
                            // Doing this check to hopefully prevent the exception I was getting:
                            // java.lang.IllegalStateException: Can not perform this action after
                            // onSaveInstanceState dialogfragment

                            if (!MainMenuActivity.this.isFinishing())
                            {
                                invitation.show(MainMenuActivity.this.getFragmentManager(), TAG);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                    break;
                case BluetoothLounge.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    break;
                case BluetoothLounge.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothLounge.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
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
                    setupBluetoothManager();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

}
