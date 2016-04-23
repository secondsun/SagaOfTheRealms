package net.sagaoftherealms.android.sagaoftherealms.input;

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by summers on 11/21/15.
 */
public interface JoyPadDelegate {

    boolean onKeyDown(int keyCode, KeyEvent event);

    boolean onGenericMotionEvent(MotionEvent event);

    boolean onKeyUp(int keyCode, KeyEvent event);

    /**
     * Set the game controller to be used to control the camera.
     *
     * @param dev the input device that will be controlling the camera
     */
    public void setInputDevice(InputDevice dev);
}
