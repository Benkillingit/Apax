# Apax Developer Guide

A comprehensive guide for developers working on the Apax project.

## Table of Contents
1. [Getting Started](#getting-started)
2. [Architecture Deep Dive](#architecture-deep-dive)
3. [Development Workflow](#development-workflow)
4. [Adding Features](#adding-features)
5. [Best Practices](#best-practices)
6. [Debugging](#debugging)
7. [Testing](#testing)
8. [Performance](#performance)
9. [Security Guidelines](#security-guidelines)
10. [Common Patterns](#common-patterns)

---

## Getting Started

### Prerequisites
- Android Studio Otter 2 Feature Drop or later
- JDK 11 or later
- Android SDK 26+ (minimum), SDK 36 (target)
- NDK (latest stable version)
- CMake 3.22.1+
- Git (for version control)

### Initial Setup
```bash
# Clone or open the project
cd /path/to/MyApplication

# Open in Android Studio
# File > Open > Select project directory

# Sync Gradle
# File > Sync Project with Gradle Files

# Build
# Build > Make Project
```

### Verify Setup
```bash
# Check that native libraries built
ls app/build/intermediates/cmake/debug/obj/

# Should see directories for each ABI:
# - arm64-v8a/
# - armeabi-v7a/
# - x86/
# - x86_64/
```

---

## Architecture Deep Dive

### Layer Communication

```
┌─────────────────────────────────────────┐
│         Android Layer (Java)            │
│  - MainActivity                         │
│  - ApaxCoreService                      │
│  - ApaxAccessibilityService             │
│  - ApaxNativeBridge                     │
└─────────────────────────────────────────┘
                    ↕
        JNI Method Calls (via JNI Bridge)
                    ↕
┌─────────────────────────────────────────┐
│      JNI Bridge Layer (C++)             │
│  - apax_jni_bridge.cpp                  │
│  - Type conversion                      │
│  - Exception handling                   │
└─────────────────────────────────────────┘
                    ↕
        Function Calls (C++ to C++)
                    ↕
┌─────────────────────────────────────────┐
│       Native Core Layer (C++)           │
│  - apax_core.cpp                        │
│  - ApaxCore class                       │
│  - Business logic                       │
└─────────────────────────────────────────┘
```

### Data Flow Example: Status Query

```java
// 1. User taps "Refresh" button
binding.btnRefresh.setOnClickListener(v -> updateStatus());

// 2. MainActivity calls native method
private void updateStatus() {
    String status = apaxStatus();  // Native method
    binding.tvStatus.setText(status);
}

// 3. Native method declaration
public native String apaxStatus();
```

```cpp
// 4. JNI bridge receives call
extern "C" JNIEXPORT jstring JNICALL
Java_com_apax_core_MainActivity_apaxStatus(JNIEnv* env, jobject) {
    // 5. Get ApaxCore instance
    apax::ApaxCore* core = getApaxCore();

    // 6. Call native core method
    std::string statusStr = core->getStatusString();

    // 7. Convert to jstring and return
    return env->NewStringUTF(statusStr.c_str());
}
```

```cpp
// 8. Native core generates status
std::string ApaxCore::getStatusString() const {
    // Pure C++ logic
    std::ostringstream oss;
    oss << "Status: " << m_status;
    return oss.str();
}
```

---

## Development Workflow

### Daily Development Cycle

1. **Pull Latest Changes**
   ```bash
   git pull origin main
   ```

2. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make Changes**
   - Edit code
   - Add tests
   - Update documentation

4. **Build and Test**
   ```bash
   # Build
   ./gradlew assembleDebug

   # Run tests
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

5. **Commit Changes**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

6. **Push and Create PR**
   ```bash
   git push origin feature/your-feature-name
   ```

### Code Review Checklist
- [ ] Code follows project style
- [ ] All tests pass
- [ ] Documentation updated
- [ ] No new warnings
- [ ] Security considerations addressed
- [ ] Performance impact considered

---

## Adding Features

### Adding a New Native Method

#### Step 1: Define in Native Core
```cpp
// apax_core.h
class ApaxCore {
public:
    std::string processCommand(const std::string& command);
};

// apax_core.cpp
std::string ApaxCore::processCommand(const std::string& command) {
    LOGI("Processing command: %s", command.c_str());

    // Your logic here
    std::string result = "Command processed: " + command;

    return result;
}
```

#### Step 2: Add JNI Bridge Method
```cpp
// apax_jni_bridge.cpp
extern "C" JNIEXPORT jstring JNICALL
Java_com_apax_core_native_1bridge_ApaxNativeBridge_processCommand(
    JNIEnv* env,
    jclass,
    jstring jCommand) {

    // Convert jstring to C++ string
    const char* command = env->GetStringUTFChars(jCommand, nullptr);

    // Call native core
    apax::ApaxCore* core = getApaxCore();
    std::string result = core->processCommand(command);

    // Release Java string
    env->ReleaseStringUTFChars(jCommand, command);

    // Convert result to jstring
    return env->NewStringUTF(result.c_str());
}
```

#### Step 3: Declare in Java Bridge
```java
// ApaxNativeBridge.java
public class ApaxNativeBridge {
    public static native String processCommand(String command);
}
```

#### Step 4: Use in Android Layer
```java
// MainActivity.java or Service
String result = ApaxNativeBridge.processCommand("do something");
Log.i(TAG, "Result: " + result);
```

### Adding a New Service

#### Step 1: Create Service Class
```java
package com.apax.core.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NewService extends Service {
    private static final String TAG = "NewService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        // Your logic here
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
```

#### Step 2: Declare in Manifest
```xml
<!-- AndroidManifest.xml -->
<service
    android:name=".services.NewService"
    android:enabled="true"
    android:exported="false" />
```

#### Step 3: Add Required Permissions
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.REQUIRED_PERMISSION" />
```

#### Step 4: Start Service
```java
Intent serviceIntent = new Intent(this, NewService.class);
startService(serviceIntent);
```

### Adding a New Permission

#### Step 1: Declare in Manifest
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.NEW_PERMISSION" />
```

#### Step 2: Request at Runtime (if dangerous)
```java
// MainActivity.java
private static final int REQUEST_CODE = 100;

private void requestPermission() {
    if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.NEW_PERMISSION) != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.NEW_PERMISSION},
            REQUEST_CODE);
    }
}

@Override
public void onRequestPermissionsResult(int requestCode,
        String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == REQUEST_CODE) {
        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
            Log.i(TAG, "Permission granted");
        } else {
            // Permission denied
            Log.w(TAG, "Permission denied");
        }
    }
}
```

---

## Best Practices

### Java Code Style

```java
// Use meaningful names
private void updateApaxStatus() {  // Good
    // ...
}

private void update() {  // Bad - too vague
    // ...
}

// Use constants for magic values
private static final int NOTIFICATION_ID = 1001;  // Good
private static final String TAG = "ApaxService";

// Document public methods
/**
 * Initialize the Apax native core.
 *
 * @return true if initialization successful, false otherwise
 */
public boolean initialize() {
    // ...
}

// Use try-with-resources
try (FileInputStream fis = new FileInputStream(file)) {
    // Use fis
} catch (IOException e) {
    Log.e(TAG, "Error reading file", e);
}
```

### C++ Code Style

```cpp
// Use namespaces
namespace apax {
    class ApaxCore {
        // ...
    };
}

// Use RAII
std::unique_ptr<ApaxCore> core = std::make_unique<ApaxCore>();

// Use const correctness
std::string getStatus() const {  // Method doesn't modify state
    return m_status;
}

// Use logging
LOGI("Initializing ApaxCore v%s", m_version.c_str());

// Handle errors
if (!initialize()) {
    LOGE("Failed to initialize");
    return false;
}
```

### JNI Best Practices

```cpp
// Always release Java strings
const char* str = env->GetStringUTFChars(jStr, nullptr);
// Use str
env->ReleaseStringUTFChars(jStr, str);

// Check for exceptions
if (env->ExceptionCheck()) {
    env->ExceptionDescribe();
    env->ExceptionClear();
    return nullptr;
}

// Use try-catch for safety
try {
    // JNI operations
} catch (const std::exception& e) {
    LOGE("Exception: %s", e.what());
    return nullptr;
}
```

---

## Debugging

### Java Debugging

```java
// Set breakpoints in Android Studio
// Click in gutter next to line number

// Use Log statements
Log.d(TAG, "Debug message");
Log.i(TAG, "Info message");
Log.w(TAG, "Warning message");
Log.e(TAG, "Error message", exception);

// Use Android Studio debugger
// Debug > Debug 'app'
// Step through code with F8 (step over), F7 (step into)
```

### Native Debugging

```cpp
// Use Android logging
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Set breakpoints in C++ code
// Android Studio supports native debugging

// View native logs
adb logcat | grep "ApaxCore"
```

### Common Issues

#### Issue: Native Library Not Loading
```java
// Check library name matches
static {
    System.loadLibrary("apax_core");  // Must match CMakeLists.txt
}
```

#### Issue: JNI Method Not Found
```cpp
// Verify method signature matches exactly
// Java: public native String apaxStatus();
// C++: Java_com_apax_core_MainActivity_apaxStatus
```

#### Issue: Memory Leaks
```cpp
// Always release JNI references
jstring jStr = env->NewStringUTF("test");
// Use jStr
env->DeleteLocalRef(jStr);  // Clean up
```

---

## Testing

### Unit Tests (Java)

```java
// app/src/test/java/com/apax/core/
@Test
public void testNativeBridge() {
    String version = ApaxNativeBridge.getVersion();
    assertNotNull(version);
    assertEquals("1.0.0", version);
}
```

### Instrumented Tests (Android)

```java
// app/src/androidTest/java/com/apax/core/
@Test
public void testServiceStart() {
    Intent intent = new Intent(context, ApaxCoreService.class);
    context.startForegroundService(intent);

    // Verify service started
    // ...
}
```

### Native Tests (C++)

```cpp
// app/src/main/cpp/test/
#include "apax_core.h"
#include <cassert>

void testApaxCore() {
    apax::ApaxCore core;
    assert(core.initialize());
    assert(core.getStatus().initialized);
}
```

---

## Performance

### Optimization Tips

1. **Minimize JNI Calls**
   - JNI has overhead
   - Batch operations when possible
   - Pass complex data as JSON strings

2. **Use Native Code for Heavy Computation**
   - C++ is faster for algorithms
   - ML inference
   - Data processing

3. **Avoid Memory Allocations in Hot Paths**
   ```cpp
   // Bad - allocates every call
   std::string process() {
       return std::string("result");
   }

   // Good - reuse buffer
   void process(std::string& result) {
       result = "result";
   }
   ```

4. **Profile Before Optimizing**
   - Use Android Profiler
   - Identify bottlenecks
   - Measure improvements

---

## Security Guidelines

### Never Do This

```java
// ❌ Don't bypass Android security
// ❌ Don't use hidden APIs
// ❌ Don't enable services programmatically
// ❌ Don't collect data without consent
// ❌ Don't obfuscate malicious behavior
```

### Always Do This

```java
// ✅ Request permissions properly
if (checkSelfPermission(permission) != GRANTED) {
    requestPermissions(new String[]{permission}, code);
}

// ✅ Validate all inputs
if (input == null || input.isEmpty()) {
    throw new IllegalArgumentException("Invalid input");
}

// ✅ Handle sensitive data carefully
// Use Android Keystore for encryption keys
// Clear sensitive data after use

// ✅ Log security events
Log.i(TAG, "Permission granted: " + permission);
```

---

## Common Patterns

### Singleton Pattern (Native Core)

```cpp
static std::unique_ptr<apax::ApaxCore> g_apaxCore = nullptr;

static apax::ApaxCore* getApaxCore() {
    if (!g_apaxCore) {
        g_apaxCore = std::make_unique<apax::ApaxCore>();
        g_apaxCore->initialize();
    }
    return g_apaxCore.get();
}
```

### Observer Pattern (Service Communication)

```java
public interface ApaxStatusListener {
    void onStatusChanged(String status);
}

public class ApaxCoreService extends Service {
    private final List<ApaxStatusListener> listeners = new ArrayList<>();

    public void addListener(ApaxStatusListener listener) {
        listeners.add(listener);
    }

    private void notifyStatusChanged(String status) {
        for (ApaxStatusListener listener : listeners) {
            listener.onStatusChanged(status);
        }
    }
}
```

### Builder Pattern (Complex Objects)

```java
public class ApaxConfig {
    private final boolean enableLogging;
    private final int maxTasks;

    private ApaxConfig(Builder builder) {
        this.enableLogging = builder.enableLogging;
        this.maxTasks = builder.maxTasks;
    }

    public static class Builder {
        private boolean enableLogging = true;
        private int maxTasks = 10;

        public Builder enableLogging(boolean enable) {
            this.enableLogging = enable;
            return this;
        }

        public Builder maxTasks(int max) {
            this.maxTasks = max;
            return this;
        }

        public ApaxConfig build() {
            return new ApaxConfig(this);
        }
    }
}

// Usage
ApaxConfig config = new ApaxConfig.Builder()
    .enableLogging(true)
    .maxTasks(20)
    .build();
```

---

## Conclusion

This guide covers the essential aspects of developing with Apax. For more information:

- **README.md** - Project overview
- **ARCHITECTURE.md** - Detailed architecture
- **SECURITY.md** - Security guidelines
- **QUICKSTART.md** - Getting started

Happy coding! 🚀
