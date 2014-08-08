package endee.fried.treasure.UI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * Created by natasha on 2014-08-05.
 */
public class Button {

    private float centerX;
    private float centerY;
    private float radius;
    protected final Callback callback;
    private boolean isActive;
    protected boolean isClicked;


    public Button(float centerX, float centerY, float radius, Callback callback) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.callback = callback;
        this.isActive = true;
        this.isClicked = false;
    }

    /**
     * returns true if the Point at x,y is within bounds of the location.
     * @param x
     * @param y
     */
    private boolean isInBounds(float x, float y) {
        return Math.pow((x - centerX), 2) + Math.pow((y - centerY), 2) < Math.pow(radius,2);
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }

    public float getX() {
        return centerX;
    }

    public float getY() {
        return centerY;
    }

    public float getRadius() {
        return radius;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void draw(Canvas canvas, Paint paint) {
        drawButton(canvas, paint, radius, isActive ? (isClicked? Color.GREEN : Color.RED ): Color.GRAY);
    }

    public boolean update(float touchX, float touchY, int eventAction) {
        if(!isActive) return false;

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:

                if (isInBounds(touchX, touchY)) {
                    isClicked = true;
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                if (isClicked && isInBounds(touchX, touchY)) {
                    callback.doAction();
                }

                isClicked = false;
                return true;
        }

        return false;
    }

    protected void drawButton(Canvas canvas, Paint paint, float radius, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(getX(), getY(), radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(Math.min(10f, radius / 20f));
        canvas.drawCircle(getX(), getY(), radius, paint);
    }
}
