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
        Bundle extras = getIntent().getExtras();
        long seed = extras.getLong(GameInvitationFragment.GAME_SEED);
        View view = new GameView(this, seed);
        view.setBackgroundColor(Color.WHITE);
        setContentView(view);
    }
}
