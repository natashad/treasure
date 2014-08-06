package endee.fried.treasure;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import endee.fried.treasure.UI.GameView;


public class GameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = new GameView(this);
        view.setBackgroundColor(Color.WHITE);
        setContentView(view);
    }
}
