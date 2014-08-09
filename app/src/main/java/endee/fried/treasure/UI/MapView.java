package endee.fried.treasure.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;

import java.util.HashMap;

import endee.fried.treasure.GameLogic.Game;

/**
 * Created by natasha on 2014-08-05.
 */
public class MapView extends SurfaceView {
    private final static int DIAMETER = 100;

    private final static float START_MAP_WIDTH = 5 * DIAMETER;
    private final static float MAP_WIDTH = Game.getHexSize();
    private final static float MIN_MAP_WIDTH = 3 * DIAMETER;
    private final static float TILE_WIDTH = MAP_WIDTH * DIAMETER + DIAMETER / 2f;
    private final static float TILE_HEIGHT = MAP_WIDTH * DIAMETER * 0.878f;

    private Game game;

    private final HashMap<Integer, TileButton> buttons;

    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector panDetector;

    private float mapScreenHeight;
    private float scale, minScale, maxScale;
    private float offsetX, offsetY;

    private final int screenPixelWidth;
    private final int screenPixelHeight;

    public MapView(final Context context) {
        super(context);

        buttons = new HashMap<Integer,TileButton>();
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        panDetector = new GestureDetector(context, new PanListener());

        screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenPixelHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        buttons = new HashMap<Integer,TileButton>();
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        panDetector = new GestureDetector(context, new PanListener());

        screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenPixelHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        buttons = new HashMap<Integer,TileButton>();
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        panDetector = new GestureDetector(context, new PanListener());

        screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenPixelHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    public void init(Game _game) {
        game = _game;

        final int[] allTiles = game.getHexMap().getAllTiles();

        for (int i = 0; i < allTiles.length; i++) {
            final int index = i;
            float[] loc = game.getHexMap().getLocation(allTiles[i]);
            buttons.put(allTiles[i], new TileButton(loc[0] * DIAMETER,
                    loc[1] * DIAMETER * 0.87f + DIAMETER * 0.065f, DIAMETER/2, new Callback() {
                @Override
                public void doAction() {
                    game.movePlayer(allTiles[index]);
                }
            }, game, allTiles[i]));
        }

        calculateScaleAndAspectRatio();
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
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(screenPixelWidth, (int)mapScreenHeight);
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

    private void calculateScaleAndAspectRatio() {
        minScale = screenPixelWidth / TILE_WIDTH;
        maxScale = screenPixelWidth / MIN_MAP_WIDTH;

        scale = screenPixelWidth / START_MAP_WIDTH;

        mapScreenHeight = screenPixelWidth * TILE_HEIGHT/TILE_WIDTH;

        // Position view to the centre of the map
        offsetX = (TILE_WIDTH / 2f - START_MAP_WIDTH / 2f) + DIAMETER / 2f;
        offsetY = TILE_HEIGHT / 2f - (TILE_HEIGHT/TILE_WIDTH * START_MAP_WIDTH) / 2f;
    }

    private void boundPanAndZoom() {
        float currentTileWidth = getWidth() / scale;
        float currentTileHeight = mapScreenHeight / scale;

        offsetX = Math.max(0, Math.min(offsetX, TILE_WIDTH - currentTileWidth));
        offsetY = Math.max(0, Math.min(offsetY, TILE_HEIGHT - currentTileHeight));

        scale = Math.max(minScale, Math.min(scale, maxScale));
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
}
