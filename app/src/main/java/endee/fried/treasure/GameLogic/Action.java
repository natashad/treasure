package endee.fried.treasure.GameLogic;

/**
 * Created by leslie on 08/08/14.
 */
public abstract class Action {
    protected Player player;

    public Action(Player player) {
        this.player = player;
    }

    public abstract void doAction();
}
