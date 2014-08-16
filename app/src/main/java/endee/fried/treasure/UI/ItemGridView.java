package endee.fried.treasure.UI;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.util.Log;
import android.widget.GridView;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;

import endee.fried.treasure.GameLogic.Game;
import endee.fried.treasure.GameLogic.Player;
import endee.fried.treasure.GameLogic.Item;


/**
 * Created by jeff on 2014-08-16.
 */
public class ItemGridView extends GridView{
    private Game _game;

    public ItemGridView(final Context context) {
        super(context);
    }

    public ItemGridView(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ItemGridView(final Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void init(Game game) {
        _game = game;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("ItemGridView", "onDraw");
    }


}
