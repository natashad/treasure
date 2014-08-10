package endee.fried.treasure.UI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by natasha on 2014-08-05.
 */
public class RectangleButton extends Button {

    private float _width;
    private float _height;

    private String _text;

    public RectangleButton(float centerX, float centerY, float width, float height, String text, Callback onClick) {
        super(centerX, centerY, onClick);

        _width = width;
        _height = height;
        _text = text;
    }

    public float getWidth() {
        return _width;
    }
    public float getHeight() {
        return _height;
    }

    public String getText() { return _text; }

    @Override
    protected boolean isInBounds(float x, float y) {
        return x > getX() - _width/2 && x < getX() + _width/2 &&
                y > getY() - _height/2 && y < getY() + _height/2;
    }

    @Override
    protected void drawButton(Canvas canvas, Paint paint, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawRect(getX() - _width/2, getY() - _height/2, getX() + _width/2, getY() + _height/2, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(Math.min(10f, _height / 20f));
        canvas.drawRect(getX() - _width/2, getY() - _height/2, getX() + _width/2, getY() + _height/2, paint);


        if(!_text.isEmpty()) {

            paint.setColor(Color.WHITE);
            paint.setTextSize(_height / 2);

            Rect rect = new Rect();
            paint.getTextBounds(_text, 0, _text.length(), rect);

            canvas.drawText(_text, getX() - rect.width() / 2, getY() + rect.height() / 2, paint);
        }
    }
}
