package com.apax.core.native_bridge;

/**
 * ApaxNativeBridge - JNI Interface to Native Core
 *
 * ARCHITECTURE BOUNDARY:
 * - This is the JAVA-side JNI interface
 * - Provides clean Java API to access native Apax core
 * - Handles library loading and native method declarations
 *
 * RESPONSIBILITY:
 * - Load native library (apax_core)
 * - Declare native methods
 * - Provide type-safe Java interface to native functionality
 *
 * SECURITY:
 * - This class has no special privileges
 * - All security enforcement happens in Android layer
 * - Native core has no direct system access
 */
public class ApaxNativeBridge {

    // Load the native library
    // This must match the library name in CMakeLists.txt
    static {
        System.loadLibrary("apax_core");
    }

    /**
     * Initialize the Apax native core
     *
     * @return true if initialization successful, false otherwise
     */
    public static native boolean initialize();

    /**
     * Shutdown the Apax native core
     * Should be called when app is being destroyed
     */
    public static native void shutdown();

    /**
     * Get the version of the Apax native core
     *
     * @return version string (e.g., "1.0.0")
     */
    public static native String getVersion();

    // ============================================
    // CONTINUOUS I/O NATIVE METHODS
    // ============================================

    /**
     * Process audio data from microphone
     *
     * SECURITY BOUNDARY:
     * - Audio data comes from Android layer with RECORD_AUDIO permission
     * - Native core only processes data, has no direct microphone access
     * - Android OS enforces all permission boundaries
     *
     * @param audioData Raw audio samples (PCM 16-bit)
     * @param sampleRate Sample rate in Hz (e.g., 44100)
     * @return Analysis result string
     */
    public static native String processAudioData(short[] audioData, int sampleRate);

    /**
     * Process video frame from camera
     *
     * SECURITY BOUNDARY:
     * - Video data comes from Android layer with CAMERA permission
     * - Native core only processes data, has no direct camera access
     * - Android OS enforces all permission boundaries
     *
     * @param frameData Raw frame data (YUV or RGB format)
     * @param width Frame width in pixels
     * @param height Frame height in pixels
     * @param format Format identifier (0=YUV, 1=RGB, etc.)
     * @return Analysis result string
     */
    public static native String processVideoFrame(byte[] frameData, int width, int height, int format);

    /**
     * Process device information
     *
     * @param deviceInfo JSON string with device data
     * @return Processing result string
     */
    public static native String processDeviceInfo(String deviceInfo);

    /**
     * Get continuous output for display
     *
     * @return Current output string to display to user
     */
    public static native String getContinuousOutput();

    // Future native methods to add:
    // public static native String processCommand(String command);
    // public static native boolean executeTask(String taskJson);
    // public static native String getContextAnalysis();
    // public static native boolean updateConfiguration(String configJson);
}
