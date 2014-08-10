package endee.fried.treasure.UI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

import endee.fried.treasure.GameLogic.Game;
import endee.fried.treasure.GameLogic.Player;

/**
 * Created by natasha on 2014-08-05.
 */
public class TileButton extends Button {

    private Game _game;
    private int _tileID;

    public TileButton(float centerX, float centerY, float radius, Callback callback, Game game, int tileID) {
        super(centerX, centerY, radius, callback);
        this._game = game;
        this._tileID = tileID;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if(_game.getLocalPlayer().getTile() == _tileID) {
            drawButton(canvas, paint, getRadius(), Color.YELLOW);
        } else if(isNeighbour() && !_game.hasMadeMove()) {
            super.draw(canvas, paint);
        } else {
            if(_game.getTile(_tileID).isDiscovered()) {
                drawButton(canvas, paint, getRadius(), Color.GRAY);
            } else {
                drawButton(canvas, paint, getRadius(), Color.BLACK);
            }
        }

        for(int i = 0; i < _game.getNumPlayers(); i++) {
            Player p = _game.getPlayer(i);
            if(p.getTile() != _tileID || p == _game.getLocalPlayer()) continue;

            drawButton(canvas, paint, getRadius() * 0.67f, Color.BLUE);
        }

        if(_game.getTile(_tileID).getLastKnownItem() != null) {
            drawButton(canvas, paint, getRadius()/2, Color.CYAN);
        }
        if(_game.getTile(_tileID).getItem() != null) {
            drawButton(canvas, paint, getRadius()/4, Color.RED);
        } else if(_game.getKeyTile() == _tileID) {
            drawButton(canvas, paint, getRadius()/4, Color.YELLOW);
        } else if(_game.getTreasureTile() == _tileID) {
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
        int playerTile = _game.getLocalPlayer().getTile();
        List<Integer> neighbours = _game.getHexMap().getNeighbours(playerTile);

        return neighbours.contains(_tileID);
    }
}
