package endee.fried.treasure.GameLogic;

/**
 * Created by leslie on 08/08/14.
 */
public class MoveAction extends Action {
    private int tile;

    public MoveAction(Player player, int tile) {
        super(player);
        this.tile = tile;
    }

    @Override
    public void doAction() {
        player.setTile(tile);
    }
}
