package endee.fried.treasure.GameLogic;

/**
 * Created by leslie on 08/08/14.
 */
public abstract class Action {
    protected Player player;
    protected Game game;

    public Action(Player player, Game game) {
        this.player = player;
        this.game = game;
    }

    public abstract void doAction();
}
