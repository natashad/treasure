package endee.fried.treasure.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import endee.fried.treasure.Bluetooth.BluetoothConnection;
import endee.fried.treasure.R;

/**
 * Created by natasha on 2014-08-24.
 */
public class BluetoothDeviceListAdapter extends ArrayAdapter<BluetoothConnection> {

    private static final String TAG = BluetoothDeviceListAdapter.class.getName();

    private Context _context;
    private int _layoutResourceId;

    public BluetoothDeviceListAdapter(Context context, int resource, ArrayList<BluetoothConnection> data) {
        super(context, resource, data);
        _context = context;
        _layoutResourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) _context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.bluetooth_devices_listitem, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.btDeviceText);
        textView.setText(getItem(position).getName());
        return rowView;
    }

}
