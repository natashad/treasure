package endee.fried.treasure.UI;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import endee.fried.treasure.Bluetooth.BluetoothConnection;
import endee.fried.treasure.R;

/**
 * Created by natasha on 2014-08-25.
 */
public class InvitedDeviceListAdapter extends ArrayAdapter<InvitedPlayer> {

    private static final String TAG = InvitedDeviceListAdapter.class.getName();

    private Context _context;
    private String _address;

    public InvitedDeviceListAdapter(Context context, int resource, List<InvitedPlayer> data, String address) {
        super(context, resource, data);
        _context = context;
        _address = address;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) _context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;

        // Don't show ourselves
        if(_address.equals(getItem(position).getConnection().getAddress())) {
            rowView = inflater.inflate(R.layout.empty_row, parent, false);
        } else {
            rowView = inflater.inflate(R.layout.invited_device_listitem, parent, false);

            TextView textView = (TextView) rowView.findViewById(R.id.btText);
            CheckBox readyBox = (CheckBox) rowView.findViewById(R.id.readyCheckBox);

            textView.setText(getItem(position).getConnection().getName());

            if (getItem(position).isHost()) {
                readyBox.setVisibility(View.INVISIBLE);
                textView.setTextColor(Color.BLUE);
            } else {
                readyBox.setChecked(getItem(position).isReady());
            }
        }

        return rowView;
    }

}
