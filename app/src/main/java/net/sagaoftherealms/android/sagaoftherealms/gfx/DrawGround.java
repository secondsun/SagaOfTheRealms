package net.sagaoftherealms.android.sagaoftherealms.gfx;

import android.util.Log;

import java.util.concurrent.Callable;

import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static net.sagaoftherealms.android.sagaoftherealms.MainThread.eye;
import static net.sagaoftherealms.android.sagaoftherealms.MainThread.halfScreenWidth;
import static net.sagaoftherealms.android.sagaoftherealms.MainThread.screenHeight;
import static net.sagaoftherealms.android.sagaoftherealms.MainThread.screenWidth;

/**
 * Created by summers on 11/19/15.
 */
public class DrawGround implements Callable<Sprite> {

    public static final int BACKGROUND_WIDTH = 256;

    public final int[] backgroundImage;
    public final int[] groundScreen;

    private int scrollOffset = 0;

    public DrawGround(int[] pixels, int[] backgroundScreen) {
        this.backgroundImage = pixels;
        this.groundScreen = backgroundScreen;
    }


    @Override
    public Sprite call() {
        scrollOffset-=5;

        int left = halfScreenWidth - (halfScreenWidth - eye.x)/ 8;
        int right = left + screenWidth;
        int top =  eye.y;
        int bottom = screenHeight;

        int width = right - left;
        int height = bottom - top;

        if (width == 0 || height == 0) {
            return null;
        }

        for (int x = 0; x < screenWidth; x++) {

            for (int y = 0; y < top; y++) {
                int index = screenWidth * y + x;
                groundScreen[index] = 0;
            }
        }


        for (int x = 0; x < screenWidth; x++) {

            for (int y = top; y < bottom; y++) {


                int xIndex = abs((int) ((left - x) * (bottom-y)>>6));
                xIndex = xIndex % BACKGROUND_WIDTH;
                int yIndex = abs(y+scrollOffset)% BACKGROUND_WIDTH;

                int index = screenWidth * y + x;
                try {

                    int pixel = backgroundImage[((yIndex) * BACKGROUND_WIDTH) + xIndex];
                    groundScreen[index] = pixel;
                } catch (ArrayIndexOutOfBoundsException ex) {
                    Log.e("Ground", ex.getMessage(),ex);
                }

            }
        }

        return null;
    }


}
