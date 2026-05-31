package com.wei.vocalgame03;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Effect extends GameObject {
	private int colCurrent = 0;
	private final int maxFrames;
	private boolean finished = false;

	// Forced duration for effects so low frame count effects still look good
	private static final long DURATION_MS = 2000;
	private final long startTimeMs;

	public Effect(Bitmap image, int rowCount, int colCount, float x, float y, float scaleFactor) {
		super(image, rowCount, colCount, x, y, scaleFactor);
		this.maxFrames = rowCount * colCount;
		this.startTimeMs = System.currentTimeMillis();
	}

	public void update() {
		if (finished) return;
		long elapsed = System.currentTimeMillis() - startTimeMs;
		if (elapsed > DURATION_MS) {
			finished = true;
			colCurrent = Math.max(0, maxFrames - 1);
			return;
		}

		double progress = (double) elapsed / (double) DURATION_MS; // 0.0 .. <1.0
		int frame = (int) Math.floor(progress * (double) maxFrames);
		if (frame < 0) frame = 0;
		if (frame >= maxFrames) frame = maxFrames - 1;
		colCurrent = frame;
	}

	public boolean isFinished() {
		return finished;
	}

	public void draw(Canvas canvas) {
		if (finished) return;
		int frameIndex = Math.max(0, Math.min(colCurrent, maxFrames - 1));
		int row = frameIndex / this.colCount;
		int col = frameIndex % this.colCount;
		Bitmap frame = createSubImageAt(row, col);

		int w = getWidth();
		int h = getHeight();

		Rect dst = new Rect(
				(int)(x - w / 2f),
				(int)(y - h / 2f),
				(int)(x + w / 2f),
				(int)(y + h / 2f)
		);

		if (frame != null) {
			//canvas.drawBitmap(frame, x - (float) getWidth() / 2, y - (float) getHeight() / 2, null);
			canvas.drawBitmap(frame, null, dst, null);
		}
	}

//	public void draw (Canvas canvas) {
//		Bitmap bitmap = this.getCurrentBitmap();
//		if (bitmap != null) {
//			int scaledWidth = (int) (getWidth() * scaleFactor);
//			int scaledHeight = (int) (getHeight() * scaleFactor);
//			Rect dest = new Rect((int)x, (int)y, (int)x + scaledWidth, (int)y + scaledHeight);
//			canvas.drawBitmap(bitmap, null, dest, null);
//		}
//	}
}


