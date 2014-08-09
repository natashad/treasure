package endee.fried.treasure.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import endee.fried.treasure.GameLogic.Game;
import endee.fried.treasure.GameLogic.Player;

/**
 * Created by natasha on 2014-08-05.
 */
public class GameView extends SurfaceView {
    private final static int DIAMETER = 100;

    private final static float START_MAP_WIDTH = 5 * DIAMETER;
    private final static float MAP_WIDTH = Game.getHexSize();
    private final static float MIN_MAP_WIDTH = 3 * DIAMETER;
    private final static float TILE_WIDTH = MAP_WIDTH * DIAMETER + 0.5f;
    private final static float TILE_HEIGHT = MAP_WIDTH * DIAMETER * 0.878f;

    private Game game;

    private final HashMap<Integer, TileButton> buttons = new HashMap<Integer,TileButton>();
    private final List<Button> activeButtons = new ArrayList<Button>();

    private final Button useItemButton;

    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector panDetector;

    private float mapScreenHeight;
    private float scale, minScale, maxScale;
    private float offsetX, offsetY;



    public GameView(final Context context, long seed) {
        super(context);
        game = new Game(1, 0, seed, context, new Callback() {
            @Override
            public void doAction() {
                Log.d("", "Invalidating in game callback");
                invalidate();
            }
        });

        scale = 1.0f;
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

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        panDetector = new GestureDetector(context, new PanListener());


        useItemButton = new Button(540, 1400, 300, new Callback() {
            @Override
            public void doAction() {
                Player player = game.getLocalPlayer();
                if(player.getNumItems() > 0 && player.canUseItem(0)) {
                    game.useItem(0);
                } else {
                    Toast.makeText(context, "You don't have an item", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

        useItemButton.draw(canvas, paint);
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
        } else {
            changed = useItemButton.update(event.getX(), event.getY(), motionEvent) || changed;
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
        offsetX = (TILE_WIDTH / 2f - START_MAP_WIDTH / 2f) + DIAMETER / 2f;
        offsetY = TILE_HEIGHT / 2f - (TILE_HEIGHT/TILE_WIDTH * START_MAP_WIDTH) / 2f;

        useItemButton.setCenterX(w / 2);
        useItemButton.setCenterY(mapScreenHeight + (h - mapScreenHeight) / 2);
        useItemButton.setRadius(w / 4);
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
