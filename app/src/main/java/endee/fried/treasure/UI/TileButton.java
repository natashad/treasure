package endee.fried.treasure.UI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

import endee.fried.treasure.GameLogic.Game;

/**
 * Created by natasha on 2014-08-05.
 */
public class TileButton extends Button {

    private Game game;
    private int tileID;

    public TileButton(float centerX, float centerY, float radius, Callback callback, Game game, int tileID) {
        super(centerX, centerY, radius, callback);
        this.game = game;
        this.tileID = tileID;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if(game.getLocalPlayer().getTile() == tileID) {
            drawButton(canvas, paint, getRadius(), Color.YELLOW);
        } else if(isNeighbour()) {
            super.draw(canvas, paint);
        } else {
            if(game.getTile(tileID).isDiscovered()) {
                drawButton(canvas, paint, getRadius(), Color.GRAY);
            } else {
                drawButton(canvas, paint, getRadius(), Color.BLACK);
            }
        }


        if(game.getTile(tileID).getLastKnownItem() != null) {
            drawButton(canvas, paint, getRadius()/2, Color.CYAN);
        }
        if(game.getTile(tileID).getItem() != null) {
            drawButton(canvas, paint, getRadius()/4, Color.RED);
        } else if(game.getKeyTile() == tileID) {
            drawButton(canvas, paint, getRadius()/4, Color.YELLOW);
        } else if(game.getTreasureTile() == tileID) {
            drawButton(canvas, paint, getRadius()/4, Color.MAGENTA);
        }
    }

    @Override
    public boolean update(float touchX, float touchY, int eventAction) {
        if(isNeighbour()) {
            return super.update(touchX, touchY, eventAction);
        } else {
            isClicked = false;
            return true;
        }
    }

    private boolean isNeighbour() {
        int playerTile = game.getLocalPlayer().getTile();
        List<Integer> neighbours = game.getHexMap().getNeighbours(playerTile);

        return neighbours.contains(tileID);
    }
}
