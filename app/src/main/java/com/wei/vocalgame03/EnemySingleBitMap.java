package com.wei.vocalgame03;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class EnemySingleBitMap extends Enemy {
    private NPCStates stateCurrent = NPCStates.IDLE;
    private int colCurrent = 0;
    private Bitmap[] bmIdle;
    private Bitmap[] bmHit;
    private Bitmap[] bmWalk;
    private Bitmap[] bmAttack;
    private Bitmap[] bmDeath;
    private GameSurface gameSurface;
    private int framesIdle;
    private int framesHit;
    private int framesWalk;
    private int framesAttack;
    private int framesDeath;
    private int movingVectorX = -5;
    private int movingVectorY = 0;
    private long lastDrawnNanoTime =-1;
    private static final int TIME_DIVISION = 1000000;

    private int health;
    private float speed;
    private int damage;
    private String type;
    private int lightningPriority = 0;

    public EnemySingleBitMap(GameSurface gameSurface, Bitmap image, int maxRowCount, int maxColCount, float x, float y,
                             int health, float speed, int damage, String type,
                             int rowIdle, int rowHit, int rowWalk, int rowAttack, int rowDeath,
                             int framesIdle, int framesHit, int framesWalk, int framesAttack, int framesDeath,
                             int lightningPriority, float scaleFactor) {
        super(image, maxRowCount, maxColCount, x, y, scaleFactor);
        this.gameSurface = gameSurface;

        this.framesIdle = framesIdle;
        this.framesHit = framesHit;
        this.framesWalk = framesWalk;
        this.framesAttack = framesAttack;
        this.framesDeath = framesDeath;


        this.bmIdle = new Bitmap[framesIdle];
        this.bmHit = new Bitmap[framesHit];
        this.bmWalk = new Bitmap[framesWalk];
        this.bmAttack = new Bitmap[framesAttack];
        this.bmDeath = new Bitmap[framesDeath];

        this.health = health;
        this.speed = speed;
        this.damage = damage;
        this.type = type;
        this.lightningPriority = lightningPriority;


        for (int i = 0; i < framesIdle; i++) {
            this.bmIdle[i] = this.createSubImageAt(rowIdle, i);
        }
        for (int i = 0; i < framesHit; i++) {
            this.bmHit[i] = this.createSubImageAt(rowHit, i);
        }
        for (int i = 0; i < framesWalk; i++) {
            this.bmWalk[i] = this.createSubImageAt(rowWalk, i);
        }
        for (int i = 0; i < framesAttack; i++) {
            this.bmAttack[i] = this.createSubImageAt(rowAttack, i);
        }

        for (int i = 0; i < framesDeath; i++) {
            this.bmDeath[i] = this.createSubImageAt(rowDeath, i);
        }
    }

    public void setState(NPCStates state) {
        this.stateCurrent = state;
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        // Hit animation takes priority
        if (this.health <= 0) {
            this.stateCurrent = NPCStates.DEATH;
            this.colCurrent = 0;
        } else {
            this.stateCurrent = NPCStates.HIT;
            this.colCurrent = 0;
        }
    }

    @Override
    public int getLightningPriority() {
        return lightningPriority;
    }

    public int getHealth() {
        return health;
    }

    public void update() {
        PlayerCharacter player = gameSurface.getPlayer();

        // Determine State (if not in HIT priority)
        if (stateCurrent != NPCStates.HIT && stateCurrent != NPCStates.DEATH) {
            if (player != null && this.x <= player.getX() + player.getWidth()) {
                if (stateCurrent != NPCStates.ATTACK) {
                    stateCurrent = NPCStates.ATTACK;
                    colCurrent = 0;
                }
            } else {
                stateCurrent = NPCStates.WALK;
            }
        }

        // Animation Logic
        int maxFrames = 0;
        switch (stateCurrent)
        {
            case IDLE:
                maxFrames = framesIdle;
                break;
            case HIT:
                maxFrames = framesHit;
                break;
            case WALK:
                maxFrames = framesWalk;
                break;
            case ATTACK:
                maxFrames = framesAttack;
                break;
            case DEATH:
                maxFrames = framesDeath;
                break;
        }

        if (maxFrames > 0) {
            colCurrent++;
            if (colCurrent >= maxFrames) {
                colCurrent = 0;
                if (stateCurrent == NPCStates.HIT) {
                    stateCurrent = NPCStates.IDLE;
                } else if (stateCurrent == NPCStates.ATTACK) {
                    // Deal damage at the end of each attack animation cycle
                    if (player != null) {
                        player.takeDamage(this.damage);
                    }
                }
            }
        }

        // Position Logic
        long now = System.nanoTime();
        if (lastDrawnNanoTime == -1) {
            lastDrawnNanoTime = now;
        }
        float deltaTime = (float) (now - lastDrawnNanoTime) / TIME_DIVISION;
        lastDrawnNanoTime = now;

        // Move only when walking
        if (stateCurrent == NPCStates.WALK) {
            float dist = speed * deltaTime;
            double movingVectorLength = Math.sqrt(movingVectorX * movingVectorX + movingVectorY * movingVectorY);
            this.x = x + (float) (dist * movingVectorX / movingVectorLength);
            this.y = y + (float) (dist * movingVectorY / movingVectorLength);

            if (this.x < 0) {
                this.x = 0;
            }
            if (this.y < 0) {
                this.y = 0;
            }

            // Don't walk through the player
            if (player != null && this.x < player.getX() + player.getWidth()) {
                this.x = player.getX() + player.getWidth();
            }
        }
    }
    
    public Bitmap[] getBitmaps() {
        switch(stateCurrent) {
            case IDLE:
                return this.bmIdle;
            case HIT:
                return this.bmHit;
            case WALK:
                return this.bmWalk;
            case ATTACK:
                return this.bmAttack;
            case DEATH:
                return this.bmDeath;
            default:
                return null;
        }
    }

    public Bitmap getCurrentBitmap() {
        Bitmap[] bitmaps = this.getBitmaps();
        if (bitmaps == null || bitmaps.length == 0) return null;
        if (colCurrent >= bitmaps.length) {
            colCurrent = 0;
        }
        return bitmaps[this.colCurrent];
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
