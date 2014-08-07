package endee.fried.treasure.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import endee.fried.treasure.HexMap;

/**
 * Created by natasha on 2014-08-05.
 */
public class GameView extends SurfaceView {

//    radius of the location tiles
    private final static int START_MAP_WIDTH = 5;
    private final static int MAP_WIDTH = 15;
    private final static int MIN_MAP_WIDTH = 3;
    private final static float TILE_WIDTH =(MAP_WIDTH + 0.5f);
    private final static float TILE_HEIGHT = (MAP_WIDTH) * 0.88f;


    private HexMap hexMap;
    private HashMap<Integer, TileButton> buttons = new HashMap<Integer,TileButton>();
    private List<Button> activeButtons = new ArrayList<Button>();

//    An integer array storing the x and y of currently active location
    private int currentlyActive;
    private float mapScreenHeight;
    private float scale, minScale, maxScale;
    private float offsetX, offsetY;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector panDetector;

    public GameView(Context context) {
        super(context);
        scale = 1.0f;
        hexMap = new HexMap(MAP_WIDTH);
        hexMap.generate(new Random());
        final int[] allTiles = hexMap.getAllTiles();
        for (int i = 0; i < allTiles.length; i++) {
            final int index = i;
            float[] loc = hexMap.getLocation(allTiles[i]);
            buttons.put(allTiles[i], new TileButton(loc[0],
                    loc[1] * 0.87f + 0.13f, 0.5f, new Callback() {
                @Override
                public void doAction() {
                    buttons.get(currentlyActive).setHasPlayer(false);
                    buttons.get(allTiles[index]).setHasPlayer(true);

                    currentlyActive = allTiles[index];

                    for (Button b : activeButtons) {
                        b.setActive(false);
                    }

                    activeButtons.clear();

                    List<Integer> neighbours = hexMap.getNeighbours(currentlyActive);

                    for (int i : neighbours) {
                        buttons.get(i).setActive(true);
                        activeButtons.add(buttons.get(i));
                    }

                }
            }));

            buttons.get(allTiles[i]).setActive(false);
        }

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        panDetector = new GestureDetector(context, new PanListener());

        currentlyActive = hexMap.getStartTile();
        buttons.get(currentlyActive).setHasPlayer(true);
        List<Integer> neighbours = hexMap.getNeighbours(currentlyActive);

        for (int i : neighbours) {
            buttons.get(i).setActive(true);
            activeButtons.add(buttons.get(i));
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        canvas.scale(scale, scale);
        canvas.translate(-offsetX, -offsetY);

        Paint paint = new Paint();
        for(Button b : buttons.values()) {
            b.draw(canvas, paint);
        }

        canvas.restore();

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, mapScreenHeight, getWidth(), getHeight(), paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean changed = false;

        scaleDetector.onTouchEvent(event);
        panDetector.onTouchEvent(event);

        int motionEvent = event.getAction();

        if(event.getY() <= mapScreenHeight) {
            for (Button b : buttons.values()) {
                changed = b.update(event.getX() / scale + offsetX, event.getY() / scale + offsetY, motionEvent) || changed;
            }
        }

        if(changed) this.invalidate();
        return true;
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        minScale = w / TILE_WIDTH;
        maxScale = w / MIN_MAP_WIDTH;

        scale = w / START_MAP_WIDTH;

        mapScreenHeight = w * TILE_HEIGHT/TILE_WIDTH;

        // Position view to the centre of the map
        offsetX = (TILE_WIDTH + 0.5f) / 2f - START_MAP_WIDTH / 2f;
        offsetY = TILE_HEIGHT / 2f - (TILE_HEIGHT/TILE_WIDTH * START_MAP_WIDTH) / 2f;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();

            float newScreenFocusX = detector.getFocusX() * detector.getScaleFactor();
            float newScreenFocusY = detector.getFocusY() * detector.getScaleFactor();

            offsetX += (newScreenFocusX - detector.getFocusX()) / scale;
            offsetY += (newScreenFocusY - detector.getFocusY()) / scale;

            boundPanAndZoom();

            invalidate();
            return true;
        }
    }

    private class PanListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            offsetX += distanceX / scale;
            offsetY += distanceY / scale;

            boundPanAndZoom();

            invalidate();
            return true;
        }
    }

    private void boundPanAndZoom() {
        float currentTileWidth = getWidth() / scale;
        float currentTileHeight = mapScreenHeight / scale;

        offsetX = Math.max(0, Math.min(offsetX, TILE_WIDTH - currentTileWidth));
        offsetY = Math.max(0, Math.min(offsetY, TILE_HEIGHT - currentTileHeight));

        scale = Math.max(minScale, Math.min(scale, maxScale));
    }
}
