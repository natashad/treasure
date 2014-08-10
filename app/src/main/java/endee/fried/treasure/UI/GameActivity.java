package endee.fried.treasure.UI;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import endee.fried.treasure.BluetoothLounge;
import endee.fried.treasure.BluetoothManager;
import endee.fried.treasure.GameInvitationFragment;
import endee.fried.treasure.GameLogic.Action;
import endee.fried.treasure.GameLogic.Game;
import endee.fried.treasure.GameLogic.SendActionCallback;
import endee.fried.treasure.InviteeLounge;
import endee.fried.treasure.R;


public class GameActivity extends Activity {
    private final static String TAG = GameActivity.class.getName();

    private Game _game;
    private BluetoothManager _bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_layout);

        Bundle extras = getIntent().getExtras();
        long seed = extras.getLong(GameInvitationFragment.GAME_SEED);
        int playerNumber = extras.getInt(InviteeLounge.PLAYER_NUMBER_PRE);
        int numPlayers = extras.getInt(InviteeLounge.NUMBER_OF_PLAYERS);

        if (seed == -1) {
            seed = new Random().nextLong();
        }

        Log.i(TAG, "Random seed: " + seed);

        _game = new Game(numPlayers, playerNumber, seed, this, new Callback() {
            @Override
            public void doAction() {
                Log.d("", "Invalidating in game _callback");
                findViewById(R.id.map_view).invalidate();
                findViewById(R.id.action_point_view).invalidate();
                findViewById(R.id.status_view).invalidate();
            }
        },new SendActionCallback() {
            @Override
            public void send(Action action) {
                if(_game.getNumPlayers() > 1) {
                    Log.e(TAG, "Sending a message");
                    sendMessage(action.toJSON(), "");
                }
            }
        });

        ((MapView)findViewById(R.id.map_view)).init(_game);
        ((ActionPointView)findViewById(R.id.action_point_view)).init(_game);
        ((StatusView)findViewById(R.id.status_view)).init(_game);

        _bluetoothManager = BluetoothManager.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        _bluetoothManager.registerHandler(handler);
    }

    @Override
    protected void onPause() {
        super.onPause();

        _bluetoothManager.removeHandler(handler);
    }

    /**
     * Sends a message.
     * @param json  A JSONObject to send.
     */
    private void sendMessage(JSONObject json, String except) {
        // Check that we're actually connected before trying anything
        if (_bluetoothManager.getState() != BluetoothManager.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        String message = json.toString();

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            _bluetoothManager.writeToEveryone(send, except);
        } else {
            throw new RuntimeException("Sending empty message");
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothLounge.MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    break;
                case BluetoothLounge.MESSAGE_WRITE:
                    Log.i(TAG, "MESSAGE_WRITE: " + msg.arg1);
                    break;
                case BluetoothLounge.MESSAGE_READ:
                    Log.i(TAG, "MESSAGE_READ: " + msg.arg1);
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    try {
                        _game.addOpponentAction(Action.fromJSON(new JSONObject(readMessage)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    break;
                case BluetoothLounge.MESSAGE_DEVICE_NAME:
                    Log.i(TAG, "MESSAGE_DEVICE_NAME: " + msg.arg1);
                    break;
                case BluetoothLounge.MESSAGE_TOAST:
                    Log.i(TAG, "MESSAGE_TOAST: " + msg.arg1);
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothLounge.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
