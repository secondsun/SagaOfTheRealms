package net.sagaoftherealms.android.sagaoftherealms;

import android.content.Context;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.sagaoftherealms.android.sagaoftherealms.game.CameraJoypadDelegate;
import net.sagaoftherealms.android.sagaoftherealms.input.InputManagerCompat;

/**
 * @author impaler
 * This is the main surface that handles the ontouch events and draws
 * the image to the screen.
 */
public class MainGamePanel extends SurfaceView implements
        SurfaceHolder.Callback, InputManagerCompat.InputDeviceListener {

    private static final String TAG = MainGamePanel.class.getSimpleName();
    private final CameraJoypadDelegate shipJoypadDelegate;

    private MainThread thread;
    private final InputManagerCompat mInputManager;

    public MainGamePanel(Context context) {
        super(context);
        // adding the callback (this) to the surface holder to intercept events
        getHolder().addCallback(this);

        // create the game loop thread
        thread = new MainThread(getHolder(), this);

        shipJoypadDelegate = new CameraJoypadDelegate(thread.eye, 15);

        // make the GamePanel focusable so it can handle events
        setFocusable(true);

        mInputManager = InputManagerCompat.Factory.getInputManager(this.getContext());
        mInputManager.registerInputDeviceListener(this, null);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // at this point the surface is created and
        // we can safely start the game loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface is being destroyed");
        // tell the thread to shut down and wait for it to finish
        // this is a clean shutdown
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                thread.setRunning(false);
                retry = false;
            } catch (InterruptedException e) {
                // try again shutting down the thread
            }
        }
        Log.d(TAG, "Thread was shut down cleanly");
    }


    /*
     * When an input device is added, we add a ship based upon the device.
     * @see
     * com.example.inputmanagercompat.InputManagerCompat.InputDeviceListener
     * #onInputDeviceAdded(int)
     */
    @Override
    public void onInputDeviceAdded(int deviceId) {
        InputDevice dev = InputDevice.getDevice(deviceId);
        shipJoypadDelegate.setInputDevice(dev);

    }

    /*
     * This is an unusual case. Input devices don't typically change, but they
     * certainly can --- for example a device may have different modes. We use
     * this to make sure that the ship has an up-to-date InputDevice.
     * @see
     * com.example.inputmanagercompat.InputManagerCompat.InputDeviceListener
     * #onInputDeviceChanged(int)
     */
    @Override
    public void onInputDeviceChanged(int deviceId) {
        shipJoypadDelegate.setInputDevice(InputDevice.getDevice(deviceId));
    }

    /*
     * Remove any ship associated with the ID.
     * @see
     * com.example.inputmanagercompat.InputManagerCompat.InputDeviceListener
     * #onInputDeviceRemoved(int)
     */
    @Override
    public void onInputDeviceRemoved(int deviceId) {
        shipJoypadDelegate.setInputDevice(null);
        thread.eye.speedX = thread.eye.speedY = thread.eye.speedZ = 0;
        thread.eye.x = MainThread.halfScreenWidth;
        thread.eye.y = MainThread.halfScreenWidth;
        thread.eye.z = 100;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return shipJoypadDelegate.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return shipJoypadDelegate.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return shipJoypadDelegate.onGenericMotionEvent(event);
    }
}
