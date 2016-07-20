package net.sagaoftherealms.android.sagaoftherealms;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import net.sagaoftherealms.android.sagaoftherealms.game.CityDemo;
import net.sagaoftherealms.android.sagaoftherealms.game.Scene;
import net.sagaoftherealms.android.sagaoftherealms.game.StrategyMapScene;
import net.sagaoftherealms.android.sagaoftherealms.input.JoyPadDelegate;

import java.io.IOException;
import java.util.List;

import static java.lang.System.currentTimeMillis;


/**
 * @author impaler
 *         <p/>
 *         The Main thread which contains the game loop. The thread must have access to
 *         the surface view and holder to trigger events every game tick.
 */
public class MainThread extends Thread {

    private static final String TAG = MainThread.class.getSimpleName();

    public static final int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels / 4;
    public static final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels / 4;

    private static final Matrix scale = new Matrix();

    static {
        scale.setScale(Resources.getSystem().getDisplayMetrics().widthPixels / screenWidth, Resources.getSystem().getDisplayMetrics().heightPixels / screenHeight);
    }


    private Scene scene;

    // Surface holder that can access the physical surface
    private SurfaceHolder surfaceHolder;

    private boolean running;
    private JoyPadDelegate delegate;

    public void setRunning(boolean running) {
        this.running = running;
    }

    public MainThread(SurfaceHolder surfaceHolder, MainGamePanel gamePanel) throws IOException {
        super();
        this.surfaceHolder = surfaceHolder;
        this.scene = new CityDemo(surfaceHolder, gamePanel);

    }


    @Override
    public void run() {
        try {
            scene.setup();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.delegate = scene.getJoypadDelegate();
        long tickCount = 0L;
        Log.d(TAG, "Starting game loop");
        while (running) {
            long millis = currentTimeMillis();
            tickCount++;
            scene.handleInput();
            scene.updateGameWorld();
            swap(scene.render());
            Log.d(TAG, String.format("FPS %d", (1000/(currentTimeMillis() - millis))));
        }
        scene.teardown();
        Log.d(TAG, "Game loop executed " + tickCount + " times");
    }


    private void swap(List<Bitmap> render) {
        Canvas canvas = surfaceHolder.getSurface().lockHardwareCanvas();
        canvas.drawARGB(255, 0, 0, 0);

        for (Bitmap bitmap : render) {
            canvas.drawBitmap(bitmap, scale, null);
        }

        surfaceHolder.getSurface().unlockCanvasAndPost(canvas);

    }


    public boolean onGenericMotionEvent(MotionEvent event) {
        return delegate.onGenericMotionEvent(event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return delegate.onKeyUp(keyCode, event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return delegate.onKeyDown(keyCode, event);
    }
}