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

    private boolean _active;
    protected boolean _clicked;

    protected final Callback _onClick;

    public Button(float centerX, float centerY, Callback onClick) {
        _centerX = centerX;
        _centerY = centerY;
        _onClick = onClick;
        _active = true;
        _clicked = false;
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

    /**
     * returns true if the Point at x,y is within bounds of the location.
     * @param x
     * @param y
     */
    protected abstract boolean isInBounds(float x, float y);

    protected abstract void drawButton(Canvas canvas, Paint paint, int color);

    public void draw(Canvas canvas, Paint paint) {
        drawButton(canvas, paint, _active ? (_clicked ? Color.GREEN : Color.RED ): Color.GRAY);
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
