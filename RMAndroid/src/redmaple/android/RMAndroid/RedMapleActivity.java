package redmaple.android.RMAndroid;

import android.os.Bundle;
import android.util.Log;
import com.badlogic.gdx.backends.android.AndroidApplication;
import redmaple.RedMaple;

public class RedMapleActivity extends AndroidApplication {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidPerfFuncs apf = new AndroidPerfFuncs();

        RedMaple redMaple = new RedMaple();
        AndroidSongFinder asf = new AndroidSongFinder(this, redMaple);
        RedMaple.songfinder = asf;
        RedMaple.actionResolver = new AndroidActionResolver(getApplicationContext());
        RedMaple.perfFuncs = apf;

        initialize(redMaple, true);
    }
}
