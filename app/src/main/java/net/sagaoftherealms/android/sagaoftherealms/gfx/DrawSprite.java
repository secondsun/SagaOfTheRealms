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

        if ((eye.z + sprite.z) == 0 ) {
            return null;
        }

        int left = (eye.z * (sprite.x - spriteHalf - eye.x)) / (eye.z + sprite.z) + eye.x;
        int right = (eye.z * (sprite.x + spriteHalf - eye.x)) / (eye.z + sprite.z) + eye.x;
        int top = (eye.z * (sprite.y - spriteHalf - eye.y)) / (eye.z + sprite.z) + eye.y;
        int bottom = (eye.z * (sprite.y + spriteHalf - eye.y)) / (eye.z + sprite.z) + eye.y;

        int width = right-left;
        int height = bottom-top;

        if (width == 0 || height == 0) {
            return null;
        }

        int spriteSide = MainThread.spriteHalf * 2;
        int scaleRatio_x = ((spriteSide<<16)/(width)) +1;
        int scaleRatio_y = ((spriteSide<<16)/(height)) +1;

        for (int x = left; x < right; x++) {
            for (int y = top; y < bottom; y++) {
                if (x < 0 || x >= screenWidth || y < 0 || y >= screenHeight) {
                    continue;
                } else {
                    int index = screenWidth * y + x;
                    if (zBuffer[index] > sprite.z) {//You are in front, draw
                        zBuffer[index] = sprite.z;

                        int x2 = (((x - left)*scaleRatio_x)>>16) ;
                        int y2 = (((y - top)*scaleRatio_y)>>16);
                        int pixel = sprite.spriteArray[(y2 * spriteSide) + x2];
                        if (pixel>>24 != 0) {//not enough transparency
                            pixels[index] = pixel;
                        } else {//partially transparent
                            pixels[index] |= pixel;
                        }
                    } else if ( (pixels[index]>>24) == 0) {//If you are drawing behind a transparent pixel

                        int x2 = (((x - left)*scaleRatio_x)>>16) ;
                        int y2 = (((y - top)*scaleRatio_y)>>16);
                        int pixel = sprite.spriteArray[(y2 * spriteSide) + x2];
                        pixels[index] = pixel;
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

}
