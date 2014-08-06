package endee.fried.treasure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by natasha on 2014-08-05.
 */
public class MyView extends SurfaceView {

    private int radius = 30;
    private HexMap hexMap;
    private HashMap<Integer, MapLocation> locations = new HashMap<Integer,MapLocation>();
//    An integer array storing the x and y of currently active location
    private int currentlyActive;

    public MyView(Context context) {
        super(context);
        hexMap = new HexMap(11);
        hexMap.generate(new Random());
        int[] allTiles = hexMap.getAllTiles();
        for (int i = 0; i < allTiles.length; i++) {
            int[] loc = hexMap.getLocation(allTiles[i]);
            locations.put(allTiles[i], new MapLocation(loc[0]*radius, (int)(loc[1]*radius*0.85f), radius));
        }
        currentlyActive = hexMap.getStartTile();
        locations.get(currentlyActive).setActive(true);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        Iterator it = locations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            MapLocation location = (MapLocation)pairs.getValue();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(location.isActive() ? Color.GREEN : Color.RED);
            canvas.drawCircle(location.getX(), location.getY(), radius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            paint.setColor(Color.BLACK);
            canvas.drawCircle(location.getX(), location.getY(), radius, paint);
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

                List<Integer> neighbours = hexMap.getNeighbours(currentlyActive);

                for (int i = 0; i < neighbours.size(); i++) {
                    MapLocation location = locations.get(neighbours.get(i));
                    if (location.isInBounds(touchX, touchY)) {
                        locations.get(currentlyActive).setActive(false);
                        currentlyActive = neighbours.get(i);
                        location.setActive(true);
                        break;
                    }
                }
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
