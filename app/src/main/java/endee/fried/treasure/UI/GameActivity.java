package endee.fried.treasure.UI;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.Random;

import endee.fried.treasure.GameInvitationFragment;
import endee.fried.treasure.GameLogic.Game;
import endee.fried.treasure.R;


public class GameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_layout);

        Bundle extras = getIntent().getExtras();
        long seed = extras.getLong(GameInvitationFragment.GAME_SEED);
        if (seed == -1) {
            seed = new Random().nextLong();
        }

        Game game = new Game(1, 0, seed, this, new Callback() {
            @Override
            public void doAction() {
                Log.d("", "Invalidating in game callback");
                findViewById(R.id.map_view).invalidate();
                findViewById(R.id.action_point_view).invalidate();
                findViewById(R.id.status_view).invalidate();
            }
        });

        ((MapView)findViewById(R.id.map_view)).init(game);
        ((ActionPointView)findViewById(R.id.action_point_view)).init(game);
        ((StatusView)findViewById(R.id.status_view)).init(game);
    }
}
