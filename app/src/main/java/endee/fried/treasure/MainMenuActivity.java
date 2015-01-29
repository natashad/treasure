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

import endee.fried.treasure.UI.Callback;
import endee.fried.treasure.UI.MenuView;

public class MainMenuActivity extends Activity implements Callback {

    //Debug
    private static final String TAG = MainMenuActivity.class.getName();

    // Local Bluetooth adapter
    private BluetoothAdapter _bluetoothAdapter = null;
    private BluetoothManager _bluetoothManager = null;
    private GameInvitationFragment _invitation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MenuView view = new MenuView(this);
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
            startActivityForResult(enableIntent, BluetoothLounge.REQUEST_ENABLE_BT);
        }
        else {
            if (_bluetoothManager == null) _bluetoothManager = BluetoothManager.getInstance();
            _bluetoothManager.startListening();
            //TODO stop listening when?
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

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothLounge.MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE");
                    // TODO: check connection problems
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

                            String name = msg.getData().getString(BluetoothManager.DEVICE_NAME_KEY);

                            Log.d(TAG, "Received a game invitation from " + name);

                            if (_invitation != null) {
                                if (_invitation.isVisible()) {
                                    _invitation.dismiss();
                                }
                            }

                            _invitation = new GameInvitationFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString(NewBluetoothLoungeActivity.JSON_KEY, readMessage);
                            _invitation.setArguments(bundle);

                            // Doing this check to hopefully prevent the exception I was getting:
                            // java.lang.IllegalStateException: Can not perform this action after
                            // onSaveInstanceState dialogfragment
                            if (!MainMenuActivity.this.isFinishing())
                            {
                                _invitation.show(MainMenuActivity.this.getFragmentManager(), TAG);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                    // Bluetooth is now enabled, so set up a chat session
                    _bluetoothManager = BluetoothManager.getInstance();
                    _bluetoothManager.startListening();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public void doAction(Object obj) {
        if (obj.equals(GameInvitationFragment.DECLINE_INVITATION)) {
            // send a message to notify connections that you are declining the game.
            JSONObject json = new JSONObject();
            try {
                json.put(InviteeLounge.LEFT_OR_DECLINED_INVITATION, _bluetoothAdapter.getAddress());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendMessage(json, "");
        }

    }
}
