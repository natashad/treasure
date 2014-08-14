package endee.fried.treasure.UI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by natasha on 2014-08-13.
 */
public class ConnectedDeviceListItem implements ListItemUI {


    private static final String TAG = ConnectedDeviceListItem.class.getName();
    private static final int BACKGROUND_COLOR = Color.WHITE;
    private static final int BORDER_COLOR = Color.BLACK;
    private static final int TEXT_COLOR = Color.BLACK;
    private static final float STROKE_WIDTH = 3f;

    private float _height;
    private float _width;
    private float _x = 0;
    private float _y;
    private Paint _paint;
    private String _text;


    public ConnectedDeviceListItem(String text, float height, float width) {
        _height = height;
        _width = width;
        _paint = new Paint();
        _text = text;
    }

    @Override
    public void draw(Canvas canvas, float y) {
        _y = y;
        _paint.setStyle(Paint.Style.FILL);
        _paint.setColor(BACKGROUND_COLOR);
        canvas.drawRect(_x, y, _width, _height + y, _paint);

        _paint.setStyle(Paint.Style.STROKE);
        _paint.setColor(BORDER_COLOR);
        _paint.setStrokeWidth(STROKE_WIDTH);
        canvas.drawRect(_x, y, _width, _height + y, _paint);

        _paint.setStyle(Paint.Style.FILL);
        _paint.setColor(TEXT_COLOR);
        _paint.setTextSize(_height / 2);

        Rect rect = new Rect();
        _paint.getTextBounds(_text, 0, _text.length(), rect);

        // To center text
//        canvas.drawText(_text, (_width - rect.width()) / 2, y + _height/2 + rect.height() / 2, _paint);
        // Left justified text.
        canvas.drawText(_text, _width*0.02f, y + _height/2 + rect.height() / 2, _paint);


    }

    @Override
    public float getHeight() {
        return _height;
    }

    @Override
    public float getWidth() {
        return _width;
    }

}
