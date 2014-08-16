package endee.fried.treasure.UI;

import java.util.ArrayList;
import java.util.List;

import android.widget.GridView;
import android.widget.ArrayAdapter;
import android.content.Context;

import endee.fried.treasure.GameLogic.Game;
import endee.fried.treasure.GameLogic.Player;
import endee.fried.treasure.GameLogic.Item;


/**
 * Created by jeff on 2014-08-16.
 */
public class ItemGridView extends GridView{
    private Game _game;

    public ItemGridView(Context context) {
        super(context);
    }

    public void init(Game game, Context context) {
        _game = game;
        Player player = game.getLocalPlayer();
        List<Item> items = player.getItems();

        ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(context, 0, items);
        setAdapter(adapter);
    }


}
