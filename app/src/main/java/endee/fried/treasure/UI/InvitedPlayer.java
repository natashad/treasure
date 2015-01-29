package endee.fried.treasure.UI;

import java.io.Serializable;

import endee.fried.treasure.Bluetooth.BluetoothConnection;

/**
 * Created by leslie on 28/01/15.
 */
public class InvitedPlayer implements Serializable {
    private final BluetoothConnection _connection;
    private final boolean _host;
    private boolean _ready;


    public InvitedPlayer(BluetoothConnection connection, boolean host, boolean ready) {
        _connection = connection;
        _host = host;
        _ready = ready;
    }

    public InvitedPlayer(BluetoothConnection connection, boolean host) {
        this(connection, host, false);
    }

    public void setReady(boolean ready) {
        _ready = ready;
    }

    public boolean isHost() { return _host; }

    public boolean isReady() { return _ready; }

    public BluetoothConnection getConnection() { return _connection; }

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof InvitedPlayer)) {
            return false;
        }
        return _connection.equals((((InvitedPlayer) o).getConnection())) &&
                _host == ((InvitedPlayer) o).isHost() &&
                _ready == ((InvitedPlayer) o).isReady();
    }
}
