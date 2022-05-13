package com.example.smartcarapp;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

//This class will be used later to execute commands in parallel
public class MultiThread extends Thread {
    int audioFile;
    Context context;

    public MultiThread(Context context, int audioFile){
        this.audioFile = audioFile;
        this.context = context;
    }
    @Override
    public void run() {

    }
    public void playAudio() throws InterruptedException {
        final MediaPlayer mp3 = MediaPlayer.create(context,audioFile);
        mp3.start();
    }


}
