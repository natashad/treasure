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
    private Game game;

    private final int screenPixelWidth;

    private final int viewHeight;

    public StatusView(final Context context) {
        super(context);

        screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        viewHeight = context.getResources().getDisplayMetrics().heightPixels / 20;
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);

        screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        viewHeight = context.getResources().getDisplayMetrics().heightPixels / 20;
    }

    public StatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        viewHeight = context.getResources().getDisplayMetrics().heightPixels / 20;
    }

    public void init(Game _game) {
        game = _game;
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String status = "";

        if(game.getGameState() == Game.State.WINNER) {
            status = "You Win!";
        } else if(game.getGameState() == Game.State.LOSER) {
            status = "Loser!";
        } else if(!game.hasMadeMove()) {
            status = "Time to make a move!";
        } else {
            status = "Waiting on " + game.waitingOnNumOpponent() + " opponents";
        }

        Paint paint = new Paint();

        paint.setColor(Color.WHITE);
        paint.setTextSize(viewHeight);

        Rect rect = new Rect();
        paint.getTextBounds(status, 0, status.length(), rect);

        canvas.drawText(status, (screenPixelWidth - rect.width()) / 2, (viewHeight + rect.height()) / 2 , paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(screenPixelWidth, viewHeight);
    }
}
