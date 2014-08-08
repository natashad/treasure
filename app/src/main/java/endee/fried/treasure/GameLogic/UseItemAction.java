package endee.fried.treasure.GameLogic;

/**
 * Created by leslie on 08/08/14.
 */
public class UseItemAction extends Action {
    private int itemIndex;

    public UseItemAction(Player player, Game game, int itemIndex) {
        super(player, game);
        this.itemIndex = itemIndex;
    }

    @Override
    public void doAction() {
        game.recycleItem(player.useItem(itemIndex));
    }
}
