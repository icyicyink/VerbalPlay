package com.wei.vocalgame03;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class BackgroundObject extends GameObject {
    private int state = 0;
    private int colCurrent = 0;

    private final Bitmap [] bmState0;

    private GameSurface gameSurface;

    private final int framesState0;

    public BackgroundObject(Bitmap image, int maxRowCount, int maxColCount, float x, float y, int framesState0, float scaleFactor) {
        super(image, maxRowCount, maxColCount, x, y, scaleFactor);
        this.framesState0 = framesState0;
        this.bmState0 = new Bitmap[framesState0];
        for (int i = 0; i < framesState0; i++) {
            this.bmState0[i] = this.createSubImageAt(0, i);
        }
    }

    public void update() {
        int maxFrames = framesState0;
        colCurrent++;
        if (colCurrent >= maxFrames) {
            colCurrent = 0;
        }
    }

    public Bitmap[] getBitmaps() {
        switch(state) {
            case 0:
                return this.bmState0;
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
