package net.sagaoftherealms.android.sagaoftherealms.game;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.view.InputDevice;
import android.view.SurfaceHolder;

import net.sagaoftherealms.android.sagaoftherealms.MainGamePanel;
import net.sagaoftherealms.android.sagaoftherealms.input.InputManagerCompat;
import net.sagaoftherealms.android.sagaoftherealms.input.JoyPadDelegate;

import java.io.IOException;

public abstract class AbstractScene implements Scene, InputManagerCompat.InputDeviceListener  {

    public static final int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels / 4;
    public static final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels / 4;

    protected final SurfaceHolder surfaceHolder;
    protected final MainGamePanel gamePanel;
    protected final JoyPadDelegate joypadDelegate;
    private InputManagerCompat mInputManager;

    public AbstractScene(SurfaceHolder surfaceHolder, MainGamePanel gamePanel) {
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
        this.joypadDelegate = createJoypadDelegate();
    }

    @Override
    public final JoyPadDelegate getJoypadDelegate() {
        return joypadDelegate;
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
        joypadDelegate.setInputDevice(dev);
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
        joypadDelegate.setInputDevice(InputDevice.getDevice(deviceId));
    }

    /*
     * Remove any ship associated with the ID.
     * @see
     * com.example.inputmanagercompat.InputManagerCompat.InputDeviceListener
     * #onInputDeviceRemoved(int)
     */
    @Override
    public void onInputDeviceRemoved(int deviceId) {
        joypadDelegate.setInputDevice(null);
    }

    @Override
    public void setup() throws IOException {
        mInputManager = InputManagerCompat.Factory.getInputManager(gamePanel.getContext());
        mInputManager.registerInputDeviceListener(this, new Handler(Looper.getMainLooper()));
    }

    @Override
    public void teardown() {
        mInputManager.unregisterInputDeviceListener(this);
    }
}
