/**
 * Containes code from AOSP
 * Copyright (C) 2013 The Android Open Source Project
 */
package net.sagaoftherealms.android.sagaoftherealms.game;

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import net.sagaoftherealms.android.sagaoftherealms.gfx.Sprite;
import net.sagaoftherealms.android.sagaoftherealms.input.JoyPadDelegate;
import net.sagaoftherealms.android.sagaoftherealms.input.JoypadUtils;

import static java.lang.Math.ceil;
import static net.sagaoftherealms.android.sagaoftherealms.input.JoypadUtils.getCenteredAxis;

/**
 * Created by summers on 11/21/15.
 */
public class ShipJoypadDelegate implements JoyPadDelegate {

    private static final int DPAD_STATE_LEFT = 1 << 0;
    private static final int DPAD_STATE_RIGHT = 1 << 1;
    private static final int DPAD_STATE_UP = 1 << 2;
    private static final int DPAD_STATE_DOWN = 1 << 3;

    private final Sprite ship;

    private int mDPadState;
    private InputDevice mInputDevice;

    public ShipJoypadDelegate(Sprite ship) {
        this.ship = ship;
    }


    public boolean onKeyUp(int keyCode, KeyEvent event) {
        int deviceId = event.getDeviceId();
        boolean handled = false;
        if (deviceId != -1) {
            // Handle keys going up.

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    ship.speedX = 0;
                    mDPadState &= ~DPAD_STATE_LEFT;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    ship.speedX = 0;
                    mDPadState &= ~DPAD_STATE_RIGHT;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    ship.speedY = 0;
                    mDPadState &= ~DPAD_STATE_UP;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    ship.speedY = 0;
                    mDPadState &= ~DPAD_STATE_DOWN;
                    handled = true;
                    break;
                default:
                    break;
            }

        }

        return handled;

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle DPad keys and fire button on initial down but not on
        // auto-repeat.
        boolean handled = false;
        if (event.getRepeatCount() == 0) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    ship.speedX = (-1);
                    mDPadState |= DPAD_STATE_LEFT;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    ship.speedX = (1);
                    mDPadState |= DPAD_STATE_RIGHT;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    ship.speedX = (-1);
                    mDPadState |= DPAD_STATE_UP;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    ship.speedX = (1);
                    mDPadState |= DPAD_STATE_DOWN;
                    handled = true;
                    break;
                default:
                    break;
            }
        }
        return handled;
    }


    /**
     * The ship directly handles joystick input.
     *
     * @param event
     * @param historyPos
     */
    private void processJoystickInput(MotionEvent event, int historyPos) {
        // Get joystick position.
        // Many game pads with two joysticks report the position of the
        // second
        // joystick
        // using the Z and RZ axes so we also handle those.
        // In a real game, we would allow the user to configure the axes
        // manually.
        if (null == mInputDevice) {
            mInputDevice = event.getDevice();
        }
        float x = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_X, historyPos);
        if (x == 0) {
            x = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_HAT_X, historyPos);
        }
        if (x == 0) {
            x = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Z, historyPos);
        }

        float y = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Y, historyPos);
        if (y == 0) {
            y = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_HAT_Y, historyPos);
        }
        if (y == 0) {
            y = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RZ, historyPos);
        }

        // Set the ship heading.
        ship.speedX = (int) ceil(x);
        ship.speedY = (int) ceil(y);

    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if (0 == mDPadState) {
            // Process all historical movement samples in the batch.
            final int historySize = event.getHistorySize();
            for (int i = 0; i < historySize; i++) {
                processJoystickInput(event, i);
            }

            // Process the current movement sample in the batch.
            processJoystickInput(event, -1);
        }
        return true;
    }

    /**
     * Set the game controller to be used to control the ship.
     *
     * @param dev the input device that will be controlling the ship
     */
    public void setInputDevice(InputDevice dev) {
        mInputDevice = dev;
    }

}
