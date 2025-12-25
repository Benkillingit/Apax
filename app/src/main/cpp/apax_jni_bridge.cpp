/*
 * Apax JNI Bridge - Android ↔ Native Interface
 * Device: Walmart 100011886 Tablet (ARM64-v8a)
 *
 * ARCHITECTURE BOUNDARY:
 * - This is the BRIDGE layer between Android (Java) and Native (C++)
 * - Handles JNI marshalling and type conversion
 * - Maintains the singleton ApaxCore instance
 *
 * RESPONSIBILITY:
 * - Convert between JNI types and C++ types
 * - Manage native object lifecycle
 * - Expose native functionality to Java layer
 *
 * SECURITY NOTE:
 * - This layer does NOT enforce security
 * - Security is enforced by Android layer (permissions, etc.)
 * - Native core has no direct system access
 *
 * DEVICE COMPATIBILITY:
 * - Compiled for ARM64 architecture (Walmart 100011886 tablet)
 * - Efficient JNI calls optimized for tablet performance
 */

#include <jni.h>
#include <string>
#include <memory>
#include <android/log.h>
#include "apax_core.h"

#define LOG_TAG "ApaxJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global singleton instance of ApaxCore
// In a production app, you might want more sophisticated lifecycle management
static std::unique_ptr<apax::ApaxCore> g_apaxCore = nullptr;

/**
 * Helper function to get or create the ApaxCore instance
 */
static apax::ApaxCore* getApaxCore() {
    if (!g_apaxCore) {
        LOGI("Creating new ApaxCore instance");
        g_apaxCore = std::make_unique<apax::ApaxCore>();
        g_apaxCore->initialize();
    }
    return g_apaxCore.get();
}

/*
 * JNI Method: apaxStatus
 *
 * Called from: com.apax.core.MainActivity.apaxStatus()
 * Returns: String containing current Apax status
 *
 * This is the main entry point from the Android layer to query
 * the native core's status.
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_apax_core_MainActivity_apaxStatus(
        JNIEnv* env,
        jobject /* this */) {

    LOGI("apaxStatus() called from Java layer");

    try {
        apax::ApaxCore* core = getApaxCore();
        std::string statusStr = core->getStatusString();
        return env->NewStringUTF(statusStr.c_str());
    } catch (const std::exception& e) {
        LOGE("Exception in apaxStatus: %s", e.what());
        std::string errorMsg = "Error: ";
        errorMsg += e.what();
        return env->NewStringUTF(errorMsg.c_str());
    }
}

/*
 * JNI Method: initializeApaxCore
 *
 * Called from: com.apax.core.native_bridge.ApaxNativeBridge.initialize()
 * Returns: boolean indicating success
 *
 * Explicitly initializes the Apax core. This can be called from
 * a service or other component to ensure the core is ready.
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_com_apax_core_native_1bridge_ApaxNativeBridge_initialize(
        JNIEnv* env,
        jclass /* class */) {

    LOGI("initializeApaxCore() called from Java layer");

    try {
        apax::ApaxCore* core = getApaxCore();
        return core->initialize() ? JNI_TRUE : JNI_FALSE;
    } catch (const std::exception& e) {
        LOGE("Exception in initializeApaxCore: %s", e.what());
        return JNI_FALSE;
    }
}

/*
 * JNI Method: shutdownApaxCore
 *
 * Called from: com.apax.core.native_bridge.ApaxNativeBridge.shutdown()
 *
 * Cleanly shuts down the Apax core. Should be called when the app
 * is being destroyed or the service is stopping.
 */
extern "C" JNIEXPORT void JNICALL
Java_com_apax_core_native_1bridge_ApaxNativeBridge_shutdown(
        JNIEnv* env,
        jclass /* class */) {

    LOGI("shutdownApaxCore() called from Java layer");

    try {
        if (g_apaxCore) {
            g_apaxCore->shutdown();
            g_apaxCore.reset();
        }
    } catch (const std::exception& e) {
        LOGE("Exception in shutdownApaxCore: %s", e.what());
    }
}

/*
 * JNI Method: getApaxVersion
 *
 * Called from: com.apax.core.native_bridge.ApaxNativeBridge.getVersion()
 * Returns: String containing version number
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_apax_core_native_1bridge_ApaxNativeBridge_getVersion(
        JNIEnv* env,
        jclass /* class */) {

    try {
        apax::ApaxCore* core = getApaxCore();
        apax::ApaxStatus status = core->getStatus();
        return env->NewStringUTF(status.version.c_str());
    } catch (const std::exception& e) {
        LOGE("Exception in getApaxVersion: %s", e.what());
        return env->NewStringUTF("Error");
    }
}

/*
 * ============================================
 * CONTINUOUS I/O JNI METHODS
 * ============================================
 */

