package com.apax.core.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.apax.core.R;
import com.apax.core.native_bridge.ApaxNativeBridge;

/**
 * ApaxAudioService - Continuous Microphone Input Service
 * Device: Walmart 100011886 Tablet
 *
 * ARCHITECTURE LAYER: Android Service Layer
 *
 * RESPONSIBILITY:
 * - Continuous microphone audio capture
 * - Send audio data to native core via JNI
 * - Run as foreground service with notification
 *
 * SECURITY MODEL:
 * - Requires RECORD_AUDIO permission (user must grant)
 * - Requires FOREGROUND_SERVICE permission
 * - Android OS enforces all permission boundaries
 * - Native core has no direct microphone access
 *
 * DEVICE OPTIMIZATION:
 * - Optimized for Walmart 100011886 tablet microphone
 * - Audio processing on background thread
 * - Efficient buffer management
 */
public class ApaxAudioService extends Service {

    private static final String TAG = "ApaxAudioService";
    private static final String CHANNEL_ID = "ApaxAudioChannel";
    private static final int NOTIFICATION_ID = 3;

    // Audio configuration
    private static final int SAMPLE_RATE = 44100; // 44.1 kHz
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private Thread recordingThread;
    private boolean isRecording = false;

    private int bufferSize;
    private int framesProcessed = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "ApaxAudioService onCreate - Walmart 100011886 Tablet");

        // Create notification channel
        createNotificationChannel();

        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification());

        // Initialize audio recording
        initializeAudioRecording();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "ApaxAudioService started");
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "ApaxAudioService onDestroy");
        stopRecording();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Initialize audio recording
     *
     * SECURITY BOUNDARY:
     * - This method requires RECORD_AUDIO permission
     * - Permission must be granted by user at runtime
     * - Android OS enforces permission check
     */
    private void initializeAudioRecording() {
        try {
            // Calculate buffer size
            bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT
            );

            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "Invalid buffer size");
                return;
            }

            // Create AudioRecord instance
            // SECURITY NOTE: Requires RECORD_AUDIO permission
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize * 2
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord not initialized");
                audioRecord = null;
            } else {
                Log.i(TAG, "AudioRecord initialized: buffer size = " + bufferSize);
            }

        } catch (SecurityException e) {
            Log.e(TAG, "RECORD_AUDIO permission not granted", e);
            audioRecord = null;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing AudioRecord", e);
            audioRecord = null;
        }
    }

    /**
     * Start audio recording
     */
    private void startRecording() {
        if (audioRecord == null) {
            Log.e(TAG, "Cannot start recording: AudioRecord not initialized");
            return;
        }

        if (isRecording) {
            Log.w(TAG, "Already recording");
            return;
        }

        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                recordAudio();
            }
        }, "AudioRecordingThread");

        recordingThread.start();
        Log.i(TAG, "Audio recording started");
    }

    /**
     * Stop audio recording
     */
    private void stopRecording() {
        isRecording = false;

        if (recordingThread != null) {
            try {
                recordingThread.join(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping recording thread", e);
            }
            recordingThread = null;
        }

        if (audioRecord != null) {
            try {
                if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop();
                }
                audioRecord.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing AudioRecord", e);
            }
            audioRecord = null;
        }

        Log.i(TAG, "Audio recording stopped");
    }

    /**
     * Record audio and send to native core
     *
     * ARCHITECTURE BOUNDARY:
     * - Receives audio from Android AudioRecord API
     * - Sends audio to native core via JNI
     * - Native core processes and returns analysis
     */
    private void recordAudio() {
        if (audioRecord == null) {
            return;
        }

        try {
            audioRecord.startRecording();
            Log.i(TAG, "AudioRecord started recording");

            short[] audioBuffer = new short[bufferSize];

            while (isRecording) {
                // Read audio data
                int samplesRead = audioRecord.read(audioBuffer, 0, bufferSize);

                if (samplesRead > 0) {
                    // Send audio to native core for processing
                    // SECURITY NOTE: Native core only processes data, has no microphone access
                    String result = ApaxNativeBridge.processAudioData(
                            audioBuffer,
                            SAMPLE_RATE
                    );

                    framesProcessed++;

                    if (framesProcessed % 100 == 0) {
                        Log.i(TAG, "Processed audio frame " + framesProcessed + ": " + result);
                    }

                } else if (samplesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG, "AudioRecord ERROR_INVALID_OPERATION");
                    break;
                } else if (samplesRead == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "AudioRecord ERROR_BAD_VALUE");
                    break;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error during audio recording", e);
        } finally {
            if (audioRecord != null && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                try {
                    audioRecord.stop();
                } catch (Exception e) {
                    Log.e(TAG, "Error stopping AudioRecord", e);
                }
            }
        }
    }

    /**
     * Create notification channel for foreground service
     */
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Apax Audio Service",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Continuous audio capture for Apax");

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Create notification for foreground service
     */
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Apax Audio Active")
                .setContentText("Processing microphone input")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
}
