package endee.fried.treasure.UI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by natasha on 2014-08-05.
 */
public class CircleButton extends Button {

    private float _radius;

    public CircleButton(float centerX, float centerY, float radius, Callback onClick) {
        super(centerX, centerY, onClick);

        _radius = radius;
    }

    public float getRadius() {
        return _radius;
    }

    @Override
    protected boolean isInBounds(float x, float y) {
        return Math.pow((x - getX()), 2) + Math.pow((y - getY()), 2) < Math.pow(_radius,2);
    }

    @Override
    protected void drawButton(Canvas canvas, Paint paint, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(getX(), getY(), _radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(Math.min(10f, _radius / 20f));
        canvas.drawCircle(getX(), getY(), _radius, paint);
    }
}
