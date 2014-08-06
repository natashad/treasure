package endee.fried.treasure.UI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * Created by natasha on 2014-08-05.
 */
public class Button {

    private final int centerX;
    private final int centerY;
    private final int radius;
    private boolean isActive;
    private boolean isClicked;
    private boolean isOver;
    private Callback callback;


    public Button(int centerX, int centerY, int radius, Callback callback) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.callback = callback;
        this.isActive = true;
        this.isClicked = false;
        this.isOver = false;

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

    public int getX() {
        return centerX;
    }

    public int getY() {
        return centerY;
    }

    public int getRadius() {
        return radius;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(isActive ? ((isClicked && isOver)? Color.GREEN : Color.RED ): Color.GRAY);
        canvas.drawCircle(getX(), getY(), radius, paint);
    }

    public boolean update(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (isInBounds(touchX, touchY)) {
                    isOver = true;
                    isClicked = true;
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isInBounds(touchX, touchY)) {
                    isOver = true;
                } else {
                    isOver = false;
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (isClicked && isInBounds(touchX, touchY)) {
                    isOver = true;
                    callback.doAction();
                }

                isClicked = false;
                return true;
        }

        return false;
    }
}