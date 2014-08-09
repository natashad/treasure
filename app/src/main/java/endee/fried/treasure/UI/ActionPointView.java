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
public class ActionPointView extends SurfaceView {
    private Game game;

    private final int screenPixelWidth;

    private final int viewHeight;

    public ActionPointView(final Context context) {
        super(context);

        screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        viewHeight = context.getResources().getDisplayMetrics().heightPixels / 20;
    }

    public ActionPointView(Context context, AttributeSet attrs) {
        super(context, attrs);

        screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        viewHeight = context.getResources().getDisplayMetrics().heightPixels / 20;
    }

    public ActionPointView(Context context, AttributeSet attrs, int defStyle) {
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

        int ap = game.getLocalPlayer().getActionPoints();

        float percent = (float)ap / Game.MAX_ACTION_POINTS;

        Paint paint = new Paint();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);

        canvas.drawRect(0, 0, screenPixelWidth * percent, viewHeight, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(viewHeight);

        Rect rect = new Rect();
        paint.getTextBounds(""+ap, 0, (""+ap).length(), rect);

        canvas.drawText(""+ap, (screenPixelWidth - rect.width()) / 2, (viewHeight + rect.height()) / 2 , paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(screenPixelWidth, viewHeight);
    }
}
