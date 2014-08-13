package endee.fried.treasure.Bluetooth;

/**
 * Created by natasha on 2014-08-12.
 */
/**
 * Class that represents a single BluetoothConnection. Stores address and name.
 */
public class BluetoothConnection {

    private String name;
    private String address;

    public BluetoothConnection(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
