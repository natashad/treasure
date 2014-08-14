package endee.fried.treasure.UI;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by natasha on 2014-08-13.
 */
public interface ListItemUI {

    public void drawListItem(Canvas canvas, Paint paint);
    public float getHeight();
    public float getWidth();
}
