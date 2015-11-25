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
import net.sagaoftherealms.android.sagaoftherealms.MainThread;
import net.sagaoftherealms.android.sagaoftherealms.gfx.DrawBackground;
import net.sagaoftherealms.android.sagaoftherealms.gfx.DrawGround;
import net.sagaoftherealms.android.sagaoftherealms.gfx.DrawSprite;
import net.sagaoftherealms.android.sagaoftherealms.gfx.FillArray;
import net.sagaoftherealms.android.sagaoftherealms.gfx.Sprite;
import net.sagaoftherealms.android.sagaoftherealms.input.InputManagerCompat;
import net.sagaoftherealms.android.sagaoftherealms.input.JoyPadDelegate;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CityDemo implements Scene,  InputManagerCompat.InputDeviceListener {

    private static  final int SPRITE_COUNT = 150;


    private final ExecutorService threadPool = Executors.newFixedThreadPool(4);

    private final CompletionService<Sprite> pool = new ExecutorCompletionService<>(threadPool);



    public static final int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels / 4;
    public static final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels / 4;

    public static final int halfScreenWidth = screenWidth / 2;
    public static final int halfScreenHeight = screenHeight / 2;

    public static final Sprite eye = new Sprite();

    public static final int spriteHalf = 32;

    private HashSet<Sprite> sprites = new HashSet<>(SPRITE_COUNT);

    private final Deque<DrawSprite> drawDeque = new ArrayDeque<>(SPRITE_COUNT);

    private final int[] backgroundImage = new int[746*320];
    private final int[] backgroundLayerPixels = new int[screenWidth * screenHeight];

    private final int[] groundImage = new int[256*256];
    private final int[] groundLayerPixels = new int[screenWidth * screenHeight];


    static {
        eye.x = halfScreenWidth;
        eye.y = halfScreenHeight;
        eye.z = 100;



    }

    private final DrawBackground drawBackground = new DrawBackground(backgroundImage, backgroundLayerPixels, eye);
    private final DrawGround drawGround = new DrawGround(groundImage, groundLayerPixels, halfScreenWidth, eye);
    private FillArray fillPixels;
    private FillArray fillZBuffer;

    private final SurfaceHolder surfaceHolder;
    private final MainGamePanel gamePanel;
    private int[] rockPixels;
    private Bitmap backgroundLayer;
    private Bitmap groundLayer;
    private Bitmap spritesLayer;
    private int[] zBuffer;
    private int[] spriteLayerPixels;
    private CameraJoypadDelegate shipJoypadDelegate;
    private InputManagerCompat mInputManager;

    public CityDemo(SurfaceHolder surfaceHolder, MainGamePanel gamePanel) throws IOException
    {
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    @Override
    public void setup() throws IOException {

        mInputManager = InputManagerCompat.Factory.getInputManager(gamePanel.getContext());
        mInputManager.registerInputDeviceListener(this, new Handler(Looper.getMainLooper()));

        rockPixels = new int[64*64];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeStream(gamePanel.getResources().getAssets().open("rock.png"),null,  options).getPixels(rockPixels, 0, 64, 0, 0, 64, 64);

        backgroundLayer = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        groundLayer = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);

        BitmapFactory.decodeStream(gamePanel.getResources().getAssets().open("background_1.png"),null,  options).getPixels(backgroundImage , 0, 746, 0, 0, 746, 320);
        BitmapFactory.decodeStream(gamePanel.getResources().getAssets().open("ground_1.png"),null,  options).getPixels(groundImage , 0, 256, 0, 0, 256, 256);


        for (int i = 0; i < SPRITE_COUNT; i++) {
            createSprite(i);
            drawDeque.push(new DrawSprite(spriteHalf, eye));
        }


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


    private void createSprite(int i) {
        Sprite sprite = new Sprite();
        sprite.z = 800;
        sprite.x = (int) (Math.random() * screenWidth);
        sprite.y = (int) (Math.random() * screenHeight);
        sprite.speedX = (int)(Math.random() * 20) - 10;
        sprite.speedY = (int)(Math.random() * 20) - 10;
        sprite.speedZ = -15;
        sprite.spriteIndex = rockPixels.hashCode();
        sprite.spriteArray = rockPixels;
        sprites.add(sprite);
    }


    @Override
    public void teardown() {
        threadPool.shutdown();
        backgroundLayer.recycle();
        spritesLayer.recycle();
        groundLayer.recycle();
        mInputManager.unregisterInputDeviceListener(this);
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
        for (Sprite sprite : sprites) {
            sprite.x += sprite.speedX;
            sprite.y += sprite.speedY;
            sprite.z += sprite.speedZ;
            if (sprite.z < 50) {
                sprite.z = 300;
                sprite.x = (int) (Math.random() * screenWidth);
                sprite.y = -1000;
                sprite.speedX = (int)(Math.random() * 20) - 10;
                sprite.speedY = (int)(Math.random() * 50) - 10;
                sprite.speedZ = -1;
            }
        }
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

        for (Sprite P : sprites) {

            DrawSprite draw = drawDeque.pop();
            draw.zBuffer = zBuffer;
            draw.sprite = P;
            draw.pixels = spriteLayerPixels;
            pool.submit(draw);
            drawDeque.addLast(draw);

        }
        pool.submit(drawBackground);
        pool.submit(drawGround);
        for (Sprite P : sprites) {

            try {
                pool.take().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
        try {
            pool.take().get();//take background
            pool.take().get();//take ground
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        ArrayList<Bitmap> toReturn = new ArrayList<>(3);

        backgroundLayer.setPixels(backgroundLayerPixels, 0, screenWidth, 0, 0, screenWidth, screenHeight);
        groundLayer.setPixels(groundLayerPixels, 0, screenWidth, 0, 0, screenWidth, screenHeight);
        spritesLayer.setPixels(spriteLayerPixels, 0, screenWidth, 0, 0, screenWidth, screenHeight);

        toReturn.add(backgroundLayer);
        toReturn.add(groundLayer);
        toReturn.add(spritesLayer);
        return toReturn;

    }

    @Override
    public JoyPadDelegate attachJoypadDelegate(JoyPadDelegate delegate) {
        shipJoypadDelegate = new CameraJoypadDelegate(eye, 15);
        return shipJoypadDelegate;
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
        shipJoypadDelegate.setInputDevice(dev);

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
        shipJoypadDelegate.setInputDevice(InputDevice.getDevice(deviceId));
    }

    /*
     * Remove any ship associated with the ID.
     * @see
     * com.example.inputmanagercompat.InputManagerCompat.InputDeviceListener
     * #onInputDeviceRemoved(int)
     */
    @Override
    public void onInputDeviceRemoved(int deviceId) {
        shipJoypadDelegate.setInputDevice(null);
        eye.speedX = eye.speedY = eye.speedZ = 0;
        eye.x = halfScreenWidth;
        eye.y = halfScreenWidth;
        eye.z = 100;
    }

}
