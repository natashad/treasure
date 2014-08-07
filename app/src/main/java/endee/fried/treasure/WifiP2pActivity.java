package endee.fried.treasure;

/**
 * Created by natasha on 2014-08-06.
 */
public interface WifiP2pActivity {
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled);
    public void discoverPeers();
    public void resetData();
}