/*
 * JNI Method: processAudioData
 *
 * Called from: Java layer with audio samples from microphone
 * Returns: String containing audio analysis result
 *
 * SECURITY NOTE:
 * - Audio data comes from Android layer with RECORD_AUDIO permission
 * - Native core only processes data, has no direct microphone access
 * - Android OS enforces permission boundaries
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_apax_core_native_1bridge_ApaxNativeBridge_processAudioData(
        JNIEnv* env,
        jclass /* class */,
        jshortArray audioData,
        jint sampleRate) {

    LOGI("processAudioData() called from Java layer");

    try {
        apax::ApaxCore* core = getApaxCore();

        // Get audio data from Java array
        jsize length = env->GetArrayLength(audioData);
        jshort* samples = env->GetShortArrayElements(audioData, nullptr);

        if (!samples) {
            LOGE("Failed to get audio samples");
            return env->NewStringUTF("Error: Failed to get audio data");
        }

        // Process audio data
        std::string result = core->processAudioData(
            reinterpret_cast<const int16_t*>(samples),
            static_cast<size_t>(length),
            static_cast<int>(sampleRate)
        );

        // Release audio data
        env->ReleaseShortArrayElements(audioData, samples, JNI_ABORT);

        return env->NewStringUTF(result.c_str());
    } catch (const std::exception& e) {
        LOGE("Exception in processAudioData: %s", e.what());
        std::string errorMsg = "Error: ";
        errorMsg += e.what();
        return env->NewStringUTF(errorMsg.c_str());
    }
}

/*
 * JNI Method: processVideoFrame
 *
 * Called from: Java layer with video frame from camera
 * Returns: String containing video analysis result
 *
 * SECURITY NOTE:
 * - Video data comes from Android layer with CAMERA permission
 * - Native core only processes data, has no direct camera access
 * - Android OS enforces permission boundaries
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_apax_core_native_1bridge_ApaxNativeBridge_processVideoFrame(
        JNIEnv* env,
        jclass /* class */,
        jbyteArray frameData,
        jint width,
        jint height,
        jint format) {

    LOGI("processVideoFrame() called from Java layer: %dx%d", width, height);

    try {
        apax::ApaxCore* core = getApaxCore();

        // Get frame data from Java array
        jsize length = env->GetArrayLength(frameData);
        jbyte* pixels = env->GetByteArrayElements(frameData, nullptr);

        if (!pixels) {
            LOGE("Failed to get frame data");
            return env->NewStringUTF("Error: Failed to get frame data");
        }

        // Process video frame
        std::string result = core->processVideoFrame(
            reinterpret_cast<const uint8_t*>(pixels),
            static_cast<int>(width),
            static_cast<int>(height),
            static_cast<int>(format)
        );

        // Release frame data
        env->ReleaseByteArrayElements(frameData, pixels, JNI_ABORT);

        return env->NewStringUTF(result.c_str());
    } catch (const std::exception& e) {
        LOGE("Exception in processVideoFrame: %s", e.what());
        std::string errorMsg = "Error: ";
        errorMsg += e.what();
        return env->NewStringUTF(errorMsg.c_str());
    }
}

/*
 * JNI Method: processDeviceInfo
 *
 * Called from: Java layer with device information
 * Returns: String containing processing result
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_apax_core_native_1bridge_ApaxNativeBridge_processDeviceInfo(
        JNIEnv* env,
        jclass /* class */,
        jstring deviceInfo) {

    LOGI("processDeviceInfo() called from Java layer");

    try {
        apax::ApaxCore* core = getApaxCore();

        // Convert Java string to C++ string
        const char* infoStr = env->GetStringUTFChars(deviceInfo, nullptr);
        if (!infoStr) {
            LOGE("Failed to get device info string");
            return env->NewStringUTF("Error: Failed to get device info");
        }

        std::string info(infoStr);
        env->ReleaseStringUTFChars(deviceInfo, infoStr);

        // Process device info
        std::string result = core->processDeviceInfo(info);

        return env->NewStringUTF(result.c_str());
    } catch (const std::exception& e) {
        LOGE("Exception in processDeviceInfo: %s", e.what());
        std::string errorMsg = "Error: ";
        errorMsg += e.what();
        return env->NewStringUTF(errorMsg.c_str());
    }
}

/*
 * JNI Method: getContinuousOutput
 *
 * Called from: Java layer to get current output for display
 * Returns: String containing current continuous output
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_apax_core_native_1bridge_ApaxNativeBridge_getContinuousOutput(
        JNIEnv* env,
        jclass /* class */) {

    try {
        apax::ApaxCore* core = getApaxCore();
        std::string output = core->getContinuousOutput();
        return env->NewStringUTF(output.c_str());
    } catch (const std::exception& e) {
        LOGE("Exception in getContinuousOutput: %s", e.what());
        return env->NewStringUTF("Error getting output");
    }
}

// Future JNI methods to add:
// - processCommand(String command) -> String result
// - executeTask(String taskJson) -> boolean success
// - getContextAnalysis() -> String analysisJson
// - updateConfiguration(String configJson) -> boolean success
