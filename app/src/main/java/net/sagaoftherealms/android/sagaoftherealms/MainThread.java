package net.sagaoftherealms.android.sagaoftherealms;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.SurfaceHolder;

import net.sagaoftherealms.android.sagaoftherealms.gfx.DrawBackground;
import net.sagaoftherealms.android.sagaoftherealms.gfx.DrawSprite;
import net.sagaoftherealms.android.sagaoftherealms.gfx.FillArray;
import net.sagaoftherealms.android.sagaoftherealms.gfx.Sprite;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.currentTimeMillis;


/**
 * @author impaler
 *         <p/>
 *         The Main thread which contains the game loop. The thread must have access to
 *         the surface view and holder to trigger events every game tick.
 */
public class MainThread extends Thread {

    private static final String TAG = MainThread.class.getSimpleName();
    private static  final int SPRITE_COUNT = 150;

    public static final int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels / 4;
    public static final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels / 4;

    public static final int halfScreenWidth = screenWidth / 2;
    public static final int halfScreenHeight = screenHeight / 2;

    public static final Sprite eye = new Sprite();

    public static final int spriteHalf = 32;

    private final int[] rockPixels;

    private static final Matrix scale = new Matrix();


    static {
        eye.x = halfScreenWidth;
        eye.y = halfScreenHeight;
        eye.z = 100;

        scale.setScale(Resources.getSystem().getDisplayMetrics().widthPixels / screenWidth, Resources.getSystem().getDisplayMetrics().heightPixels / screenHeight);

    }

    private final ExecutorService threadPool = Executors.newFixedThreadPool(4);

    private final CompletionService<Sprite> pool = new ExecutorCompletionService<>(threadPool);
    private final Deque<DrawSprite> drawDeque = new ArrayDeque<>(SPRITE_COUNT);

    private final int[] backgroundImage = new int[746*160];
    private final int[] backgroundLayerPixels = new int[screenWidth * screenHeight];
    private final DrawBackground drawBackground = new DrawBackground(backgroundImage, backgroundLayerPixels);
    private final FillArray fillPixels;
    private final FillArray fillZBuffer;



    // Surface holder that can access the physical surface
    private SurfaceHolder surfaceHolder;

    public Bitmap backgroundLayer;
    public Bitmap spritesLayer;

    private HashSet<Sprite> sprites = new HashSet<>(SPRITE_COUNT);
    // flag to hold game state
    private boolean running;
    private final int[] zBuffer;
    private final int[] spriteLayerPixels;

    public void setRunning(boolean running) {
        this.running = running;
    }

    public MainThread(SurfaceHolder surfaceHolder, MainGamePanel gamePanel) throws IOException {
        super();
        this.surfaceHolder = surfaceHolder;

        rockPixels = new int[64*64];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeStream(gamePanel.getResources().getAssets().open("rock.png"),null,  options).getPixels(rockPixels, 0, 64, 0, 0, 64, 64);

        backgroundLayer = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);

        BitmapFactory.decodeStream(gamePanel.getResources().getAssets().open("background_1.png"),null,  options).getPixels(backgroundImage , 0, 746, 0, 0, 746, 160);


        for (int i = 0; i < SPRITE_COUNT; i++) {
            createSprite(i);
            drawDeque.push(new DrawSprite());
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
    public void run() {
        long tickCount = 0L;
        Log.d(TAG, "Starting game loop");
        while (running) {
            long millis = currentTimeMillis();
            tickCount++;

            //Handles input
            handleInput();

            //Updates game world (IE NPC states and positions)
            updateGameWorld();



            //render steps
            clearScreen();
            render();
            swap();
            eye.normalTheta+=.05;
            Log.d(TAG, String.format("FPS %d", (1000/(currentTimeMillis() - millis))));

        }
        Log.d(TAG, "Game loop executed " + tickCount + " times");
    }

    private void handleInput() {
        eye.x += ((eye.x + eye.speedX) > 0 && (eye.x + eye.speedX) < halfScreenWidth * 2) ? eye.speedX : 0;
        eye.y += ((eye.y + eye.speedY) > 0 && (eye.y + eye.speedY) < halfScreenHeight * 2) ? eye.speedY : 0;
        eye.z = 100;//Eye does not move
    }

    private void clearScreen() {
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
    }

    private void updateGameWorld() {
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

    private void render() {
        if (!surfaceHolder.getSurface().isValid()) {
            return;
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    private void swap() {
        Canvas canvas = surfaceHolder.getSurface().lockHardwareCanvas();
        canvas.drawARGB(255, 0, 0, 0);

        backgroundLayer.setPixels(backgroundLayerPixels, 0, screenWidth, 0, 0, screenWidth, screenHeight);
        canvas.drawBitmap(backgroundLayer, scale, null);
        spritesLayer.setPixels(spriteLayerPixels, 0, screenWidth, 0, 0, screenWidth, screenHeight);
        canvas.drawBitmap(spritesLayer, scale, null);
        surfaceHolder.getSurface().unlockCanvasAndPost(canvas);
    }


}