package com.wei.vocalgame03;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class PlayerCharacter extends GameObject {
    private static final int SPRITE_COUNT = 6;
    private static final int STATE_IDLE = 0;
    private static final int ROW_IDLE = 0;
    private static final int COL_IDLE = 0;
    private static final int STATE_HIT = 1;
    private static final int ROW_HIT = 0;
    private static final int COL_HIT = 1;
    private static final int STATE_WALK = 2;
    private static final int ROW_WALK = 1;
    private static final int COL_WALK = 1;
    private int colCurrent = 0;
    private int stateCurrent = STATE_IDLE;
    private final Bitmap[] bmIdle;
    private final Bitmap[] bmHit;
    private final Bitmap[] bmWalk;
    private int health = 100;

    public PlayerCharacter(GameSurface gameSurface, Bitmap image, int x, int y, float scaleFactor) {
        super(image, 9, 54, x, y, scaleFactor);
        // 8 x 6 Col
        this.bmIdle = new Bitmap[SPRITE_COUNT];
        this.bmHit = new Bitmap[SPRITE_COUNT];
        this.bmWalk = new Bitmap[SPRITE_COUNT];


        for (int i = 0; i < SPRITE_COUNT; i++) {
            this.bmIdle[i] = this.createSubImageAt(ROW_IDLE, i*9+COL_IDLE);
            this.bmHit[i] = this.createSubImageAt(ROW_HIT, i*9+COL_HIT);
            this.bmWalk[i] = this.createSubImageAt(ROW_WALK, i*9+COL_WALK);
        }
    }


    public void takeDamage(int damage) {
        this.health -= damage;
        this.stateCurrent = STATE_HIT;
        this.colCurrent = 0;
    }



    public int getHealth() {
        return health;
    }

    public Bitmap[] getBitmaps() {
        switch(stateCurrent) {
            case STATE_IDLE:
                return this.bmIdle;
            case STATE_HIT:
                return this.bmHit;
            case STATE_WALK:
                return this.bmWalk;
            default:
                return null;
        }
    }

    public Bitmap getCurrentBitmap() {
        Bitmap[] bitmaps = this.getBitmaps();
        if (bitmaps == null || colCurrent >= bitmaps.length) return null;
        return bitmaps[this.colCurrent];
    }

    public void update() {
        this.colCurrent++;
        if (colCurrent >= SPRITE_COUNT) {
            colCurrent = 0;
            if (stateCurrent != STATE_IDLE)
            {
                stateCurrent = STATE_IDLE;
            }
        }
    }

    public void draw (Canvas canvas) {
        Bitmap bitmap = this.getCurrentBitmap();
        if (bitmap != null) {
            int scaledWidth = (int) (getWidth() * scaleFactor);
            int scaledHeight = (int) (getHeight() * scaleFactor);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false);
            canvas.drawBitmap(scaledBitmap, x - (float) scaledWidth /2, y - (float) scaledHeight /2 , null);
        }
    }
}
