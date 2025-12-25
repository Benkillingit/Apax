package com.apax.core.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.apax.core.MainActivity;
import com.apax.core.R;
import com.apax.core.native_bridge.ApaxNativeBridge;

/**
 * ApaxCoreService - Persistent Foreground Service
 *
 * ARCHITECTURE LAYER: Android Service Layer
 *
 * RESPONSIBILITY:
 * - Run Apax core persistently in the background
 * - Maintain foreground service with notification (Android requirement)
 * - Manage service lifecycle and native core initialization
 * - Handle service start/stop commands
 *
 * SECURITY MODEL:
 * - Runs as a foreground service (user-visible notification required)
 * - Requires FOREGROUND_SERVICE permission (user must install app)
 * - No hidden background execution - fully compliant with Android policies
 * - All privileged operations require explicit permissions
 *
 * ANDROID COMPLIANCE:
 * - Uses foreground service (visible to user via notification)
 * - Properly handles Android 8.0+ notification channels
 * - Follows Android background execution limits
 * - No WorkManager bypass or hidden execution
 *
 * FUTURE EXPANSION:
 * - Task scheduling and execution
 * - Context monitoring (with permissions)
 * - Communication with accessibility service
 * - ML model inference
 */
public class ApaxCoreService extends Service {

    private static final String TAG = "ApaxCoreService";
    private static final String CHANNEL_ID = "apax_core_service_channel";
    private static final int NOTIFICATION_ID = 1001;

    // Service state
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "ApaxCoreService onCreate");

        // Create notification channel (required for Android 8.0+)
        createNotificationChannel();

        // Initialize native core
        initializeNativeCore();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "ApaxCoreService onStartCommand");

        if (!isRunning) {
            // Start as foreground service with notification
            // This is REQUIRED by Android for long-running services
            startForeground(NOTIFICATION_ID, createNotification());
            isRunning = true;

            Log.i(TAG, "ApaxCoreService started as foreground service");
        }

        // START_STICKY: Service will be restarted if killed by system
        // This is standard Android behavior, not a security bypass
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "ApaxCoreService onDestroy");

        isRunning = false;

        // Note: We don't shutdown native core here because it might be
        // used by other components. It will be cleaned up when process dies.

        Log.i(TAG, "ApaxCoreService destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This is a started service, not a bound service
        // Return null to indicate we don't support binding
        return null;
    }

    /**
     * Initialize the native Apax core
     */
    private void initializeNativeCore() {
        try {
            Log.i(TAG, "Initializing native Apax core");
            boolean success = ApaxNativeBridge.initialize();

            if (success) {
                Log.i(TAG, "Native core initialized successfully");
                String version = ApaxNativeBridge.getVersion();
                Log.i(TAG, "Native core version: " + version);
            } else {
                Log.e(TAG, "Failed to initialize native core");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception initializing native core", e);
        }
    }

    /**
     * Create notification channel for Android 8.0+
     * This is REQUIRED by Android for foreground services
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.apax_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW  // Low importance = minimal intrusion
            );
            channel.setDescription(getString(R.string.apax_notification_channel_description));

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.i(TAG, "Notification channel created");
            }
        }
    }

    /**
     * Create the foreground service notification
     * This notification is REQUIRED by Android and must be visible to the user
     *
     * @return Notification object
     */
    private Notification createNotification() {
        // Intent to open MainActivity when notification is tapped
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.apax_service_name))
                .setContentText(getString(R.string.apax_service_description))
                .setSmallIcon(R.drawable.ic_launcher_foreground)  // Use app icon
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)  // Low priority = less intrusive
                .setOngoing(true);  // Cannot be dismissed by user swipe

        return builder.build();
    }

    // Future methods to add:
    // - public void processTask(Task task)
    // - public void updateContext(Context context)
    // - public void executeCommand(Command command)
    // - private void scheduleNextTask()
}
