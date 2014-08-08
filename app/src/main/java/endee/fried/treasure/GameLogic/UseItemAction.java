package endee.fried.treasure.GameLogic;

/**
 * Created by leslie on 08/08/14.
 */
public class UseItemAction extends Action {
    private int itemIndex;
    private Game game;

    public UseItemAction(Player player, Game game, int itemIndex) {
        super(player);
        this.itemIndex = itemIndex;
        this.game = game;
    }

    @Override
    public void doAction() {
        game.recycleItem(player.useItem(itemIndex));
    }
}
