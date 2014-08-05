package endee.fried.treasure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceView;

/**
 * Created by natasha on 2014-08-05.
 */
public class MyView extends SurfaceView {

    private int radius = 200;
    private boolean circleTouched = false;

    public MyView(Context context) {
        super(context);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(circleTouched ? Color.GREEN : Color.RED);
        canvas.drawCircle(getWidth()/2, getHeight()/2, radius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        if (Math.pow((touchX - getWidth()/2), 2) + Math.pow((touchY - getHeight()/2), 2) < Math.pow(radius,2)) {
            System.out.println("CIRCLE");
            int eventAction = event.getAction();

            switch (eventAction) {
                case MotionEvent.ACTION_DOWN:
                    circleTouched = true;
                    break;

                case MotionEvent.ACTION_MOVE:
                    // finger moves on the screen
                    break;

                case MotionEvent.ACTION_UP:
                    circleTouched = false;
                    break;
            }
        }
        else {
            circleTouched = false;
            System.out.println(Math.pow((touchX - getWidth()/2), 2));
            System.out.println(Math.pow((touchY - getHeight()/2), 2));
            System.out.println(Math.pow(radius, 2));

        }
        this.invalidate();
        return true;
    }
}
