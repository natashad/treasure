package endee.fried.treasure.UI;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.Random;

import endee.fried.treasure.GameLogic.Game;
import endee.fried.treasure.R;


public class GameActivity extends Activity {
    private Game game;

    public GameActivity() {
        game = new Game(1, 0, new Random().nextLong(), this, new Callback() {
            @Override
            public void doAction() {
                Log.d("", "Invalidating in game callback");
                findViewById(R.id.map_view).invalidate();
                findViewById(R.id.action_point_view).invalidate();
                findViewById(R.id.status_view).invalidate();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_layout);

        ((MapView)findViewById(R.id.map_view)).init(game);
        ((ActionPointView)findViewById(R.id.action_point_view)).init(game);
        ((StatusView)findViewById(R.id.status_view)).init(game);
    }
}
