package net.sagaoftherealms.android.sagaoftherealms.gfx;

import android.graphics.Color;
import android.util.Log;

import net.sagaoftherealms.android.sagaoftherealms.MainThread;

import static net.sagaoftherealms.android.sagaoftherealms.MainThread.*;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Created by summers on 11/19/15.
 */
public class DrawSprite implements Callable<Sprite> {

    public Sprite sprite;

    public int[] pixels;
    public int[] zBuffer;

    @Override
    public Sprite call() {
        checkAllNotNull();

        int left = (eye.z * (sprite.x - spriteHalf - eye.x)) / (eye.z + sprite.z) + eye.x;
        int right = (eye.z * (sprite.x + spriteHalf - eye.x)) / (eye.z + sprite.z) + eye.x;
        int top = (eye.z * (sprite.y - spriteHalf - eye.y)) / (eye.z + sprite.z) + eye.y;
        int bottom = (eye.z * (sprite.y + spriteHalf - eye.y)) / (eye.z + sprite.z) + eye.y;

        int width = right-left;
        int height = bottom-top;

        int spriteSide = MainThread.spriteHalf * 2;
        int scaleRatio_x = ((spriteSide<<16)/(width)) +1;
        int scaleRatio_y = ((spriteSide<<16)/(height)) +1;

        for (int x = left; x < right; x++) {
            for (int y = top; y < bottom; y++) {
                if (x < 0 || x >= screenWidth || y < 0 || y >= screenHeight) {
                    continue;
                } else {
                    int index = screenWidth * y + x;
                    if (zBuffer[index] > sprite.z) {
                        zBuffer[index] = sprite.z;

                        int x2 = (((x - left)*scaleRatio_x)>>16) ;
                        int y2 = (((y - top)*scaleRatio_y)>>16);
                        try {
                            pixels[index] = sprite.spriteArray[(y2 * spriteSide) + x2];
                        } catch(ArrayIndexOutOfBoundsException ex) {
                            Log.d("Darwing", String.format("AIOOBE %d,%d -> %d,%d",x,y,x2,y2));
                        }
                    }
                }
            }
        }


        this.sprite = null;
        pixels = null;
        zBuffer = null;

        return null;
    }

    private void checkAllNotNull() {
        if (sprite == null || zBuffer == null || pixels == null) {
            throw new RuntimeException("something was null");
        }
    }


    public int[] resizePixels(int[] pixels,int w1,int h1,int w2,int h2) {
        int[] temp = new int[w2*h2] ;
        // EDIT: added +1 to account for an early rounding problem
        int x_ratio = (int)((w1<<16)/w2) +1;
        int y_ratio = (int)((h1<<16)/h2) +1;
        //int x_ratio = (int)((w1<<16)/w2) ;
        //int y_ratio = (int)((h1<<16)/h2) ;
        int x2, y2 ;
        for (int i=0;i<h2;i++) {
            for (int j=0;j<w2;j++) {
                x2 = ((j*x_ratio)>>16) ;
                y2 = ((i*y_ratio)>>16) ;
                temp[(i*w2)+j] = pixels[(y2*w1)+x2] ;
            }
        }
        return temp ;
    }

}
