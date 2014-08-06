package endee.fried.treasure.UI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by natasha on 2014-08-05.
 */
public class TileButton extends Button {

    boolean hasPlayer;

    public TileButton(int centerX, int centerY, int radius, Callback callback) {
        super(centerX, centerY, radius, callback);
        hasPlayer = false;
    }

    public void setHasPlayer(boolean hasPlayer) { this.hasPlayer = hasPlayer; }

    @Override
    public void draw(Canvas canvas, Paint paint, float scale) {
        if(hasPlayer) {
            int scaledRadius = (int)(getRadius() * scale);
            int scaledX = (int)(getX() * scale);
            int scaledY = (int)(getY() * scale);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.YELLOW);
            canvas.drawCircle(scaledX, scaledY, scaledRadius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(Math.min(10f, scaledRadius / 20f));
            canvas.drawCircle(scaledX, scaledY, scaledRadius, paint);
        } else {
            super.draw(canvas, paint, scale);
        }
    }
}
