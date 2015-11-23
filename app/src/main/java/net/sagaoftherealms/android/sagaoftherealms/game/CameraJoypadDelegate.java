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

import static net.sagaoftherealms.android.sagaoftherealms.input.JoypadUtils.getCenteredAxis;

/**
 * This class moves the camera behind the screen providing basic perspective transformations.
 *
 *
 */
public class CameraJoypadDelegate implements JoyPadDelegate {

    private static final int DPAD_STATE_LEFT = 1 << 0;
    private static final int DPAD_STATE_RIGHT = 1 << 1;
    private static final int DPAD_STATE_UP = 1 << 2;
    private static final int DPAD_STATE_DOWN = 1 << 3;

    private final Sprite camera;

    private int mDPadState;

    private final int maxAcceleration;
    private InputDevice mInputDevice;

    public CameraJoypadDelegate(Sprite camera, int maxAcceleration) {
        this.camera = camera;
        this.maxAcceleration = maxAcceleration;
    }


    public boolean onKeyUp(int keyCode, KeyEvent event) {
        int deviceId = event.getDeviceId();
        boolean handled = false;

            // Handle keys going up.

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    camera.speedX = 0;
                    mDPadState &= ~DPAD_STATE_LEFT;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    camera.speedX = 0;
                    mDPadState &= ~DPAD_STATE_RIGHT;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    camera.speedY = 0;
                    mDPadState &= ~DPAD_STATE_UP;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    camera.speedY = 0;
                    mDPadState &= ~DPAD_STATE_DOWN;
                    handled = true;
                    break;
                default:
                    break;
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
                    camera.speedX = (-maxAcceleration);
                    mDPadState |= DPAD_STATE_LEFT;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    camera.speedX = (maxAcceleration);
                    mDPadState |= DPAD_STATE_RIGHT;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    camera.speedX = (-maxAcceleration);
                    mDPadState |= DPAD_STATE_UP;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    camera.speedX = (maxAcceleration);
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
     * The camera directly handles joystick input.
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

        // Set the camera heading.
        camera.speedX = x < 0 ? -maxAcceleration : x == 0 ? 0 : maxAcceleration;
        camera.speedY = y < 0 ? -maxAcceleration : y == 0 ? 0 : maxAcceleration;

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
     * Set the game controller to be used to control the camera.
     *
     * @param dev the input device that will be controlling the camera
     */
    public void setInputDevice(InputDevice dev) {
        mInputDevice = dev;
    }

}
