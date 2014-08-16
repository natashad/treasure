package endee.fried.treasure.GameLogic;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by leslie on 08/08/14.
 */
public class MakeToastItem implements Item {
    private final Context context;
    private final String name = "ToastItem";

    public MakeToastItem(Context context) {
        this.context = context;
    }

    public int getCost() { return 0; }

    public void activateItem() {
        Toast.makeText(context,"Mmm Toast ", Toast.LENGTH_SHORT).show();
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }
}
