package net.sagaoftherealms.android.sagaoftherealms.game;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import net.sagaoftherealms.android.sagaoftherealms.MainGamePanel;
import net.sagaoftherealms.android.sagaoftherealms.input.JoyPadDelegate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;
import static net.sagaoftherealms.android.sagaoftherealms.MainThread.screenHeight;
import static net.sagaoftherealms.android.sagaoftherealms.MainThread.screenWidth;

/**
 * Created by summers on 4/23/16.
 */
public class StrategyMapScene extends AbstractScene {

    private static final int   TILE_SIDE = 16;
    private static final int[] LAND_TILE = {3,1};
    private static final int[] FLOWER_TILE = {4,1};

    private static final int[] CHARACTER_TILE_DOWN = {0,0};
    private static final int[] CHARACTER_TILE_LEFT = {0,1};
    private static final int[] CHARACTER_TILE_RIGHT = {0,2};
    private static final int[] CHARACTER_TILE_UP = {0,3};


    private static final int[] ENEMY_TILE_DOWN = {0,0};
    private static final int[] ENEMY_TILE_LEFT = {0,1};
    private static final int[] ENEMY_TILE_RIGHT = {0,2};
    private static final int[] ENEMY_TILE_UP = {0,3};


    private static final String CHARACTERS_PATH = "characters.png";
    private static final String LAND_PATH = "basictiles.png";

    private int[] playerPos = {0,0};
    private int[] enemyPos = {18,18};

    private int[] boardDimens = {32,32};
    private int[] playerPixels;
    private int[] enemyPixels;
    private int[] landPixels;
    private Bitmap backgroundLayer;

    public StrategyMapScene(SurfaceHolder surfaceHolder, MainGamePanel gamePanel) {
        super(surfaceHolder, gamePanel);
    }

    @Override
    public void setup() throws IOException {
        super.setup();

        playerPixels = new int[48*64];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeStream(gamePanel.getResources().getAssets().open(CHARACTERS_PATH),null,  options).getPixels(playerPixels, 0, 48, 0, 0, 48, 64);

        enemyPixels = new int[48*64];
        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeStream(gamePanel.getResources().getAssets().open(CHARACTERS_PATH),null,  options).getPixels(enemyPixels, 0, 48, 64, 0, 48, 64);

        landPixels = new int[32*16];
        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeStream(gamePanel.getResources().getAssets().open(LAND_PATH),null,  options).getPixels(landPixels, 0, 32, 48, 16, 32, 16);

        backgroundLayer = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);


    }

    @Override
    public void teardown() {
        super.teardown();
    }

    @Override
    public void handleInput() {

    }

    @Override
    public void updateGameWorld() {

    }

    @Override
    public List<Bitmap> render() {

        if (!surfaceHolder.getSurface().isValid()) {
            return new ArrayList<>(0);
        }


        List<Bitmap> toReturn = new ArrayList<>(1);

        for (int x = 0; x < screenWidth/16; x++) {
            for (int y = 0; y < screenHeight/16; y++) {

                if (Math.random()*100 > 95) {
                    backgroundLayer.setPixels(landPixels, 16, 32, x * 16, y * 16, 16, 16);
                } else {
                    backgroundLayer.setPixels(landPixels, 0, 32, x * 16, y * 16, 16, 16);
                }
            }
        }

        backgroundLayer.setPixels(this.playerPixels, 0, 48, 0, 0, 48, 64);


        toReturn.add(backgroundLayer);
        return toReturn;
    }

    @Override
    public JoyPadDelegate createJoypadDelegate() {
        return new JoyPadDelegate() {
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                return false;
            }

            @Override
            public boolean onGenericMotionEvent(MotionEvent event) {
                return false;
            }

            @Override
            public boolean onKeyUp(int keyCode, KeyEvent event) {
                return false;
            }

            @Override
            public void setInputDevice(InputDevice dev) {

            }
        };
    }

}
