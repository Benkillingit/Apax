/*
 * Apax Web Preview Bridge
 *
 * Exposes the REAL native core (app/src/main/cpp/apax_core.cpp) to the browser
 * preview as WebAssembly, so the app's reasoning core can be exercised in the
 * preview iframe - an Android APK cannot run in a browser.
 *
 * It deliberately mirrors app/src/main/cpp/apax_jni_bridge.cpp: same lazy
 * singleton that creates and initializes ApaxCore on first use, same one
 * function per native method the Java layer calls.
 *
 * This file is part of the preview harness only - it is never shipped in the APK.
 */

#include <memory>
#include <string>

#include "apax_core.h"

// Mirrors g_apaxCore / getApaxCore() in apax_jni_bridge.cpp
static std::unique_ptr<apax::ApaxCore> g_apaxCore = nullptr;

static apax::ApaxCore *getApaxCore() {
    if (!g_apaxCore) {
        g_apaxCore = std::make_unique<apax::ApaxCore>();
        g_apaxCore->initialize();
    }
    return g_apaxCore.get();
}

// Returned strings must outlive the call, exactly as NewStringUTF copies them.
static const char *keep(const std::string &value) {
    static std::string holder;
    holder = value;
    return holder.c_str();
}

extern "C" {

// Mirrors ApaxNativeBridge.initialize()
int apaxInitialize() {
    return getApaxCore()->initialize() ? 1 : 0;
}

// Mirrors ApaxNativeBridge.getVersion()
const char *apaxVersion() {
    return keep(getApaxCore()->getStatus().version);
}

// Mirrors MainActivity.apaxStatus()
const char *apaxStatusString() {
    return keep(getApaxCore()->getStatusString());
}

// Mirrors ApaxNativeBridge.getContinuousOutput()
const char *apaxContinuousOutput() {
    return keep(getApaxCore()->getContinuousOutput());
}

// Mirrors ApaxNativeBridge.processAudioData(short[], int)
const char *apaxProcessAudioData(const int16_t *samples, int length, int sampleRate) {
    if (!samples || length <= 0) {
        return keep("Error: Invalid audio data");
    }
    return keep(getApaxCore()->processAudioData(samples, static_cast<size_t>(length), sampleRate));
}

// Mirrors ApaxNativeBridge.processVideoFrame(byte[], int, int, int)
const char *apaxProcessVideoFrame(const uint8_t *frameData, int width, int height, int format) {
    if (!frameData || width <= 0 || height <= 0) {
        return keep("Error: Invalid video frame");
    }
    return keep(getApaxCore()->processVideoFrame(frameData, width, height, format));
}

} // extern "C"
