package com.wei.vocalgame03;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {

    private boolean running;
    private final GameSurface gameSurface;
    private final SurfaceHolder surfaceHolder;

    public GameThread(GameSurface gameSurface, SurfaceHolder surfaceHolder) {
        this.gameSurface = gameSurface;
        this.surfaceHolder = surfaceHolder;
    }

    @Override
    public void run() {
        long targetFPS = 15;
        long targetTime = 1000 / targetFPS; // Target time for each frame in milliseconds

        while (running) {
            long startTime = System.currentTimeMillis();
            Canvas canvas = null;
            try {
                // Get Canvas from Holder and lock it.
                canvas = this.surfaceHolder.lockCanvas();

                // Synchronized
                if (canvas != null) {
                    synchronized (canvas) {
                        this.gameSurface.update();
                        this.gameSurface.draw(canvas);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    this.surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

            long timeTaken = System.currentTimeMillis() - startTime;
            long waitTime = targetTime - timeTaken;

            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
