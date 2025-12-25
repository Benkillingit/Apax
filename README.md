# Apax - Personal AI Assistant

A private, high-privilege personal assistant application for Android with a clean architecture separating Android interface from native reasoning core.

## 🎯 Walmart 100011886 Tablet Edition

This version of Apax is specifically optimized for the **Walmart 100011886 tablet**:
- ✅ **ARM64-v8a** native library (optimized for tablet's CPU)
- ✅ **Android 11+** (API 30+) target
- ✅ **1280x800** resolution UI optimization
- ✅ **Tablet-sized** touch targets and text
- ✅ **Device Owner** mode support

**📖 Quick Start**: See [WALMART_TABLET_GUIDE.md](WALMART_TABLET_GUIDE.md) for device-specific setup and configuration.
**🔧 Advanced Setup**: See [DEVICE_OWNER_SETUP.md](DEVICE_OWNER_SETUP.md) for Device Owner provisioning.

## 🏗️ Architecture

Apax follows a three-layer architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                   ANDROID LAYER (Java)                  │
│  - Permission Enforcement                               │
│  - UI Components (MainActivity)                         │
│  - Services (Foreground, Accessibility)                 │
│  - Android OS Integration                               │
└─────────────────────────────────────────────────────────┘
                          ↕ JNI Bridge
┌─────────────────────────────────────────────────────────┐
│                  JNI BRIDGE LAYER (C++)                 │
│  - Type Conversion (Java ↔ C++)                         │
│  - Native Method Implementations                        │
│  - Lifecycle Management                                 │
└─────────────────────────────────────────────────────────┘
                          ↕ Function Calls
┌─────────────────────────────────────────────────────────┐
│                 NATIVE CORE LAYER (C++)                 │
│  - Reasoning Engine                                     │
│  - Decision Making                                      │
│  - Task Processing                                      │
│  - ML Inference (Future)                                │
└─────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### Android Layer (Java)
- **Location**: `app/src/main/java/com/apax/core/`
- **Purpose**: Android OS interface and permission enforcement
- **Components**:
  - `MainActivity`: UI and status display
  - `ApaxCoreService`: Persistent foreground service
  - `ApaxAccessibilityService`: User-authorized accessibility features
  - `ApaxNativeBridge`: JNI interface wrapper

#### JNI Bridge Layer (C++)
- **Location**: `app/src/main/cpp/apax_jni_bridge.cpp`
- **Purpose**: Clean interface between Android and native code
- **Responsibilities**:
  - Convert between JNI types and C++ types
  - Manage native object lifecycle
  - Expose native functionality to Java

#### Native Core Layer (C++)
- **Location**: `app/src/main/cpp/apax_core.{h,cpp}`
- **Purpose**: Pure C++ reasoning and decision logic
- **Responsibilities**:
  - Core decision-making algorithms
  - State management
  - Task processing
  - Future: ML inference, context analysis

## 🔒 Security Model

Apax follows Android security best practices with **NO security bypasses**:

### Permission Model
- ✅ All permissions explicitly declared in AndroidManifest.xml
- ✅ User must approve all permissions
- ✅ No hidden APIs or undocumented behavior
- ✅ Android OS enforces all security boundaries

### Service Types

#### Foreground Service
- **Requires**: `FOREGROUND_SERVICE` permission
- **User Control**: Visible notification, can be stopped anytime
- **Compliance**: Follows Android 8.0+ foreground service requirements
- **Purpose**: Persistent operation for core functionality

#### Accessibility Service
- **Requires**: Explicit user authorization via Settings
- **User Control**: Must be manually enabled in Settings > Accessibility
- **Cannot**: Be enabled programmatically
- **Compliance**: All capabilities declared in XML configuration
- **Purpose**: Context-aware assistance when user-approved

#### Device Owner (Optional)
- **Requires**: ADB provisioning or NFC setup
- **User Control**: Requires factory reset or special setup
- **Cannot**: Be set programmatically by the app
- **Compliance**: Legitimate Android enterprise feature
- **Purpose**: Advanced device management (when needed)

## 🚀 Features

### Current Features
- ✅ Native C++ core with JNI interface
- ✅ ViewBinding for type-safe UI
- ✅ Foreground service for persistent operation
- ✅ Accessibility service structure (user must enable)
- ✅ Clean architecture with separation of concerns
- ✅ Comprehensive logging and debugging

### Future Expansion Points
- 🔮 ML model inference in native core
- 🔮 Context-aware task automation
- 🔮 Voice command processing
- 🔮 Smart notification management
- 🔮 Calendar and contact integration
- 🔮 Location-aware assistance
- 🔮 Device owner capabilities (when provisioned)

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/apax/core/
│   │   ├── MainActivity.java              # Main UI activity
│   │   ├── services/
│   │   │   ├── ApaxCoreService.java       # Foreground service
│   │   │   └── ApaxAccessibilityService.java  # Accessibility service
│   │   └── native_bridge/
│   │       └── ApaxNativeBridge.java      # JNI wrapper
│   ├── cpp/
│   │   ├── apax_core.h                    # Native core header
│   │   ├── apax_core.cpp                  # Native core implementation
│   │   ├── apax_jni_bridge.cpp            # JNI bridge implementation
│   │   └── CMakeLists.txt                 # Native build configuration
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml          # Main UI layout
│   │   ├── values/
│   │   │   └── strings.xml                # String resources
│   │   └── xml/
│   │       └── accessibility_service_config.xml  # Accessibility config
│   └── AndroidManifest.xml                # App manifest
└── build.gradle                           # App build configuration
```

## 🛠️ Building the Project

### Prerequisites
- Android Studio Otter 2 Feature Drop or later
- Android SDK 26+ (minimum)
- Android SDK 36 (target)
- NDK for native C++ compilation
- CMake 3.22.1+

### Build Steps

1. **Open in Android Studio**
   ```
   File > Open > Select project directory
   ```

2. **Sync Gradle**
   ```
   File > Sync Project with Gradle Files
   ```

3. **Build Native Libraries**
   ```
   Build > Make Project
   ```
   This will compile the C++ native libraries using CMake.

4. **Run on Device/Emulator**
   ```
   Run > Run 'app'
   ```

### Build Configuration

The project uses:
- **Namespace**: `com.apax.core`
- **Application ID**: `com.apax.core`
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36
- **Native Library**: `apax_core`
- **C++ Standard**: C++17

## 📱 Usage

### Initial Setup

1. **Install the app** on your device
2. **Grant permissions** when prompted (notifications, etc.)
3. **Open the app** to see Apax status

### Enabling Services

#### Foreground Service
- Start from MainActivity or programmatically
- Will show persistent notification
- Can be stopped via notification or app

#### Accessibility Service
1. Go to **Settings > Accessibility**
2. Find **Apax Accessibility Service**
3. Enable the service
4. Review and accept capabilities
5. Service will start automatically

### Using the App

- **Refresh Status**: Tap "Refresh Status" button
- **Initialize Core**: Tap "Initialize Core" button
- **View Logs**: Check Android Logcat for detailed logs

## 🔧 Development

### Adding New Features

#### Adding Native Methods

1. **Declare in Java**:
   ```java
   // In ApaxNativeBridge.java
   public static native String newMethod(String input);
   ```

2. **Implement in C++**:
   ```cpp
   // In apax_jni_bridge.cpp
   extern "C" JNIEXPORT jstring JNICALL
   Java_com_apax_core_native_1bridge_ApaxNativeBridge_newMethod(
       JNIEnv* env, jclass, jstring input) {
       // Implementation
   }
   ```

3. **Add to Core Logic**:
   ```cpp
   // In apax_core.cpp
   std::string ApaxCore::processNewFeature(const std::string& input) {
       // Core logic
   }
   ```

#### Adding Permissions

1. **Declare in AndroidManifest.xml**:
   ```xml
   <uses-permission android:name="android.permission.NEW_PERMISSION" />
   ```

2. **Request at runtime** (for dangerous permissions):
   ```java
   ActivityCompat.requestPermissions(this,
       new String[]{Manifest.permission.NEW_PERMISSION},
       REQUEST_CODE);
   ```

### Logging

The project uses Android's Log system:

- **Java**: `Log.i(TAG, "message")`
- **C++**: `__android_log_print(ANDROID_LOG_INFO, TAG, "message")`

View logs in Android Studio Logcat with filters:
- `ApaxMainActivity`
- `ApaxCoreService`
- `ApaxAccessibility`
- `ApaxCore`
- `ApaxJNI`

## ⚖️ Legal & Ethical Considerations

### Compliance
- ✅ Follows Android security guidelines
- ✅ All permissions explicitly declared
- ✅ No hidden functionality
- ✅ User consent required for all elevated access
- ✅ Respects user privacy

### Ethical Use
- This app has elevated privileges when services are enabled
- Must be used responsibly and transparently
- User must understand what they're authorizing
- Should provide genuine value to the user
- Must respect privacy laws and regulations

### Accessibility Service Ethics
- Accessibility services are powerful tools
- Should be used for legitimate assistance purposes
- Must respect user privacy and data
- Should not abuse elevated privileges
- User can disable at any time

## 📄 License

This is a personal project. Use responsibly and in compliance with all applicable laws and regulations.

## 🤝 Contributing

This is a personal assistant app. If you fork this project:
- Maintain security best practices
- Respect user privacy
- Follow Android guidelines
- Document all changes
- Test thoroughly

## 📞 Support

For issues or questions:
- Check Android Studio build output
- Review Logcat for error messages
- Ensure all permissions are granted
- Verify NDK and CMake are installed

## 🔄 Version History

### Version 1.0.0 (Current)
- Initial architecture implementation
- Native C++ core with JNI bridge
- Foreground service support
- Accessibility service structure
- ViewBinding implementation
- Comprehensive documentation
