package endee.fried.treasure.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import endee.fried.treasure.MainActivity;

/**
 * Created by natasha on 2014-08-05.
 */
public class MenuView extends SurfaceView {
    List<Button> buttons;

    public MenuView(Context context) {
        super(context);
        setBackgroundColor(Color.WHITE);

        buttons = new ArrayList<Button>();

        final View myView = this;

        buttons.add(new Button(100, 100, 100, new Callback() {
            @Override
            public void doAction() {
                myView.getContext().startActivity(new Intent(myView.getContext(), MainActivity.class));
            }
        }));

        buttons.add(new Button(400, 400, 100, new Callback() {
            @Override
            public void doAction() {
                Log.e("","Pressed button 2!");
            }
        }));

        buttons.add(new Button(200, 700, 100, new Callback() {
            @Override
            public void doAction() {
                Log.e("","Pressed button 3!");
            }
        }));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();

        for(Button b : buttons) {
            b.draw(canvas, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean changed = false;

        for(Button b: buttons) {
            changed = b.update(event) || changed;
        }

        if(changed) this.invalidate();
        return true;
    }
}
