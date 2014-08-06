package endee.fried.treasure;

import android.app.Activity;
import android.os.Bundle;

import endee.fried.treasure.UI.MenuView;


public class MainMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MenuView view = new MenuView(this);
        setContentView(view);
    }
}
