package com.wei.vocalgame03;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class EnemyFactory {

	public static Enemy createMushroom(GameSurface gs, float x, float y, float scaleFactor) {
		Bitmap idle = BitmapFactory.decodeResource(gs.getResources(), R.drawable.mushroomidle);
		Bitmap walk = BitmapFactory.decodeResource(gs.getResources(), R.drawable.mushroomrun);
		Bitmap hit = BitmapFactory.decodeResource(gs.getResources(), R.drawable.mushroomhit);
		Bitmap attack = BitmapFactory.decodeResource(gs.getResources(), R.drawable.mushroomattack);
		y = y - (float) 110;
		return new EnemyMultiBitMap(gs, idle, walk, hit, attack,
				1, 7, x, y,
				100, 0.06f, 5, "Mushroom",
				0,0,0,0,
				1,1,1,1,
				7,5,8,10, 10, scaleFactor);
	}

	public static Enemy createForestSlime(GameSurface gs, float x, float y, float scaleFactor) {
		Bitmap idle = BitmapFactory.decodeResource(gs.getResources(), R.drawable.slime1idle);
		Bitmap walk = BitmapFactory.decodeResource(gs.getResources(), R.drawable.slime1walk);
		Bitmap hit = BitmapFactory.decodeResource(gs.getResources(), R.drawable.slime1hit);
		Bitmap attack = BitmapFactory.decodeResource(gs.getResources(), R.drawable.slime1attack);

		y = y - (float) 50;
		return new EnemyMultiBitMap(gs, idle, walk, hit, attack,
				4, 6, x, y,
				100, 0.03f, 5, "Forest Slime",
				2,2,2,2,
				4,4,4,4,
				6,5,8,10, 0, scaleFactor);
	}

	public static Enemy createBossSlime (GameSurface gs, float x, float y, float scaleFactor) {
		Bitmap enemyBitmap = BitmapFactory.decodeResource(gs.getResources(), R.drawable.slimeboss);
		y = y - (float) 420;
		return new EnemySingleBitMap(gs, enemyBitmap, 5, 22, x, y,
				300, 0.02f, 25, "Slime Boss",
				0, 3, 1, 2, 4,
				6, 5, 12, 15, 22, 20, scaleFactor);
	}
}


