/*
 * Apax Core - Native Reasoning Engine Header
 * Device: Walmart 100011886 Tablet
 * Target: ARM64-v8a (64-bit ARM architecture)
 *
 * ARCHITECTURE BOUNDARY:
 * - This is the NATIVE CORE layer - pure C++ logic
 * - NO Android dependencies here (no JNI types in this header)
 * - All Android interaction happens through apax_jni_bridge.cpp
 *
 * RESPONSIBILITY:
 * - Core decision-making and reasoning logic
 * - State management
 * - Command processing
 * - Future: ML inference, context analysis, task scheduling
 *
 * SECURITY:
 * - This layer has NO direct system access
 * - All privileged operations must go through Android layer
 * - Android enforces all permissions and security policies
 *
 * DEVICE OPTIMIZATION:
 * - Optimized for ARM64 architecture (Walmart 100011886 tablet)
 * - Efficient memory usage for tablet constraints
 * - No device-specific hardware dependencies
 */

#ifndef APAX_CORE_H
#define APAX_CORE_H

#include <string>
#include <memory>

namespace apax {

/**
 * Core status and state information
 */
struct ApaxStatus {
    bool initialized;
    std::string version;
    std::string status_message;
    int active_tasks;

    ApaxStatus() : initialized(false), version("1.0.0"),
                   status_message("Not initialized"), active_tasks(0) {}
};

/**
 * Main Apax Core Engine
 *
 * This class encapsulates the core reasoning and decision logic.
 * It is completely isolated from Android and communicates only
 * through the JNI bridge layer.
 */
class ApaxCore {
public:
    ApaxCore();
    ~ApaxCore();

    // Initialize the core engine
    bool initialize();

    // Shutdown and cleanup
    void shutdown();

    // Get current status
    ApaxStatus getStatus() const;

    // Get status as formatted string for display
    std::string getStatusString() const;

    // ============================================
    // CONTINUOUS I/O PROCESSING METHODS
    // ============================================

    /**
     * Process audio data from microphone
     * @param audioData Raw audio samples (PCM format)
     * @param length Number of samples
     * @param sampleRate Sample rate in Hz (e.g., 44100)
     * @return Processed result or analysis
     */
    std::string processAudioData(const int16_t* audioData, size_t length, int sampleRate);

    /**
     * Process video frame from camera
     * @param frameData Raw frame data (YUV or RGB format)
     * @param width Frame width in pixels
     * @param height Frame height in pixels
     * @param format Format identifier (0=YUV, 1=RGB, etc.)
     * @return Processed result or analysis
     */
    std::string processVideoFrame(const uint8_t* frameData, int width, int height, int format);

    /**
     * Process device information
     * @param deviceInfo JSON string with device data
     * @return Processed result or analysis
     */
    std::string processDeviceInfo(const std::string& deviceInfo);

    /**
     * Get continuous output for display
     * @return Current output string to display to user
     */
    std::string getContinuousOutput() const;

    // Future expansion points:
    // - processCommand(const std::string& command)
    // - executeTask(const Task& task)
    // - analyzeContext(const Context& context)
    // - updateModel(const ModelData& data)

private:
    bool m_initialized;
    std::string m_version;

    // Continuous I/O state
    std::string m_continuousOutput;
    int m_audioFramesProcessed;
    int m_videoFramesProcessed;

    // Helper methods for processing
    std::string analyzeAudio(const int16_t* audioData, size_t length, int sampleRate);
    std::string analyzeVideo(const uint8_t* frameData, int width, int height, int format);

    // Future: Add ML models, task queues, context analyzers, etc.
};

} // namespace apax

#endif // APAX_CORE_H
