package endee.fried.treasure;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = new MyView(this);
        view.setBackgroundColor(Color.WHITE);
        setContentView(view);

    }
}
