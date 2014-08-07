package endee.fried.treasure.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import endee.fried.treasure.BluetoothChat;
import endee.fried.treasure.GameActivity;

/**
 * Created by natasha on 2014-08-05.
 */
public class MenuView extends SurfaceView implements WifiP2pManager.PeerListListener{
    List<Button> buttons;
    private Context context;

    public MenuView(Context context) {
        super(context);
        setBackgroundColor(Color.WHITE);

        this.context = context;

        buttons = new ArrayList<Button>();

        final View myView = this;

        buttons.add(new Button(100, 100, 100, new Callback() {
            @Override
            public void doAction() {
                getContext().startActivity(new Intent(getContext(), GameActivity.class));
            }
        }));

        buttons.add(new Button(400, 400, 100, new Callback() {
            @Override
            public void doAction() {
                Log.e("","Pressed button 2!");
                getContext().startActivity(new Intent(getContext(), BluetoothChat.class));
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
            changed = b.update(event.getX(), event.getY(), event.getAction()) || changed;
        }

        if(changed) this.invalidate();
        return true;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        Collection<WifiP2pDevice> deviceList = peerList.getDeviceList();
        for (WifiP2pDevice device : deviceList) {
            Log.d("" , device.deviceName + " " + device.deviceAddress);
        }


    }
}
