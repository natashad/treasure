package endee.fried.treasure.GameLogic;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by leslie on 08/08/14.
 */
public class MoveAction extends Action {
    public final static String TYPE = "MOVE";
    public final static String TILE_KEY = "Tile";

    private int _tile;

    public MoveAction(int player, int tile) {
        super(player);
        _tile = tile;
    }

    public int getTile() { return _tile;}

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            json.put(Action.TYPE_KEY, TYPE);
            json.put(Action.PLAYER_KEY, _player);
            json.put(TILE_KEY, _tile);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    @Override
    public void doAction(Game game) {
        game.getPlayer(_player).setTile(_tile);
    }
}
