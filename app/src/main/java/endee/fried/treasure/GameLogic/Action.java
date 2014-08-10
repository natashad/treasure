package endee.fried.treasure.GameLogic;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by leslie on 08/08/14.
 */
public abstract class Action {
    public final static String TYPE_KEY = "Type";
    public final static String PLAYER_KEY = "Player";

    protected int _player;

    public Action(int player) {
        _player = player;
    }

    public static Action fromJSON(JSONObject json) {
        try {
            String type = json.getString(TYPE_KEY);
            int player = json.getInt(PLAYER_KEY);

            if(MoveAction.TYPE.equals(type)) {
                int tile = json.getInt(MoveAction.TILE_KEY);
                return new MoveAction(player, tile);
            } else if(UseItemAction.TYPE.equals(type)) {
                int itemIndex = json.getInt(UseItemAction.ITEM_KEY);
                return new UseItemAction(player, itemIndex);
            } else {
                throw new RuntimeException("Unknown Action Type: " + type);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public abstract JSONObject toJSON();

    public abstract void doAction(Game game);
}
