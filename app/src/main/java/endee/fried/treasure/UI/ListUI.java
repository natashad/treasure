package endee.fried.treasure.UI;

/**
 * Created by natasha on 2014-08-13.
 */

import android.graphics.Canvas;

import java.util.ArrayList;

/**
 * A class to represent the UI of a list.
 */
public class ListUI {

    private static final String TAG = ListUI.class.getName();

    private ArrayList<ListItemUI> _listItems = new ArrayList<ListItemUI>();
    private float _height;
    private float _width = 0;


    public ListUI() {
    }

    public void addListItem(ListItemUI listItem) {
        _listItems.add(listItem);
    }

    public void draw(Canvas canvas) {
        _height = 0;
        for (ListItemUI item : _listItems) {
            item.draw(canvas, _height);
            _height += item.getHeight();
            _width = Math.max(item.getWidth(), _width);
        }
    }

    public float getWidth() { return _width; }
    public float getHeight() { return _height; }


}
