package com.example.projeto_lummora;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;

public class AudioService extends Service {
    private MediaPlayer mp = null;
    private int posicao;

    @Override
    public int onStartCommand(Intent it, int flags, int startId) {
        play();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent it) { return null; }

    private void play() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(mp==null) {
                    mp = MediaPlayer.create(getApplicationContext(), R.raw.notificacao);
                    mp.setLooping(false);
                    mp.start();
                } else if (!mp.isPlaying()) {
                    mp.seekTo(0);
                    mp.start();
                }
            }
        };
        new Handler().post(runnable);
    }

}
