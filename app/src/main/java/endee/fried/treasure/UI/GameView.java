package endee.fried.treasure.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
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
    private int RADIUS = 50;
//    keep a margin on the screen
    private int MAP_WIDTH = 15;

    private HexMap hexMap;
    private HashMap<Integer, TileButton> buttons = new HashMap<Integer,TileButton>();
    private List<Button> activeButtons = new ArrayList<Button>();

//    An integer array storing the x and y of currently active location
    private int currentlyActive;
    private float scale;

    public GameView(Context context) {
        super(context);
        scale = 1.0f;
        hexMap = new HexMap(MAP_WIDTH);
        hexMap.generate(new Random());
        final int[] allTiles = hexMap.getAllTiles();
        for (int i = 0; i < allTiles.length; i++) {
            final int index = i;
            int[] loc = hexMap.getLocation(allTiles[i]);
            buttons.put(allTiles[i], new TileButton(loc[0] * RADIUS,
                    (int) (loc[1] * RADIUS * 0.87f + RADIUS * 0.13f), RADIUS, new Callback() {
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

        int width = getWidth();

        float actualWidth = (MAP_WIDTH + 0.5f) * RADIUS*2;

        scale = width / actualWidth;

        Paint paint = new Paint();
        for(Button b : buttons.values()) {
            b.draw(canvas, paint, scale);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean changed = false;

        for(Button b: buttons.values()) {
            changed = b.update(event, scale) || changed;
        }

        if(changed) this.invalidate();
        return true;
    }
}
