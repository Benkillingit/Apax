# Apax Architecture Documentation

## Overview

Apax is designed with a clean three-layer architecture that maintains clear separation between Android OS integration, JNI bridging, and native core logic.

## Architecture Principles

### 1. Separation of Concerns
- **Android Layer**: Handles all OS integration, permissions, and UI
- **JNI Bridge**: Provides clean interface between Java and C++
- **Native Core**: Contains pure business logic with no Android dependencies

### 2. Security by Design
- No security bypasses or exploits
- All elevated access is user-granted
- Android OS enforces all permission boundaries
- Native core has no direct system access

### 3. Modularity
- Each layer can be developed and tested independently
- Clear interfaces between layers
- Easy to extend and maintain

## Layer Details

### Android Layer (Java)

**Package**: `com.apax.core`

#### Components

##### MainActivity
- **Purpose**: Primary UI for Apax status and control
- **Responsibilities**:
  - Display native core status
  - Handle user interactions
  - Manage ViewBinding
  - Lifecycle management
- **Security**: Normal app permissions only

##### ApaxCoreService (Foreground Service)
- **Purpose**: Persistent background operation
- **Responsibilities**:
  - Run continuously with user-visible notification
  - Initialize and manage native core
  - Handle service lifecycle
  - Future: Task scheduling, context monitoring
- **Security**: Requires FOREGROUND_SERVICE permission
- **Compliance**: Follows Android 8.0+ foreground service requirements

##### ApaxAccessibilityService
- **Purpose**: Context-aware assistance
- **Responsibilities**:
  - Monitor accessibility events (when enabled)
  - Provide context to native core
  - Assist with user interactions
  - Future: Smart automation, proactive suggestions
- **Security**: Requires explicit user authorization via Settings
- **Compliance**: Cannot be enabled programmatically

##### ApaxNativeBridge
- **Purpose**: Java-side JNI interface
- **Responsibilities**:
  - Load native library
  - Declare native methods
  - Provide type-safe Java API
- **Security**: No special privileges

### JNI Bridge Layer (C++)

**File**: `apax_jni_bridge.cpp`

#### Responsibilities
- Convert between JNI types (jstring, jboolean, etc.) and C++ types
- Manage native object lifecycle (singleton ApaxCore instance)
- Handle exceptions and error cases
- Expose native functionality to Java layer

#### Native Methods

```cpp
// Get Apax status string
Java_com_apax_core_MainActivity_apaxStatus()

// Initialize native core
Java_com_apax_core_native_1bridge_ApaxNativeBridge_initialize()

// Shutdown native core
Java_com_apax_core_native_1bridge_ApaxNativeBridge_shutdown()

// Get version information
Java_com_apax_core_native_1bridge_ApaxNativeBridge_getVersion()
```

#### Design Patterns
- **Singleton**: Single ApaxCore instance managed globally
- **RAII**: Automatic resource management with smart pointers
- **Exception Safety**: All JNI methods wrapped in try-catch

### Native Core Layer (C++)

**Files**: `apax_core.h`, `apax_core.cpp`

#### ApaxCore Class

```cpp
class ApaxCore {
public:
    ApaxCore();
    ~ApaxCore();

    bool initialize();
    void shutdown();
    ApaxStatus getStatus() const;
    std::string getStatusString() const;

    // Future expansion:
    // - processCommand(const std::string& command)
    // - executeTask(const Task& task)
    // - analyzeContext(const Context& context)

private:
    bool m_initialized;
    std::string m_version;
    // Future: ML models, task queues, etc.
};
```

#### Responsibilities
- Core reasoning and decision-making logic
- State management
- Task processing
- Future: ML inference, context analysis, pattern learning

#### Design Principles
- **No Android Dependencies**: Pure C++ with no JNI types
- **Testable**: Can be unit tested independently
- **Extensible**: Clear extension points for future features
- **Efficient**: Optimized for performance

## Data Flow

### Status Query Flow

```
User taps "Refresh" button
    ↓
MainActivity.updateStatus()
    ↓
MainActivity.apaxStatus() [native method]
    ↓
JNI Bridge: Java_com_apax_core_MainActivity_apaxStatus()
    ↓
ApaxCore::getStatusString()
    ↓
Return std::string
    ↓
JNI Bridge: Convert to jstring
    ↓
MainActivity: Display in TextView
```

### Service Initialization Flow

```
System starts ApaxCoreService
    ↓
ApaxCoreService.onCreate()
    ↓
ApaxNativeBridge.initialize()
    ↓
JNI Bridge: Java_com_apax_core_native_1bridge_ApaxNativeBridge_initialize()
    ↓
ApaxCore::initialize()
    ↓
Load models, initialize state
    ↓
Return success/failure
    ↓
Service starts foreground with notification
```

## Security Architecture

### Permission Enforcement

