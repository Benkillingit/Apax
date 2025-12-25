# Apax Quick Start Guide

Get up and running with Apax in minutes!

## Prerequisites

- Android Studio Otter 2 Feature Drop or later
- Android device or emulator (Android 8.0+)
- Basic understanding of Android development

## Step 1: Open the Project

1. Launch Android Studio
2. Click **File > Open**
3. Navigate to the project directory
4. Click **OK**

## Step 2: Sync and Build

1. Wait for Gradle sync to complete
2. If prompted, accept any SDK updates
3. Click **Build > Make Project**
4. Wait for native libraries to compile

**Expected Output**:
```
BUILD SUCCESSFUL in 30s
```

## Step 3: Run the App

1. Connect an Android device or start an emulator
2. Click the **Run** button (green triangle)
3. Select your device
4. Wait for installation

**Expected Result**: App launches and shows Apax status screen

## Step 4: Explore the UI

### Main Screen
- **Title**: "Apax"
- **Version**: Shows native core version
- **Status Display**: Shows detailed Apax status
- **Refresh Button**: Updates status display
- **Initialize Button**: Explicitly initializes native core

### Try It Out
1. Tap **"Refresh Status"** - Status should update
2. Tap **"Initialize Core"** - Core should initialize
3. Check Logcat for detailed logs

## Step 5: Enable Foreground Service (Optional)

The foreground service allows Apax to run persistently.

### From Code
Add to MainActivity or create a button:
```java
Intent serviceIntent = new Intent(this, ApaxCoreService.class);
startForegroundService(serviceIntent);
```

### Expected Result
- Notification appears: "Apax Core Service"
- Service runs in background
- Can be stopped via notification

## Step 6: Enable Accessibility Service (Optional)

The accessibility service provides context-aware features.

### Steps
1. Open device **Settings**
2. Navigate to **Accessibility**
3. Find **"Apax Accessibility Service"**
4. Tap to open
5. Review capabilities
6. Toggle **ON**
7. Confirm warning dialog

### Expected Result
- Service shows as "On"
- Apax can now receive accessibility events
- Check Logcat for "ApaxAccessibility" logs

## Step 7: Check Logs

View detailed logs in Android Studio Logcat:

### Filter by Tag
- `ApaxMainActivity` - UI events
- `ApaxCoreService` - Service lifecycle
- `ApaxAccessibility` - Accessibility events
- `ApaxCore` - Native core logs
- `ApaxJNI` - JNI bridge logs

### Example Log Output
```
I/ApaxMainActivity: MainActivity onCreate
I/ApaxJNI: apaxStatus() called from Java layer
I/ApaxCore: ApaxCore constructor called
I/ApaxCore: Initializing ApaxCore v1.0.0
I/ApaxCore: ApaxCore initialization complete
```

## Common Issues

### Issue: Build Fails with CMake Error

**Solution**:
1. Check NDK is installed: **Tools > SDK Manager > SDK Tools > NDK**
2. Verify CMake version: Should be 3.22.1+
3. Clean and rebuild: **Build > Clean Project** then **Build > Rebuild Project**

### Issue: Native Library Not Found

**Error**: `UnsatisfiedLinkError: dlopen failed: library "apax_core" not found`

**Solution**:
1. Ensure native build succeeded
2. Check `app/build/intermediates/cmake/` for .so files
3. Rebuild project
4. Sync Gradle files

### Issue: Accessibility Service Not Appearing

**Solution**:
1. Ensure app is installed
2. Check AndroidManifest.xml has accessibility service declared
3. Verify accessibility_service_config.xml exists
4. Reinstall app
5. Restart device

### Issue: Foreground Service Crashes

**Error**: `ForegroundServiceDidNotStartInTimeException`

**Solution**:
1. Ensure `startForeground()` is called within 5 seconds
2. Check notification channel is created
3. Verify FOREGROUND_SERVICE permission in manifest
4. Check Android version compatibility

## Next Steps

### Explore the Code

1. **MainActivity.java** - UI and user interaction
2. **ApaxCoreService.java** - Foreground service
3. **ApaxAccessibilityService.java** - Accessibility features
4. **apax_core.cpp** - Native reasoning engine
5. **apax_jni_bridge.cpp** - JNI interface

### Customize

1. Modify UI in `activity_main.xml`
2. Add new native methods in `apax_core.cpp`
3. Extend services with new features
4. Add permissions as needed

### Read Documentation

- **README.md** - Project overview
- **ARCHITECTURE.md** - Detailed architecture
- **SECURITY.md** - Security model and compliance

## Development Workflow

### Making Changes

1. **Edit Code** - Make your changes
2. **Build** - Build > Make Project
3. **Run** - Test on device/emulator
4. **Check Logs** - Verify in Logcat
5. **Iterate** - Repeat as needed

### Adding Native Methods

1. Declare in `ApaxNativeBridge.java`
2. Implement in `apax_jni_bridge.cpp`
3. Add logic in `apax_core.cpp`
4. Rebuild project
5. Call from Java code

### Adding Permissions

1. Add to `AndroidManifest.xml`
2. Request at runtime (if dangerous)
3. Handle permission result
4. Use permission-protected feature

## Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] App launches successfully
- [ ] Status displays correctly
- [ ] Buttons respond to clicks
- [ ] Native methods work
- [ ] Service starts/stops
- [ ] Accessibility service enables
- [ ] Logs show expected output

## Debugging Tips

### Enable Verbose Logging
In native code, use:
```cpp
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
```

### Attach Debugger
1. Set breakpoints in Java code
2. Click **Debug** button
3. Step through code

### Native Debugging
1. Set breakpoints in C++ code
2. Use **Debug** with native debugging enabled
3. Requires debug build variant

### Check Native Crashes
```bash
adb logcat | grep "DEBUG"
```

## Performance Monitoring

### Check Memory Usage
```bash
adb shell dumpsys meminfo com.apax.core
```

### Check CPU Usage
```bash
adb shell top | grep apax
```

### Battery Impact
Settings > Battery > Battery Usage > Apax

## Deployment

### Debug Build
- For development and testing
- Includes debug symbols
- Verbose logging

### Release Build
1. Configure signing in `build.gradle`
2. Build > Generate Signed Bundle/APK
3. Select release variant
4. Sign with keystore
5. Generate APK/AAB

## Getting Help

### Resources
- Android Developer Documentation
- Android Studio Documentation
- JNI Documentation
- CMake Documentation

### Troubleshooting
1. Check Logcat for errors
2. Review build output
3. Verify configuration
4. Clean and rebuild
5. Restart Android Studio

## Success Checklist

You're ready to develop with Apax when:

- ✅ Project builds without errors
- ✅ App runs on device/emulator
- ✅ Native methods execute successfully
- ✅ Logs show expected output
- ✅ UI responds to interactions
- ✅ Services can be started
- ✅ You understand the architecture

## What's Next?

Now that you're set up, you can:

1. **Extend Native Core** - Add reasoning logic
2. **Implement Features** - Build new capabilities
3. **Integrate ML** - Add TensorFlow Lite models
4. **Create UI** - Design better interfaces
5. **Add Services** - Implement background tasks
6. **Optimize** - Improve performance

Happy coding! 🚀
