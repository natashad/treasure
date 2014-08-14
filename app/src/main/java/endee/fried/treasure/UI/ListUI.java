package endee.fried.treasure.UI;

/**
 * Created by natasha on 2014-08-13.
 */

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * A class to represent the UI of a list.
 */
public class ListUI {

    private static final String TAG = ListUI.class.getName();

    private final ArrayList<ListItemUI> _listItems;
    private float _height;
    private float _width;


    public ListUI() {
        _listItems = new ArrayList<ListItemUI>();

        _height = 0;
        _width = 0;
    }

    public void addListItem(ListItemUI listItem) {
        _listItems.add(listItem);

        _height += listItem.getHeight();
        _width = Math.max(listItem.getWidth(), _width);
    }

    public void draw(Canvas canvas, Paint paint) {
        int currentHeight = 0;

        canvas.save();

        for (ListItemUI item : _listItems) {
            item.drawListItem(canvas, paint);
            currentHeight += item.getHeight();
            canvas.translate(0, item.getHeight());
        }

        canvas.restore();
    }

    public float getWidth() { return _width; }
    public float getHeight() { return _height; }


}
