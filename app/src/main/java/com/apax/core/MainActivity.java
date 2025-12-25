package com.apax.core;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import com.apax.core.databinding.ActivityMainBinding;
import com.apax.core.native_bridge.ApaxNativeBridge;
import com.apax.core.services.ApaxCameraService;
import com.apax.core.services.ApaxAudioService;

/**
 * MainActivity - Apax Control Interface with Continuous I/O
 * Device: Walmart 100011886 Tablet
 * Target: Android 11+ (API 30+)
 *
 * ARCHITECTURE LAYER: Android UI Layer
 *
 * RESPONSIBILITY:
 * - Display Apax status and continuous I/O output
 * - Provide user interface for Apax control
 * - Handle runtime permissions (camera, microphone)
 * - Start/stop continuous I/O services
 * - Handle Android lifecycle events
 * - Manage ViewBinding for UI components
 *
 * SECURITY MODEL:
 * - This activity runs with normal app permissions
 * - All elevated access requires explicit user grants
 * - Camera and microphone permissions requested at runtime
 * - No security bypasses or hidden APIs
 * - Android OS enforces all permission boundaries
 * - Works within stock Android APIs (no root required)
 *
 * ARCHITECTURE NOTES:
 * - Android Layer (Java): Permission enforcement, UI, Services
 * - JNI Bridge: Clean interface between layers
 * - Native Core (C++): Reasoning, decisions, task processing
 *
 * CONTINUOUS I/O FLOW:
 * - Camera/Audio services capture data (with user permission)
 * - Services send data to native core via JNI
 * - Native core processes and returns analysis
 * - MainActivity displays continuous output
 *
 * DEVICE COMPATIBILITY:
 * - Optimized for Walmart 100011886 tablet
 * - UI scaled for 1280x800 resolution
 * - Touch-optimized for tablet interaction
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ApaxMainActivity";

    // Permission request codes
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Required permissions for continuous I/O
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    // Load Apax native core library (compiled for ARM64-v8a)
    // This library is optimized for Walmart 100011886 tablet
    static {
        System.loadLibrary("apax_core");
        Log.i(TAG, "Loaded apax_core native library for ARM64 architecture");
    }

    // ViewBinding for type-safe view access
    private ActivityMainBinding binding;

    // Service state
    private boolean isCameraServiceRunning = false;
    private boolean isAudioServiceRunning = false;

    // Handler for periodic UI updates
    private Handler updateHandler;
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "MainActivity onCreate - Walmart 100011886 Tablet Edition with Continuous I/O");

        // Inflate UI using ViewBinding (Android best practice)
        // Layout optimized for 1280x800 tablet resolution
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize UI
        setupUI();

        // Check and request permissions
        checkPermissions();

        // Display initial Apax status
        updateStatus();

        // Setup periodic updates for continuous output
        setupPeriodicUpdates();
    }

    /**
     * Setup UI components and event handlers
     */
    private void setupUI() {
        // Refresh button to update status
        binding.btnRefresh.setOnClickListener(v -> {
            Log.i(TAG, "Refresh button clicked");
            updateStatus();
        });

        // Initialize button to explicitly initialize core
        binding.btnInitialize.setOnClickListener(v -> {
            Log.i(TAG, "Initialize button clicked");
            boolean success = ApaxNativeBridge.initialize();
            if (success) {
                Log.i(TAG, "Apax core initialized successfully");
                Toast.makeText(this, "Apax core initialized", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Failed to initialize Apax core");
                Toast.makeText(this, "Failed to initialize core", Toast.LENGTH_SHORT).show();
            }
            updateStatus();
        });

        // Start Camera button
        binding.btnStartCamera.setOnClickListener(v -> {
            Log.i(TAG, "Start Camera button clicked");
            if (checkPermissions()) {
                startCameraService();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        });

        // Stop Camera button
        binding.btnStopCamera.setOnClickListener(v -> {
            Log.i(TAG, "Stop Camera button clicked");
            stopCameraService();
        });

        // Start Audio button
        binding.btnStartAudio.setOnClickListener(v -> {
            Log.i(TAG, "Start Audio button clicked");
            if (checkPermissions()) {
                startAudioService();
            } else {
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show();
            }
        });

        // Stop Audio button
        binding.btnStopAudio.setOnClickListener(v -> {
            Log.i(TAG, "Stop Audio button clicked");
            stopAudioService();
        });
    }

    /**
     * Update the status display with current Apax information
     */
    private void updateStatus() {
        try {
            // Get status from native core via JNI
            String status = apaxStatus();
            binding.tvStatus.setText(status);

            // Get version info
            String version = ApaxNativeBridge.getVersion();
            binding.tvVersion.setText("Native Core Version: " + version);

            // Get continuous output
            String continuousOutput = ApaxNativeBridge.getContinuousOutput();
            binding.tvContinuousOutput.setText(continuousOutput);

            // Update service status
            updateServiceStatus();

            Log.i(TAG, "Status updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating status", e);
            binding.tvStatus.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Update service status indicators
     */
    private void updateServiceStatus() {
        String cameraStatus = isCameraServiceRunning ? "Camera: RUNNING" : "Camera: STOPPED";
        String audioStatus = isAudioServiceRunning ? "Audio: RUNNING" : "Audio: STOPPED";
        binding.tvServiceStatus.setText(cameraStatus + " | " + audioStatus);
    }

    /**
     * Setup periodic updates for continuous output display
     */
    private void setupPeriodicUpdates() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateStatus();
                updateHandler.postDelayed(this, 1000); // Update every second
            }
        };
        updateHandler.post(updateRunnable);
    }

    /**
     * Check if all required permissions are granted
     *
     * SECURITY BOUNDARY:
     * - Checks for CAMERA and RECORD_AUDIO permissions
     * - These are dangerous permissions requiring runtime grants
     * - Android OS enforces permission checks
     *
     * @return true if all permissions granted, false otherwise
     */
    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permissions
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Log.i(TAG, "All permissions granted");
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "Some permissions denied");
                Toast.makeText(this, "Permissions required for continuous I/O", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Start camera service for continuous video capture
     *
     * SECURITY NOTE:
     * - Requires CAMERA permission (checked before calling)
     * - Service runs in foreground with notification
     * - Android OS enforces permission boundaries
     */
    private void startCameraService() {
        Intent intent = new Intent(this, ApaxCameraService.class);
        startForegroundService(intent);
        isCameraServiceRunning = true;
        updateServiceStatus();
        Toast.makeText(this, "Camera service started", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Camera service started");
    }

    /**
     * Stop camera service
     */
    private void stopCameraService() {
        Intent intent = new Intent(this, ApaxCameraService.class);
        stopService(intent);
        isCameraServiceRunning = false;
        updateServiceStatus();
        Toast.makeText(this, "Camera service stopped", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Camera service stopped");
    }

    /**
     * Start audio service for continuous microphone input
     *
     * SECURITY NOTE:
     * - Requires RECORD_AUDIO permission (checked before calling)
     * - Service runs in foreground with notification
     * - Android OS enforces permission boundaries
     */
    private void startAudioService() {
        Intent intent = new Intent(this, ApaxAudioService.class);
        startForegroundService(intent);
        isAudioServiceRunning = true;
        updateServiceStatus();
        Toast.makeText(this, "Audio service started", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Audio service started");
    }

    /**
     * Stop audio service
     */
    private void stopAudioService() {
        Intent intent = new Intent(this, ApaxAudioService.class);
        stopService(intent);
        isAudioServiceRunning = false;
        updateServiceStatus();
        Toast.makeText(this, "Audio service stopped", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Audio service stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "MainActivity onDestroy");

        // Stop periodic updates
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }

        // Note: We don't shutdown the core here because it might be used by services
        // The core will be shutdown when the app process is destroyed
    }

    /**
     * Native method to get Apax status
     * Implemented in apax_jni_bridge.cpp
     *
     * DEVICE NOTE:
     * - This JNI call is optimized for ARM64 architecture
     * - Native library compiled specifically for Walmart 100011886 tablet
     *
     * @return Formatted status string from native core
     */
    public native String apaxStatus();
}
