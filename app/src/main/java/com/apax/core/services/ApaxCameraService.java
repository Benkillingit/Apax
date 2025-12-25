package com.apax.core.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.apax.core.R;
import com.apax.core.native_bridge.ApaxNativeBridge;

public class ApaxCameraService extends Service {

    private static final String TAG = "ApaxCameraService";
    private static final String CHANNEL_ID = "ApaxCameraChannel";
    private static final int NOTIFICATION_ID = 2;

    private Thread processingThread;
    private boolean isProcessing = false;
    private int framesProcessed = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        startFrameProcessing();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopFrameProcessing();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startFrameProcessing() {
        if (isProcessing) return;
        isProcessing = true;

        processingThread = new Thread(() -> processFrames(), "CameraProcessingThread");
        processingThread.start();
    }

    private void stopFrameProcessing() {
        isProcessing = false;
        if (processingThread != null) {
            try {
                processingThread.join(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping thread", e);
            }
        }
    }

    private void processFrames() {
        int width = 1280, height = 720, format = 0;

        while (isProcessing) {
            try {
                byte[] frameData = new byte[width * height];
                for (int i = 0; i < frameData.length; i++)
                    frameData[i] = (byte)(128 + (Math.random()*50 -25));

                String result = ApaxNativeBridge.processVideoFrame(frameData, width, height, format);
                framesProcessed++;
                if(framesProcessed % 30 == 0)
                    Log.i(TAG,"Processed frame " + framesProcessed + ": " + result);

                Thread.sleep(33);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Apax Camera Service",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Continuous camera capture for Apax");

        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(manager != null) manager.createNotificationChannel(channel);
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Apax Camera Active")
                .setContentText("Processing video frames")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
}
