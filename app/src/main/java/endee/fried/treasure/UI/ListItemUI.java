package endee.fried.treasure.UI;

import android.graphics.Canvas;

/**
 * Created by natasha on 2014-08-13.
 */
public interface ListItemUI {

    public void draw(Canvas canvas, float y);
    public float getHeight();
    public float getWidth();
}
