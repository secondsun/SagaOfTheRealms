package net.sagaoftherealms.android.sagaoftherealms.game;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.InputDevice;
import android.view.SurfaceHolder;

import net.sagaoftherealms.android.sagaoftherealms.MainGamePanel;
import net.sagaoftherealms.android.sagaoftherealms.gfx.DrawBackground;
import net.sagaoftherealms.android.sagaoftherealms.gfx.FillArray;
import net.sagaoftherealms.android.sagaoftherealms.gfx.Sprite;
import net.sagaoftherealms.android.sagaoftherealms.input.InputManagerCompat;
import net.sagaoftherealms.android.sagaoftherealms.input.JoyPadDelegate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WarpTunnelDemo extends AbstractScene {

    private final ExecutorService threadPool = Executors.newFixedThreadPool(4);

    private final CompletionService<Sprite> pool = new ExecutorCompletionService<>(threadPool);

    public static final int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels / 4;
    public static final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels / 4;

    public static final int halfScreenWidth = screenWidth / 2;
    public static final int halfScreenHeight = screenHeight / 2;

    public static final Sprite eye = new Sprite();

    private final int[] backgroundImage = new int[746*320];
    private final int[] backgroundLayerPixels = new int[screenWidth * screenHeight];

    private final int[] groundImage = new int[256*256];


    static {
        eye.x = halfScreenWidth;
        eye.y = halfScreenHeight;
        eye.z = 100;



    }

    private final DrawBackground drawBackground = new DrawBackground(backgroundImage, backgroundLayerPixels, eye);
    private FillArray fillPixels;
    private FillArray fillZBuffer;

    private int[] rockPixels;
    private Bitmap backgroundLayer;
    private Bitmap spritesLayer;
    private int[] zBuffer;
    private int[] spriteLayerPixels;

    public WarpTunnelDemo(SurfaceHolder surfaceHolder, MainGamePanel gamePanel) throws IOException
    {
        super(surfaceHolder, gamePanel);
    }

    @Override
    public void setup() throws IOException {

        super.setup();

        rockPixels = new int[64*64];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeStream(gamePanel.getResources().getAssets().open("rock.png"),null,  options).getPixels(rockPixels, 0, 64, 0, 0, 64, 64);

        backgroundLayer = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        BitmapFactory.decodeStream(gamePanel.getResources().getAssets().open("background_1.png"),null,  options).getPixels(backgroundImage , 0, 746, 0, 0, 746, 320);

        spritesLayer = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);

        zBuffer = new int[screenWidth * screenHeight];
        spriteLayerPixels = new int[screenHeight * screenWidth];

        this.fillPixels = new FillArray();
        fillPixels.array = spriteLayerPixels;
        fillPixels.value = Color.TRANSPARENT;


        this.fillZBuffer = new FillArray();
        fillZBuffer.array = zBuffer;
        fillZBuffer.value = 1000;

    }

    @Override
    public void teardown() {
        threadPool.shutdown();
        backgroundLayer.recycle();
        spritesLayer.recycle();
        super.teardown();
    }

    @Override
    public void handleInput() {
        eye.x += ((eye.x + eye.speedX) > 0 && (eye.x + eye.speedX) < halfScreenWidth * 2) ? eye.speedX : 0;
        eye.y += ((eye.y + eye.speedY) > 0 && (eye.y + eye.speedY) < halfScreenHeight * 2) ? eye.speedY : 0;
        eye.z = 100;//Eye does not move
    }

    @Override
    public void updateGameWorld() {
        eye.normalTheta+=.05;
    }

    @Override
    public List<Bitmap> render() {
        if (!surfaceHolder.getSurface().isValid()) {
            return new ArrayList<>(0);
        }

        pool.submit(fillPixels);
        pool.submit(fillZBuffer);
        try {
            pool.take().get();
            pool.take().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        pool.submit(drawBackground);
        try {
            pool.take().get();//take background
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        ArrayList<Bitmap> toReturn = new ArrayList<>(3);

        backgroundLayer.setPixels(backgroundLayerPixels, 0, screenWidth, 0, 0, screenWidth, screenHeight);
        spritesLayer.setPixels(spriteLayerPixels, 0, screenWidth, 0, 0, screenWidth, screenHeight);

        toReturn.add(backgroundLayer);
        toReturn.add(spritesLayer);
        return toReturn;

    }

    @Override
    public JoyPadDelegate createJoypadDelegate() {
        return new CameraJoypadDelegate(eye, 15);
    }

    /*
     * Remove any ship associated with the ID.
     * @see
     * com.example.inputmanagercompat.InputManagerCompat.InputDeviceListener
     * #onInputDeviceRemoved(int)
     */
    @Override
    public void onInputDeviceRemoved(int deviceId) {
        super.onInputDeviceRemoved(deviceId);
        eye.speedX = eye.speedY = eye.speedZ = 0;
        eye.x = halfScreenWidth;
        eye.y = halfScreenWidth;
        eye.z = 100;
    }

}
