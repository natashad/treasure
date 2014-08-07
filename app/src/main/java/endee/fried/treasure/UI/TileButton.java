package endee.fried.treasure.UI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by natasha on 2014-08-05.
 */
public class TileButton extends Button {

    boolean hasPlayer;

    public TileButton(float centerX, float centerY, float radius, Callback callback) {
        super(centerX, centerY, radius, callback);
        hasPlayer = false;
    }

    public void setHasPlayer(boolean hasPlayer) { this.hasPlayer = hasPlayer; }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if(hasPlayer) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.YELLOW);
            canvas.drawCircle(getX(), getY(), getRadius(), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(Math.min(10f, getRadius() / 20f));
            canvas.drawCircle(getX(), getY(), getRadius(), paint);
        } else {
            super.draw(canvas, paint);
        }
    }
}
