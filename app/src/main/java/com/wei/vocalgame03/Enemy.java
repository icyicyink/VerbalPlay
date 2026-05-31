package com.wei.vocalgame03;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public abstract class Enemy extends GameObject {
    private int spawnTime = 0;
    public Enemy(Bitmap image, int maxRowCount, int maxColCount, float x, float y, float scaleFactor) {
        super(image, maxRowCount, maxColCount, x, y, scaleFactor);
    }

    public void setSpawnTime(int spawnTime){
        this.spawnTime = spawnTime;
    }
    public int getSpawnTime() {
        return spawnTime;
    }
    public abstract void update();
    public abstract void draw(Canvas canvas);
    public abstract int getHealth();
    public abstract void takeDamage(int damage);
    public abstract int getLightningPriority();
}
