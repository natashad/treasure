package endee.fried.treasure.GameLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leslie on 05/08/14.
 */
public class Tile {
    private boolean discovered;

    private final List<Trap> traps;

    // Can be null when no item present;
    private Item item;
    private Item lastKnownItem;

    public Tile() {
        discovered = false;

        traps = new ArrayList<Trap>();

        item = null;
        lastKnownItem = null;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public Item getLastKnownItem() {
        return lastKnownItem;
    }

    public void discover() {
        discovered = true;
        lastKnownItem = item;
    }

    public boolean isDiscovered() {
        return discovered;
    }
}
