/*
 * Minimal <android/log.h> shim for the WebAssembly preview build.
 *
 * apax_core.cpp includes <android/log.h>, which only exists in the Android NDK.
 * This shim is supplied on the include path (via -I) so the REAL native core
 * source compiles unchanged for the browser preview. Log lines go to stderr,
 * which Emscripten forwards to the browser console.
 *
 * This file is part of the preview harness only - it is never shipped in the APK.
 */

#ifndef APAX_ANDROID_LOG_SHIM_H
#define APAX_ANDROID_LOG_SHIM_H

#include <stdarg.h>
#include <stdio.h>

#define ANDROID_LOG_INFO 4
#define ANDROID_LOG_ERROR 6

#ifdef __cplusplus
extern "C" {
#endif

static inline int __android_log_print(int prio, const char *tag, const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    int written = fprintf(stderr, prio == ANDROID_LOG_ERROR ? "[%s] E " : "[%s] I ", tag ? tag : "apax");
    written += vfprintf(stderr, fmt, args);
    written += fprintf(stderr, "\n");
    va_end(args);
    return written;
}

#ifdef __cplusplus
}
#endif

#endif /* APAX_ANDROID_LOG_SHIM_H */
