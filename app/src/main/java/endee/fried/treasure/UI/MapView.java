package endee.fried.treasure.UI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;

import java.util.HashMap;

import endee.fried.treasure.GameLogic.Game;
import endee.fried.treasure.R;

/**
 * Created by natasha on 2014-08-05.
 */
public class MapView extends SurfaceView {
    private final static int DIAMETER = 100;

    private final static float START_MAP_WIDTH = 5 * DIAMETER;
    private final static float MAP_WIDTH = Game.getHexSize();
    private final static float MIN_MAP_WIDTH = 3 * DIAMETER;
    private final static float TILE_WIDTH = MAP_WIDTH * DIAMETER + DIAMETER / 2f;
    private final static float TILE_HEIGHT = MAP_WIDTH * DIAMETER * 0.886f;

    private Game _game;

    private final HashMap<Integer, TileButton> _buttons;

    private final ScaleGestureDetector _scaleDetector;
    private final GestureDetector _panDetector;

    private final Bitmap _hexBmp;

    private final int _screenPixelWidth;

    private float _mapScreenHeight;
    private float _scale, _minScale, _maxScale;
    private float _offsetX, _offsetY;



    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        _buttons = new HashMap<Integer,TileButton>();
        _scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        _panDetector = new GestureDetector(context, new PanListener());

        _screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;

        _hexBmp = BitmapFactory.decodeResource(getResources(), R.drawable.base_hex);
    }

    public void init(Game game) {
        _game = game;

        final int[] allTiles = _game.getHexMap().getAllTiles();

        for (int i = 0; i < allTiles.length; i++) {
            final int index = i;
            float[] loc = _game.getHexMap().getLocation(allTiles[i]);
            _buttons.put(allTiles[i], new TileButton(loc[0] * DIAMETER,
                    loc[1] * DIAMETER * 0.87f + DIAMETER * 0.11f, DIAMETER / 2, new Callback() {
                @Override
                public void doAction(Object obj) {
                    _game.movePlayer(allTiles[index]);
                }
            }, _game, allTiles[i], _hexBmp
            ));
        }

        calculateScaleAndAspectRatio();
    }

    public void centerOnTile(int tile) {
        float[] loc = _game.getHexMap().getLocation(tile);

        float x = loc[0] * DIAMETER;
        float y = loc[1] * DIAMETER * 0.87f + DIAMETER * 0.11f;

        float tileMapWidth = _screenPixelWidth / _scale;
        float tileMapHeight = _mapScreenHeight / _scale;

        _offsetX = x - tileMapWidth / 2;
        _offsetY = y - tileMapHeight / 2;

        boundPanAndZoom();
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        canvas.scale(_scale, _scale);
        canvas.translate(-_offsetX, -_offsetY);

        Paint paint = new Paint();
        for(Button b : _buttons.values()) {
            b.draw(canvas, paint);
        }

        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(_screenPixelWidth, (int) _mapScreenHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean changed = false;

        _scaleDetector.onTouchEvent(event);
        _panDetector.onTouchEvent(event);

        int motionEvent = event.getAction();

        if(event.getY() <= _mapScreenHeight) {
            for (Button b : _buttons.values()) {
                changed = b.update(event.getX() / _scale + _offsetX, event.getY() / _scale + _offsetY, motionEvent) || changed;
            }
        }

        if(changed) invalidate();
        return true;
    }

    private void calculateScaleAndAspectRatio() {
        _minScale = _screenPixelWidth / TILE_WIDTH;
        _maxScale = _screenPixelWidth / MIN_MAP_WIDTH;

        _scale = _screenPixelWidth / START_MAP_WIDTH;

        _mapScreenHeight = _screenPixelWidth * TILE_HEIGHT/TILE_WIDTH;

        // Position view to the centre of the map
        _offsetX = (TILE_WIDTH / 2f - START_MAP_WIDTH / 2f) + DIAMETER / 2f;
        _offsetY = TILE_HEIGHT / 2f - (TILE_HEIGHT/TILE_WIDTH * START_MAP_WIDTH) / 2f;
    }

    private void boundPanAndZoom() {
        float currentTileWidth = getWidth() / _scale;
        float currentTileHeight = _mapScreenHeight / _scale;

        _offsetX = Math.max(0, Math.min(_offsetX, TILE_WIDTH - currentTileWidth));
        _offsetY = Math.max(0, Math.min(_offsetY, TILE_HEIGHT - currentTileHeight));

        _scale = Math.max(_minScale, Math.min(_scale, _maxScale));
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            _scale *= detector.getScaleFactor();

            float newScreenFocusX = detector.getFocusX() * detector.getScaleFactor();
            float newScreenFocusY = detector.getFocusY() * detector.getScaleFactor();

            _offsetX += (newScreenFocusX - detector.getFocusX()) / _scale;
            _offsetY += (newScreenFocusY - detector.getFocusY()) / _scale;

            boundPanAndZoom();

            invalidate();
            return true;
        }
    }

    private class PanListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            _offsetX += distanceX / _scale;
            _offsetY += distanceY / _scale;

            boundPanAndZoom();

            invalidate();
            return true;
        }
    }
}
