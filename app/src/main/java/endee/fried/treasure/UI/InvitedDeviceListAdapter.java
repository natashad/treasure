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
 * Created by natasha on 2014-08-25.
 */
public class InvitedDeviceListAdapter extends ArrayAdapter<BluetoothConnection> {

    private static final String TAG = InvitedDeviceListAdapter.class.getName();

    private Context _context;
    private int _layoutResourceId;

    public InvitedDeviceListAdapter(Context context, int resource, ArrayList<BluetoothConnection> data) {
        super(context, resource, data);
        _context = context;
        _layoutResourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) _context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.invited_device_listitem, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.btText);
        textView.setText(getItem(position).getName());

        //TODO: Things with the [x] button and the checking the checkbox when ready!
        return rowView;
    }

}
