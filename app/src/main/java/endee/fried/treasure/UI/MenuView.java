package endee.fried.treasure.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import endee.fried.treasure.BluetoothLounge;
import endee.fried.treasure.BluetoothManager;
import endee.fried.treasure.GameInvitationFragment;
import endee.fried.treasure.InviteeLounge;
import endee.fried.treasure.NewBluetoothLoungeActivity;

/**
 * Created by natasha on 2014-08-05.
 */
public class MenuView extends SurfaceView {
    private final List<Button> _buttons;

    public MenuView(final Context context) {
        super(context);
        setBackgroundColor(Color.WHITE);

        int screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenPixelHeight = context.getResources().getDisplayMetrics().heightPixels;

        _buttons = new ArrayList<Button>();

        _buttons.add(new RectangleButton(screenPixelWidth / 2, screenPixelHeight * 0.1f, screenPixelWidth * 0.8f, screenPixelHeight * 0.1f, "Start Game", new Callback() {
            @Override
            public void doAction(Object obj) {
                Intent intent = new Intent(getContext(), GameActivity.class);
                intent.putExtra(NewBluetoothLoungeActivity.GAME_SEED_KEY, new Random().nextLong());
                intent.putExtra(InviteeLounge.PLAYER_NUMBER_KEY, 0);
                intent.putExtra(InviteeLounge.NUMBER_OF_PLAYERS, 1);
                getContext().startActivity(intent);
            }
        }));

        _buttons.add(new RectangleButton(screenPixelWidth / 2, screenPixelHeight * 0.25f, screenPixelWidth * 0.8f, screenPixelHeight * 0.1f, "Host Game", new Callback() {
            @Override
            public void doAction(Object obj) {
                context.startActivity(new Intent(getContext(), BluetoothLounge.class));
            }
        }));

        _buttons.add(new RectangleButton(screenPixelWidth / 2, screenPixelHeight * 0.40f, screenPixelWidth * 0.8f, screenPixelHeight * 0.1f, "Make Discoverable", new Callback() {
            @Override
            public void doAction(Object obj) {
                BluetoothManager.getInstance().ensureDiscoverable(context);
            }
        }));

        _buttons.add(new RectangleButton(screenPixelWidth / 2, screenPixelHeight * 0.55f, screenPixelWidth * 0.8f, screenPixelHeight * 0.1f, "Temp New Bluetooth Lounge", new Callback() {
            @Override
            public void doAction(Object obj) {
                context.startActivity(new Intent(getContext(), NewBluetoothLoungeActivity.class));
            }
        }));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();

        for(Button b : _buttons) {
            b.draw(canvas, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean changed = false;

        for(Button b: _buttons) {
            changed = b.update(event.getX(), event.getY(), event.getAction()) || changed;
        }

        if(changed) this.invalidate();
        return true;
    }
}
