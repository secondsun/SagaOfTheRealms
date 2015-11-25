package net.sagaoftherealms.android.sagaoftherealms.game;

import android.graphics.Bitmap;

import net.sagaoftherealms.android.sagaoftherealms.input.JoyPadDelegate;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Defines a game scene
 */
public interface Scene {

    /**
     * Set up the game
     */
    void setup() throws IOException;

    /**
     * Release resources, clean up, etc
     */
    void teardown();

    /**
     * Handles Input
     */
    void handleInput();

    /**
     * Updates game state
     */
    void updateGameWorld();


    /**
     * Returns a list of all bitmaps to be drawn to the screen in order.
     *
     */
    List<Bitmap> render();


    JoyPadDelegate attachJoypadDelegate(JoyPadDelegate delegate);


}
