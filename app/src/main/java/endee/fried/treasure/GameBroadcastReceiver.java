package endee.fried.treasure;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by natasha on 2014-08-06.
 */
public class GameBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pActivity activity;


    public GameBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                 WifiP2pActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if WiFi P2P mode is enabled or not, alert the activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d("", "Wifi p2p State enabled");
                activity.setIsWifiP2pEnabled(true);
            } else {
                Log.d("", "Wifi p2p State disabled");
                activity.setIsWifiP2pEnabled(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // The Peer List has changed.
            if (manager != null) {
                manager.requestPeers(channel, (WifiP2pManager.PeerListListener) ((Activity)activity).getFragmentManager()
                        .findFragmentById(R.id.frag_list));
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection State Changed.
            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP

                Log.d("GameBroadcastReceiver", "Connection established");

                DeviceDetailFragment fragment = (DeviceDetailFragment) ((Activity)activity)
                        .getFragmentManager().findFragmentById(R.id.frag_detail);
                manager.requestConnectionInfo(channel, fragment);
            } else {
                if (networkInfo.isConnectedOrConnecting()) {
                    Log.d("GameBroadcastReceiver", "is trying to connect");
                }
                else {
                    // It's a disconnect
                    Log.d("GameBroadcastReceiver", "RESETTING");
                    Log.d("GameBroadcastReceiver", "REASON: "  + networkInfo.getReason());

                    activity.resetData();
                }
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            DeviceListFragment fragment = (DeviceListFragment) ((Activity)activity).getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }
}
