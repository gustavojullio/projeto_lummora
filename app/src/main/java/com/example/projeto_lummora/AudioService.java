package com.example.projeto_lummora;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class AudioService extends Service {
    private MediaPlayer mp = null;
    private int posicao;
    String acao;
    @Override
    public int onStartCommand(Intent it, int flags, int startId) {
        acao = it.getAction();
        try {
            if(!acao.isEmpty())
                play();
        } catch (Exception e) {
            Log.d("ERRO", "Erro serviço de audio " + e.getMessage());
        }


        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent it) { return null; }

    private void play() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(mp != null) {
                    if(!mp.isPlaying()) {
                        mp = acao.equals("INICIO")
                                ? MediaPlayer.create(getApplicationContext(), R.raw.comeco_estudo)
                                : MediaPlayer.create(getApplicationContext(), R.raw.final_estudo);
                        mp.setLooping(false);
                        mp.start();
                    }
                } else {
                    mp = acao.equals("INICIO")
                            ? MediaPlayer.create(getApplicationContext(), R.raw.comeco_estudo)
                            : MediaPlayer.create(getApplicationContext(), R.raw.final_estudo);
                    mp.setLooping(false);
                    mp.start();
                }



            }
        };
        new Handler().post(runnable);
    }

}
