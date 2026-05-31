package com.wei.vocalgame03;

import android.graphics.Bitmap;

public abstract class GameObject {
    protected Bitmap image;
    protected final int rowCount;
    protected final int colCount;
    protected final int WIDTH;
    protected final int HEIGHT;
    protected final int width;
    protected final int height;
    protected float x;
    protected float y;

    protected float scaleFactor;

    public GameObject(Bitmap image, int maxRowCount, int maxColCount, float x, float y, float scaleFactor)  {
        this.image = image;
        this.rowCount= maxRowCount;
        this.colCount= maxColCount;

        this.x= x;
        this.y= y;

        this.scaleFactor = scaleFactor;

        this.WIDTH = image.getWidth();
        this.HEIGHT = image.getHeight();

        this.width = this.WIDTH/ maxColCount;
        this.height= this.HEIGHT/ maxRowCount;
    }

    protected Bitmap createSubImageAt(int row, int col)  {
        Bitmap subImage = Bitmap.createBitmap(image, col* width, row* height ,width,height);
        return subImage;
    }

    public float getX()  {
        return this.x;
    }
    public float getY()  {
        return this.y;
    }
    public int getHeight() {
        return height;
    }
    public int getWidth() {
        return width;
    }
}