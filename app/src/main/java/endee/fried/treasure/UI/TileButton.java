package endee.fried.treasure.UI;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.List;

import endee.fried.treasure.GameLogic.Game;
import endee.fried.treasure.GameLogic.Player;

/**
 * Created by natasha on 2014-08-05.
 */
public class TileButton extends CircleButton {

    private Game _game;
    private int _tileID;

    private Bitmap _image;

    public TileButton(float centerX, float centerY, float radius, Callback onClick, Game game, int tileID, Bitmap image) {
        super(centerX, centerY, radius, onClick);
        _game = game;
        _tileID = tileID;
        _image = image;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);

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
    protected int getCurrentBackgroundColor() {
        if(_game.getLocalPlayer().getTile() == _tileID) {
            return Color.YELLOW;
        } else if(isNeighbour() && !_game.hasMadeMove()) {
            return super.getCurrentBackgroundColor();
        } else {
            if(_game.getTile(_tileID).isDiscovered()) {
                return Color.GRAY;
            } else {
                return Color.BLACK;
            }
        }
    }

    @Override
    protected void drawButton(Canvas canvas, Paint paint) {
        paint.setColorFilter(new LightingColorFilter(getCurrentBackgroundColor(), 0));

        float scale = getRadius() * 2 / _image.getWidth();

        float width = getRadius() * 2;
        float height = _image.getHeight() * scale;

        Rect imageRect = new Rect(0, 0, _image.getWidth(), _image.getHeight());
        RectF screenRect = new RectF(getX() - width/2, getY() - height/2, getX() + width/2, getY() + height/2);

        canvas.drawBitmap(_image, imageRect, screenRect, paint);

        paint.setColorFilter(null);

        // Uncomment these too see the click area
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setColor(Color.BLACK);
//        paint.setStrokeWidth(Math.min(10f, getRadius() / 20f));
//        canvas.drawCircle(getX(), getY(), getRadius(), paint);
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
