package endee.fried.treasure.GameLogic;

/**
 * Created by leslie on 08/08/14.
 */
public class MoveAction extends Action {
    private int tile;

    public MoveAction(Player player, Game game, int tile) {
        super(player, game);
        this.tile = tile;
    }

    @Override
    public void doAction() {
        player.setTile(tile);
    }
}