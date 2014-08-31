package endee.fried.treasure.Bluetooth;

/**
 * Created by natasha on 2014-08-12.
 */

import java.io.Serializable;

/**
 * Class that represents a single BluetoothConnection. Stores address and name.
 */
public class BluetoothConnection implements Serializable {

    private static final long serialVersionUID = 7526472295622776147L;

    private String _name;
    private String _address;

    public BluetoothConnection(String name, String address) {
        _name = name;
        _address = address;
    }

    public String getName() {
        return _name;
    }

    public String getAddress() {
        return _address;
    }

    @Override
    public String toString() {
        return _name + " @ "  + _address;
    }

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof BluetoothConnection)) {
            return false;
        }
        return _name.equals((((BluetoothConnection) o).getName())) &&
               _address.equals(((BluetoothConnection) o).getAddress());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_name == null) ? 0 : _name.hashCode());
        result = prime * result + ((_address == null) ? 0 : _address.hashCode());
        return result;
    }
}
