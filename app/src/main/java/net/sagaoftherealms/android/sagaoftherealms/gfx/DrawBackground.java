package net.sagaoftherealms.android.sagaoftherealms.gfx;

import java.util.concurrent.Callable;

import static net.sagaoftherealms.android.sagaoftherealms.MainThread.eye;
import static net.sagaoftherealms.android.sagaoftherealms.MainThread.screenWidth;

/**
 * Created by summers on 11/19/15.
 */
public class DrawBackground implements Callable<Sprite> {

    public static final int BACKGROUND_WIDTH = 746;

    public static final int BACKGROUND_QUARTER_WIDTH = BACKGROUND_WIDTH / 4;


    public final int[] backgroundImage;
    public final int[] backgroundScreen;

    public DrawBackground(int[] pixels, int[] backgroundScreen) {
        this.backgroundImage = pixels;
        this.backgroundScreen = backgroundScreen;
    }


    @Override
    public Sprite call() {

        int left = eye.x;
        int right = left + screenWidth;
        int top = 0;
        int bottom = 320;

        int width = right - left;
        int height = bottom - top;

        if (width == 0 || height == 0) {
            return null;
        }


        for (int x = 0; x < screenWidth; x++) {
            for (int y = 0; y < bottom; y++) {
                int xIndex = x + left;
                if (xIndex < 0 || xIndex >= BACKGROUND_WIDTH) {
                    xIndex = 2 * BACKGROUND_WIDTH - xIndex - 1;
                }

                int index = screenWidth * y + x;
                int pixel = backgroundImage[(y * BACKGROUND_WIDTH) + xIndex];
                backgroundScreen[index] = pixel;

            }
        }

        return null;
    }


}
