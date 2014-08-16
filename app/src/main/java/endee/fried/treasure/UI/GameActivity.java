package endee.fried.treasure.UI;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import endee.fried.treasure.BluetoothLounge;
import endee.fried.treasure.BluetoothManager;
import endee.fried.treasure.GameInvitationFragment;
import endee.fried.treasure.GameLogic.Action;
import endee.fried.treasure.GameLogic.Game;
import endee.fried.treasure.GameLogic.Item;
import endee.fried.treasure.GameLogic.Player;
import endee.fried.treasure.InviteeLounge;
import endee.fried.treasure.R;


public class GameActivity extends Activity {
    private final static String TAG = GameActivity.class.getName();

    private Game _game;
    private BluetoothManager _bluetoothManager;

    private boolean centerOnMove = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_layout);

        Bundle extras = getIntent().getExtras();
        long seed = extras.getLong(GameInvitationFragment.GAME_SEED);
        int playerNumber = extras.getInt(InviteeLounge.PLAYER_NUMBER_PRE);
        int numPlayers = extras.getInt(InviteeLounge.NUMBER_OF_PLAYERS);

        Log.i(TAG, "Random seed: " + seed);

        final Context context = this;

        _game = new Game(numPlayers, playerNumber, seed, this, new Callback<Object>() {
            @Override
            public void doAction(Object object) {
                Log.d("", "Invalidating in game callback");
                findViewById(R.id.map_view).invalidate();
                findViewById(R.id.action_point_view).invalidate();
                findViewById(R.id.status_view).invalidate();
                findViewById(R.id.item_grid_view).invalidate();
            }
        },new Callback<Action>() {
            @Override
            public void doAction(Action action) {
                if(_game.getNumPlayers() > 1) {
                    Log.e(TAG, "Sending a message");
                    _bluetoothManager.writeToEveryone(action.toJSON().toString().getBytes(), "");
                }
            }
        }, new Callback<Integer>() {
            @Override
            public void doAction(Integer tile) {
                if(centerOnMove) {
                    ((MapView) findViewById(R.id.map_view)).centerOnTile(tile);
                }
            }
        }, new Callback<Item>() {
            @Override
            public void doAction(Item item) {
                ItemGridView gridView = (ItemGridView) findViewById(R.id.item_grid_view);
                Player player = _game.getLocalPlayer();
                List<Item> items = player.getItems();
                ArrayAdapter adapter = (ArrayAdapter)gridView.getAdapter();
                adapter.clear();
                adapter.addAll(items);
            }
        }

        );

        ((MapView)findViewById(R.id.map_view)).init(_game);
        ((ActionPointView)findViewById(R.id.action_point_view)).init(_game);
        ((StatusView)findViewById(R.id.status_view)).init(_game);
        ((ItemGridView)findViewById(R.id.item_grid_view)).init(_game);
        ItemGridView gridView = (ItemGridView) findViewById(R.id.item_grid_view);
        List<Item> items = new ArrayList<Item>();
        ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(context, R.layout.item_view, items);
        gridView.setAdapter(adapter);

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

        _bluetoothManager.unregisterHandler(handler);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothLounge.MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE");
                    // TODO respond to connection problems
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
                        _bluetoothManager.writeToEveryone(readBuf, msg.getData().getString(BluetoothLounge.DEVICE_ADDRESS));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                    break;
            }
        }
    };
}
