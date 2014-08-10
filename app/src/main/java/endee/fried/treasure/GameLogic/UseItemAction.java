package endee.fried.treasure.GameLogic;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by leslie on 08/08/14.
 */
public class UseItemAction extends Action {
    public final static String TYPE = "USE_ITEM";
    public final static String ITEM_KEY = "Item";

    /**
     * The index of the item in the players inventory
     */
    private int _itemIndex;

    public UseItemAction(int player, int itemIndex) {
        super(player);
        _itemIndex = itemIndex;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            json.put(Action.TYPE_KEY, TYPE);
            json.put(Action.PLAYER_KEY, _player);
            json.put(ITEM_KEY, _itemIndex);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    @Override
    public void doAction(Game game) {
        game.recycleItem(game.getPlayer(_player).useItem(_itemIndex));
    }
}
