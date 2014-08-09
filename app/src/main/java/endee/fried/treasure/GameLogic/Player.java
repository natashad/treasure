package endee.fried.treasure.GameLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leslie on 05/08/14.
 */
public class Player {
    private final boolean local;
    private final List<Item> items;

    private int tile;
    private boolean hasKey;

    private int actionPoints;

    public  Player(boolean local, int startTile) {
        this.local = local;
        tile = startTile;
        items = new ArrayList<Item>();

        actionPoints = 0;
        hasKey = false;
    }

    public void setTile(int tile) {
        this.tile = tile;
    }

    public int getTile() {
        return tile;
    }

    public boolean isLocal() {
        return local;
    }

    public void giveActionPoint() {
        actionPoints = Math.min(Game.MAX_ACTION_POINTS, actionPoints + 1);
    }

    public int getActionPoints() { return actionPoints; }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    public boolean hasKey() {
        return hasKey;
    }

    public void giveItem(Item item) {
        items.add(item);
    }

    public Item useItem(int index) {
        Item item = items.get(index);

        if(actionPoints >= item.getCost()) {
            items.remove(index);
            actionPoints -= item.getCost();
            item.activateItem();
            return item;
        }

        assert(false) : "Cant use item, not enough action points!";

        return null;
    }

    public boolean canUseItem(int index) {
        return actionPoints >= items.get(index).getCost();
    }

    public Item getItem(int index) {
        return items.get(index);
    }

    public int getNumItems() {
        return items.size();
    }

}
