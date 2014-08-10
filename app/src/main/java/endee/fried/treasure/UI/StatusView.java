package endee.fried.treasure.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceView;

import endee.fried.treasure.GameLogic.Game;

/**
 * Created by natasha on 2014-08-05.
 */
public class StatusView extends SurfaceView {
    private Game _game;

    private final int _screenPixelWidth;

    private final int _viewHeight;

    public StatusView(final Context context) {
        super(context);

        _screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        _viewHeight = context.getResources().getDisplayMetrics().heightPixels / 20;
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);

        _screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        _viewHeight = context.getResources().getDisplayMetrics().heightPixels / 20;
    }

    public StatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        _screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        _viewHeight = context.getResources().getDisplayMetrics().heightPixels / 20;
    }

    public void init(Game _game) {
        this._game = _game;
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String status = "";

        if(_game.getGameState() == Game.State.WINNER) {
            status = "You Win!";
        } else if(_game.getGameState() == Game.State.LOSER) {
            status = "Loser!";
        } else if(!_game.hasMadeMove()) {
            status = "Time to make a move!";
        } else {
            status = "Waiting on " + _game.waitingOnNumOpponent() + " opponent(s)";
        }

        Paint paint = new Paint();

        paint.setColor(Color.WHITE);
        paint.setTextSize(_viewHeight / 2);

        Rect rect = new Rect();
        paint.getTextBounds(status, 0, status.length(), rect);

        canvas.drawText(status, (_screenPixelWidth - rect.width()) / 2, (_viewHeight + rect.height()) / 2 , paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(_screenPixelWidth, _viewHeight);
    }
}
