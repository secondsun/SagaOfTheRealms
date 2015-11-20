package net.sagaoftherealms.android.sagaoftherealms.gfx;

import java.util.concurrent.Callable;

/**
 * Created by summers on 11/20/15.
 */
public class FillArray implements Callable<Sprite> {

    public int[] array = null;
    public int value = 0;

    @Override
    public Sprite call() throws Exception {
        int len = array.length;

        if (len > 0) {
            array[0] = value;
        }

        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
        return null;
    }
}