```
┌─────────────────────────────────────┐
│         User/Android OS             │
│  - Grants/denies permissions        │
│  - Enables/disables services        │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│         Android Layer               │
│  - Checks permissions               │
│  - Enforces security policies       │
│  - Validates all operations         │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│         JNI Bridge                  │
│  - No security enforcement          │
│  - Just type conversion             │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│         Native Core                 │
│  - No system access                 │
│  - Pure logic only                  │
│  - Trusts Android layer             │
└─────────────────────────────────────┘
```

### Security Boundaries

1. **Android OS**: Ultimate authority
   - Enforces all permissions
   - Controls service lifecycle
   - Manages app sandboxing

2. **Android Layer**: Permission checker
   - Requests permissions from user
   - Validates operations before calling native
   - Handles all privileged operations

3. **JNI Bridge**: Neutral translator
   - No security decisions
   - Just converts types

4. **Native Core**: Isolated logic
   - No direct system access
   - Cannot bypass Android security
   - Relies on Android layer for all I/O

## Extension Points

### Adding New Features

#### 1. New Native Method

**Step 1**: Add to ApaxCore (native core)
```cpp
// apax_core.h
std::string processCommand(const std::string& command);

// apax_core.cpp
std::string ApaxCore::processCommand(const std::string& command) {
    // Implementation
}
```

**Step 2**: Add JNI bridge method
```cpp
// apax_jni_bridge.cpp
extern "C" JNIEXPORT jstring JNICALL
Java_com_apax_core_native_1bridge_ApaxNativeBridge_processCommand(
    JNIEnv* env, jclass, jstring jCommand) {

    const char* command = env->GetStringUTFChars(jCommand, nullptr);
    std::string result = getApaxCore()->processCommand(command);
    env->ReleaseStringUTFChars(jCommand, command);
    return env->NewStringUTF(result.c_str());
}
```

**Step 3**: Add Java declaration
```java
// ApaxNativeBridge.java
public static native String processCommand(String command);
```

**Step 4**: Use in Android layer
```java
// MainActivity.java or Service
String result = ApaxNativeBridge.processCommand("do something");
```

#### 2. New Service

**Step 1**: Create service class
```java
public class NewService extends Service {
    // Implementation
}
```

**Step 2**: Declare in AndroidManifest.xml
```xml
<service android:name=".services.NewService" />
```

**Step 3**: Add required permissions
```xml
<uses-permission android:name="android.permission.REQUIRED_PERMISSION" />
```

#### 3. New Permission

**Step 1**: Declare in AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.NEW_PERMISSION" />
```

**Step 2**: Request at runtime (if dangerous)
```java
if (ContextCompat.checkSelfPermission(this, permission) != GRANTED) {
    ActivityCompat.requestPermissions(this, new String[]{permission}, CODE);
}
```

**Step 3**: Handle permission result
```java
@Override
public void onRequestPermissionsResult(int requestCode, ...) {
    // Handle result
}
```

## Future Architecture Enhancements

### 1. ML Model Integration
- Load TensorFlow Lite models in native core
- Inference in C++ for performance
- Model updates via Android layer

### 2. Task Scheduling System
- Priority queue in native core
- Android WorkManager integration
- Background task execution

### 3. Context Analysis Engine
- Collect context from accessibility service
- Analyze in native core
- Provide proactive suggestions

### 4. Communication Protocol
- Define structured message format (JSON/Protobuf)
- Bidirectional communication between layers
- Event-driven architecture

### 5. Plugin System
- Dynamic feature loading
- Modular capabilities
- User-installable extensions

## Performance Considerations

### Native Code Benefits
- **Speed**: C++ is faster than Java for compute-intensive tasks
- **Memory**: Better control over memory allocation
- **ML**: Native libraries (TensorFlow, ONNX) perform better
- **Battery**: More efficient for long-running operations

### Optimization Strategies
- Use native code for heavy computation
- Keep JNI calls minimal (overhead)
- Batch operations when possible
- Use appropriate data structures

## Testing Strategy

### Unit Tests
- **Native Core**: Test C++ logic independently
- **Android Layer**: Test Java components with JUnit
- **Integration**: Test JNI bridge with instrumented tests

### Test Structure
```
app/src/test/          # Unit tests (Java)
app/src/androidTest/   # Instrumented tests (Android)
app/src/main/cpp/test/ # Native unit tests (C++)
```

## Build System

### Gradle (Android)
- Manages Java compilation
- Handles dependencies
- Triggers native build

### CMake (Native)
- Compiles C++ code
- Links libraries
- Generates shared library (.so)

### Build Flow
```
Gradle sync
    ↓
CMake configure
    ↓
Compile C++ → libapax_core.so
    ↓
Compile Java → classes.dex
    ↓
Package APK
```

## Deployment

### Debug Build
- Includes debug symbols
- Verbose logging
- No obfuscation

### Release Build
- ProGuard/R8 obfuscation
- Optimized native code
- Minimal logging
- Signed APK

## Maintenance

### Code Organization
- Keep layers independent
- Document all public APIs
- Use consistent naming conventions
- Comment complex logic

### Version Control
- Separate commits for each layer
- Clear commit messages
- Tag releases
- Maintain changelog

### Documentation
- Keep README.md updated
- Document architecture changes
- Maintain API documentation
- Update comments
