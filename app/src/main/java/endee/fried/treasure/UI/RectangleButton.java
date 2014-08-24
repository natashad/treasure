package endee.fried.treasure.UI;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by natasha on 2014-08-05.
 */
public class RectangleButton extends Button {

    private float _width;
    private float _height;

    private String _text;

    private RectF _rect;

    public RectangleButton(float centerX, float centerY, float width, float height, String text, Callback onClick) {
        super(centerX, centerY, onClick);

        _width = width;
        _height = height;
        _text = text;

        _rect = new RectF(getX() - _width/2, getY() - _height/2, getX() + _width/2, getY() + _height/2);
    }

    @Override
    public void setX(float centerX) {
        _rect.offset(centerX - getX(), 0);
        super.setX(centerX);
    }

    @Override
    public void setY(float centerY) {
        _rect.offset(0, centerY - getY());
        super.setY(centerY);
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
        return _rect.contains(x, y);
    }

    @Override
    protected void drawButton(Canvas canvas, Paint paint) {

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getCurrentBackgroundColor());
        canvas.drawRect(_rect, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getBorderColor());
        paint.setStrokeWidth(Math.min(10f, _height / 20f));
        canvas.drawRect(_rect, paint);


        if(!_text.isEmpty()) {

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getTextColor());
            paint.setTextSize(_height / 2);

            Rect textRect = new Rect();
            paint.getTextBounds(_text, 0, _text.length(), textRect);

            canvas.drawText(_text, getX() - textRect.width() / 2, getY() + textRect.height() / 2, paint);
        }
    }


}
