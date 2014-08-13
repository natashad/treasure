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
    private int  _textColor = Color.WHITE;
    private int _borderColor = Color.BLACK;

    public RectangleButton(float centerX, float centerY, float width, float height, String text, Callback onClick) {
        super(centerX, centerY, onClick);

        _width = width;
        _height = height;
        _text = text;
    }

    public void setTextColor(int color) { _textColor = color; }

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
    protected void drawButton(Canvas canvas, Paint paint, int activeColorClicked, int activeColorUnclicked, int inactiveColor, int textColor) {

        int backgroundColor = _active ? (_clicked ? activeColorClicked : activeColorUnclicked) : inactiveColor;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(backgroundColor);
        canvas.drawRect(getX() - _width/2, getY() - _height/2, getX() + _width/2, getY() + _height/2, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(_borderColor);
        paint.setStrokeWidth(Math.min(10f, _height / 20f));
        canvas.drawRect(getX() - _width/2, getY() - _height/2, getX() + _width/2, getY() + _height/2, paint);


        if(!_text.isEmpty()) {

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(_textColor);
            paint.setTextSize(_height / 2);

            Rect rect = new Rect();
            paint.getTextBounds(_text, 0, _text.length(), rect);

            canvas.drawText(_text, getX() - rect.width() / 2, getY() + rect.height() / 2, paint);
        }
    }
}
