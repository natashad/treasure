package endee.fried.treasure.UI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * Created by natasha on 2014-08-05.
 */
public abstract class Button {

    private float _centerX;
    private float _centerY;

    protected boolean _active;
    protected boolean _clicked;

    private int _baseColor;
    private int _clickedColor;
    private int _inactiveColor;
    private int _borderColor;
    private int _textColor;

    protected final Callback _onClick;

    public Button(float centerX, float centerY, Callback onClick) {
        _centerX = centerX;
        _centerY = centerY;
        _onClick = onClick;
        _active = true;
        _clicked = false;

        _baseColor = Color.argb(255, 153, 204, 255);
        _inactiveColor = Color.DKGRAY;
        _clickedColor = Color.argb(255, 204, 229, 255);
        _borderColor = Color.BLACK;
        _textColor = Color.BLACK;

    }

    public void setActive(boolean _active) {
        this._active = _active;
    }

    public boolean isActive() { return _active; }

    public float getX() {
        return _centerX;
    }

    public float getY() {
        return _centerY;
    }

    public void setX( float centerX ) { _centerX = centerX; }

    public void setY( float centerY ) { _centerY = centerY; }

    public int getBaseColor() {
        return _baseColor;
    }

    public void setBaseColor(int baseColor) {
        _baseColor = baseColor;
    }

    public int getClickedColor() {
        return _clickedColor;
    }

    public void setClickedColor(int clickedColor) {
        _clickedColor = clickedColor;
    }

    public int getInactiveColor() {
        return _inactiveColor;
    }

    public void setInactiveColor(int inactiveColor) {
        _inactiveColor = inactiveColor;
    }

    public int getTextColor() {
        return _textColor;
    }

    public void setTextColor(int textColor) {
        _textColor = textColor;
    }

    public int getBorderColor() {
        return _borderColor;
    }

    public void setBorderColor(int borderColor) {
        _borderColor = borderColor;
    }

    /**
     * returns true if the Point at x,y is within bounds of the location.
     * @param x
     * @param y
     */
    protected abstract boolean isInBounds(float x, float y);

    protected abstract void drawButton(Canvas canvas, Paint paint);

    public void draw(Canvas canvas, Paint paint) {
        drawButton(canvas, paint);
    }

    protected int getCurrentBackgroundColor() {
        return _active ? (_clicked ? _clickedColor : _baseColor) : _inactiveColor;

    }

    public boolean update(float touchX, float touchY, int eventAction) {
        if(!_active) return false;

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                if (isInBounds(touchX, touchY)) {
                    _clicked = true;
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                if (_clicked && isInBounds(touchX, touchY)) {
                    _onClick.doAction(null);
                }

                _clicked = false;
                return true;
        }

        return false;
    }
}
