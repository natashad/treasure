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
public class TileButton extends CircleButton {

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
            drawButton(canvas, paint, Color.YELLOW);
        } else if(isNeighbour() && !_game.hasMadeMove()) {
            super.draw(canvas, paint);
        } else {
            if(_game.getTile(_tileID).isDiscovered()) {
                drawButton(canvas, paint, Color.GRAY);
            } else {
                drawButton(canvas, paint, Color.BLACK);
            }
        }

        for(int i = 0; i < _game.getNumPlayers(); i++) {
            Player p = _game.getPlayer(i);
            if(p.getTile() != _tileID || p == _game.getLocalPlayer()) continue;

            drawCircle(canvas, paint, getRadius() * 0.67f, Color.BLUE);
        }

        if(_game.getTile(_tileID).getLastKnownItem() != null) {
            drawCircle(canvas, paint, getRadius() / 2, Color.CYAN);
        }
        if(_game.getTile(_tileID).getItem() != null) {
            drawCircle(canvas, paint, getRadius() / 4, Color.RED);
        } else if(_game.getKeyTile() == _tileID) {
            drawCircle(canvas, paint, getRadius() / 4, Color.YELLOW);
        } else if(_game.getTreasureTile() == _tileID) {
            drawCircle(canvas, paint, getRadius() / 4, Color.MAGENTA);
        }
    }

    @Override
    public boolean update(float touchX, float touchY, int eventAction) {
        if(isNeighbour()) {
            return super.update(touchX, touchY, eventAction);
        } else {
            _clicked = false;
            return true;
        }
    }

    private boolean isNeighbour() {
        int playerTile = _game.getLocalPlayer().getTile();
        List<Integer> neighbours = _game.getHexMap().getNeighbours(playerTile);

        return neighbours.contains(_tileID);
    }

    private void drawCircle(Canvas canvas, Paint paint, float radius, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(getX(), getY(), radius, paint);
    }
}
