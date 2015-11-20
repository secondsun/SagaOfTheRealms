package net.sagaoftherealms.android.sagaoftherealms;

import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class SOTRMain extends Activity {

    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requesting to turn the title OFF
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set our MainGamePanel as the View
        setContentView(new MainGamePanel(this));

    }


}
