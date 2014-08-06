package endee.fried.treasure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by natasha on 2014-08-05.
 */
public class MyView extends SurfaceView {

    private int radius = 70;
    private MapLocation[][] locations = new MapLocation[5][5];
//    An integer array storing the x and y of currently active location
    private int[] currentlyActive = new int[2];

    public MyView(Context context) {
        super(context);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                locations[i][j] = new MapLocation((i*radius*2)+radius, (j*radius*2)+radius, radius);
            }
        }
        currentlyActive[0] = 0;
        currentlyActive[1] = 0;
        locations[currentlyActive[0]][currentlyActive[1]].setActive(true);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        for (int i = 0; i < locations.length; i++) {
            for (int j = 0; j < locations[i].length; j++) {
                paint.setColor(locations[i][j].isActive() ? Color.GREEN : Color.RED);
                canvas.drawCircle(locations[i][j].getX(), locations[i][j].getY(), radius, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        boolean validTouch = false;

        int eventAction = event.getAction();

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:

                ArrayList<int[]> neighbours = new ArrayList<int[]>();
                neighbours.add(new int[]{currentlyActive[0] + 1, currentlyActive[1]});
                neighbours.add(new int[]{currentlyActive[0] - 1, currentlyActive[1]});
                neighbours.add(new int[]{currentlyActive[0], currentlyActive[1] + 1});
                neighbours.add(new int[]{currentlyActive[0], currentlyActive[1] - 1});

                for (int i = 0; i < neighbours.size(); i++) {
                    int x = neighbours.get(i)[0];
                    int y = neighbours.get(i)[1];
                    if (x >= 0 && x < locations.length && y >= 0 && y < locations.length) {
                        if (locations[x][y].isInBounds(touchX, touchY)) {
                            locations[currentlyActive[0]][currentlyActive[1]].setActive(false);
                            currentlyActive[0] = x;
                            currentlyActive[1] = y;
                            break;
                        }
                    }
                }
                locations[currentlyActive[0]][currentlyActive[1]].setActive(true);
                break;

            case MotionEvent.ACTION_MOVE:
                // finger moves on the screen
                break;

            case MotionEvent.ACTION_UP:
                break;
        }
        this.invalidate();
        return true;
    }
}
