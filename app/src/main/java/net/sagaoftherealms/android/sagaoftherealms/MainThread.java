package net.sagaoftherealms.android.sagaoftherealms;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.SurfaceHolder;

import net.sagaoftherealms.android.sagaoftherealms.gfx.DrawSprite;
import net.sagaoftherealms.android.sagaoftherealms.gfx.FillArray;
import net.sagaoftherealms.android.sagaoftherealms.gfx.Sprite;

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

    public static final int screenWidth = 1920 / 4;
    public static final int screenHeight = 1080 / 4;

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

        scale.setScale(1920 / screenWidth, 1080 / screenHeight);

    }

    private final ExecutorService threadPool = Executors.newFixedThreadPool(4);

    private final CompletionService<Sprite> pool = new ExecutorCompletionService<>(threadPool);
    private final Deque<DrawSprite> drawDeque = new ArrayDeque<>(SPRITE_COUNT);
    private final FillArray fillPixels;
    private final FillArray fillZBuffer;
    // Surface holder that can access the physical surface
    private SurfaceHolder surfaceHolder;
    // The actual view that handles inputs
    // and draws to the surface
    private MainGamePanel gamePanel;

    public Bitmap back;

    private HashSet<Sprite> sprites = new HashSet<>(SPRITE_COUNT);
    // flag to hold game state
    private boolean running;
    private final int[] zBuffer;
    private final int[] pixels;

    public void setRunning(boolean running) {
        this.running = running;
    }

    public MainThread(SurfaceHolder surfaceHolder, MainGamePanel gamePanel) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;

        rockPixels = new int[64*64];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeResource(gamePanel.getResources(), R.drawable.rock, options).getPixels(rockPixels, 0, 64, 0, 0, 64, 64);

        for (int i = 0; i < SPRITE_COUNT; i++) {
            createSprite(i);
            drawDeque.push(new DrawSprite());
        }

        back = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);

        zBuffer = new int[screenWidth * screenHeight];
        pixels = new int[screenHeight * screenWidth];
        this.fillPixels = new FillArray();
        fillPixels.array = pixels;
        fillPixels.value = Color.TRANSPARENT;


        this.fillZBuffer = new FillArray();
        fillZBuffer.array = zBuffer;
        fillZBuffer.value = 1000;

    }


    private void createSprite(int i) {
        Sprite sprite = new Sprite();
        sprite.z = (int) (Math.random() * 1000);
        sprite.x = (int) (Math.random() * screenWidth);
        sprite.y = (int) (Math.random() * screenHeight);
        sprite.speedX = (int)(Math.random() * 20) - 10;
        sprite.speedY = (int)(Math.random() * 20) - 10;
        sprite.speedZ = (int) (Math.random() * 20) + 1;
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
        eye.x += eye.speedX;
        eye.y += eye.speedY;
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
            if (sprite.z > 1000) {
                sprite.z = 1;
                sprite.x = (int) (Math.random() * screenWidth);
                sprite.y = (int) (Math.random() * screenHeight);
                sprite.speedX = (int)(Math.random() * 10) - 5;
                sprite.speedY = (int)(Math.random() * 10) - 5;
                sprite.speedZ = (int) (Math.random() * 4) + 1;
                sprite.spriteArray = rockPixels;
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
            draw.pixels = pixels;
            pool.submit(draw);
            drawDeque.addLast(draw);

        }

        for (Sprite P : sprites) {

            try {
                pool.take().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

    }

    private void swap() {
        Canvas canvas = surfaceHolder.getSurface().lockHardwareCanvas();
        canvas.drawARGB(255, 0, 0, 0);
        back.setPixels(pixels, 0, screenWidth, 0, 0, screenWidth, screenHeight);

        canvas.drawBitmap(back, scale, null);
        surfaceHolder.getSurface().unlockCanvasAndPost(canvas);
    }


}