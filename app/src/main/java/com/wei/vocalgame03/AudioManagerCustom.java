package com.wei.vocalgame03;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class AudioManagerCustom {

    private static AudioManagerCustom instance;
    private final SoundPool soundPool;
    private final Context context;
    private int soundIdFireball;
    private int soundIdLightning;
    private MediaPlayer bgmPlayer;

    private float volumeLeft = 0.8f;
    private float volumeRight = 0.8f;

    private AudioManagerCustom(Context ctx) {
        context = ctx.getApplicationContext();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(8)
                .build();
        soundIdFireball = soundPool.load(context, R.raw.fireball, 1);
        soundIdLightning = soundPool.load(context, R.raw.lightning, 1);
    }

    public static AudioManagerCustom get(Context ctx) {
        if (instance == null) {
            instance = new AudioManagerCustom(ctx);
        }
        return instance;
    }

    public void playFireball() {
        soundPool.play(soundIdFireball, volumeLeft, volumeRight, 1, 0, 1f);
    }

    public void playLightning() {
        soundPool.play(soundIdLightning, volumeLeft, volumeRight, 1, 0, 1f);
    }

    public void playBgm(int resId) {
        stopBgm();

        bgmPlayer = MediaPlayer.create(context, resId);
        bgmPlayer.setLooping(true);
        bgmPlayer.setVolume(1f, 1f);
        bgmPlayer.start();
    }

    public void stopBgm() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.release();
            bgmPlayer = null;
        }
    }

    public void release() {
        soundPool.release();
        stopBgm();
    }
}
