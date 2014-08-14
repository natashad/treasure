package endee.fried.treasure.UI;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import endee.fried.treasure.Bluetooth.BluetoothConnection;
import endee.fried.treasure.TemporaryActivity;

/**
 * Created by natasha on 2014-08-12.
 */
public class LoungeView extends SurfaceView {

    // CONSTANTS
    private static final String TAG = SurfaceView.class.getName();
    
    // MEMBERS VARIABLES
    private BluetoothAdapter _bluetoothAdapter;
    private Button _scanButton;
    private ArrayList<BluetoothConnection> _availableConnections = new ArrayList();
    private ArrayList<Button> _availableConButtons = new ArrayList<Button>();
    private float _buttonWidthScale = 0.8f;
    private float _buttonHeightScale = 0.1f;

    private int _screenPixelWidth;
    private int _screenPixelHeight;
    private boolean _isSearching = false;



    public LoungeView(Context context) {
        super(context);

        setBackgroundColor(Color.WHITE);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(_receiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(_receiver, filter);

        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        _screenPixelWidth = context.getResources().getDisplayMetrics().widthPixels;
        _screenPixelHeight = context.getResources().getDisplayMetrics().heightPixels;

        _scanButton = new RectangleButton(_screenPixelWidth /2, _screenPixelHeight * _buttonHeightScale,
                _screenPixelWidth * _buttonWidthScale, _screenPixelHeight * _buttonHeightScale, "Scan for Devices",
                new Callback() {
                    @Override
                    public void doAction(Object obj) {
                        Log.d(TAG, "Scanning for devices");
                        doDiscovery();
                    }
                });

        Set<BluetoothDevice> pairedDevices = _bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                final String name = device.getName();
                final String address = device.getAddress();
                _availableConnections.add(new BluetoothConnection(device.getName(), device.getAddress()));
                _availableConButtons.add(new RectangleButton(_screenPixelWidth / 2, -1,
                        _screenPixelWidth, _screenPixelHeight * 0.05f, name,
                        new Callback() {
                            @Override
                            public void doAction(Object obj) {

                                // Get the BLuetoothDevice object
                                BluetoothDevice device = _bluetoothAdapter.getRemoteDevice(address);
                                // Attempt to connect to the device
                                ((TemporaryActivity) getContext()).connectToDevice(device);
                                ((TemporaryActivity) getContext()).connectToDevice(device);
                            }
                        }
                ));
            }

        }

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint p = new Paint();

        int count = 0;

        ListUI connList = new ListUI();

        for (BluetoothConnection conn : ((TemporaryActivity)getContext()).getConnectedDevices().values()) {
            connList.addListItem(new ConnectedDeviceListItem(conn.getName(), _screenPixelHeight*0.05f, _screenPixelWidth));
        }

        connList.draw(canvas, p);

        float offset = connList.getHeight() + (_screenPixelHeight * 0.03f);

        p = new Paint();
        _scanButton.setY( (_screenPixelHeight * _buttonHeightScale) + offset );
        _scanButton.draw(canvas, p);

        offset += _screenPixelHeight * 0.05f + _scanButton.getY() + ((RectangleButton)_scanButton).getHeight();

        count = 0;

        if (_isSearching) {

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.BLACK);
            p.setTextSize(30);
            canvas.drawText("Scanning...", _screenPixelWidth*(1-_buttonWidthScale)/2, offset, p );

        } else {

            for (Button b : _availableConButtons) {

                b.setY(offset + (count * _screenPixelHeight * 0.05f));
                b.setTextColor(Color.BLACK);
                b.drawButton(canvas, p);
                count += 1;
            }

        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        ArrayList<Button> allButtons = new ArrayList<Button>();
        allButtons.add(_scanButton);
        allButtons.addAll(_availableConButtons);

        boolean changed = false;

        for(Button b: allButtons) {
            changed = b.update(event.getX(), event.getY(), event.getAction()) || changed;
        }

        if (changed) { this.invalidate(); }

        return true;
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        _availableConnections.clear();
        _availableConButtons.clear();
        _isSearching = true;

        // If we're already discovering, stop it
        if (_bluetoothAdapter.isDiscovering()) {
            _bluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        _bluetoothAdapter.startDiscovery();

        invalidate();
    }


    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver _receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                Log.d(TAG, "discovered something");

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    // devices that are already paired, in case we care.
                }

                final String name = device.getName();
                final String address = device.getAddress();

                _availableConnections.add(new BluetoothConnection(name, device.getAddress()));
                Button b = new RectangleButton(_screenPixelWidth/2, -1,
                        _screenPixelWidth, _screenPixelHeight * 0.05f, name,
                        new Callback() {
                            @Override
                            public void doAction(Object obj) {

                                // Get the BLuetoothDevice object
                                BluetoothDevice device = _bluetoothAdapter.getRemoteDevice(address);
                                // Attempt to connect to the device
                                ((TemporaryActivity)getContext()).connectToDevice(device);
                                ((TemporaryActivity)getContext()).connectToDevice(device);
                            }
                        });
                _availableConButtons.add(b);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Log.d(TAG, "Discovery finished");

                if (_availableConnections.isEmpty()) {
                    Toast.makeText(LoungeView.this.getContext(),
                            "No Connections are available. Ensure that other devices are discoverable.",
                            Toast.LENGTH_LONG).show();
                }

                _isSearching = false;
                LoungeView.this.invalidate();


            }
        }
    };
}
