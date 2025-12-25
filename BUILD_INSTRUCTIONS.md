# Build Instructions for Walmart 100011886 Tablet

## Quick Build Guide

This document provides instructions for building Apax for the Walmart 100011886 tablet.

---

## Prerequisites

### Required Software
1. **Android Studio** Otter 2 Feature Drop or later
2. **Java Development Kit (JDK)** 11 or higher
3. **Android SDK** with API 30+ installed
4. **Android NDK** (for native library compilation)
5. **CMake** 3.22.1 or higher

### Verify Installation

Open Android Studio and check:
- **File → Project Structure → SDK Location**
  - Android SDK location should be set
  - JDK location should be set
  - Android NDK location should be set

---

## Building in Android Studio

### Method 1: Using Android Studio GUI (Recommended)

1. **Open Project**
   - Launch Android Studio
   - File → Open
   - Navigate to: `C:/Users/Bensg/AndroidStudioProjects/MyApplication`
   - Click OK

2. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for "Gradle sync finished" message
   - If errors occur, click "Sync Project with Gradle Files" button

3. **Build APK**
   - **Debug Build**: Build → Build Bundle(s) / APK(s) → Build APK(s)
   - **Release Build**: Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Wait for build to complete

4. **Locate APK**
   - Click "locate" link in build notification
   - Or navigate to: `app/build/outputs/apk/debug/` or `app/build/outputs/apk/release/`

### Method 2: Using Gradle Command Line

If you have Java configured in your PATH:

#### Windows (PowerShell or Command Prompt)
```powershell
# Navigate to project directory
cd C:/Users/Bensg/AndroidStudioProjects/MyApplication

# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

#### If JAVA_HOME is not set
Set it temporarily in PowerShell:
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
./gradlew assembleDebug
```

Or set it permanently:
1. Open System Properties → Environment Variables
2. Add new System Variable:
   - Name: `JAVA_HOME`
   - Value: Path to JDK (e.g., `C:\Program Files\Android\Android Studio\jbr`)
3. Restart terminal/PowerShell

---

## Build Configuration

### Target Device: Walmart 100011886 Tablet

The project is configured specifically for this tablet:

```gradle
android {
    defaultConfig {
        minSdk 30              // Android 11+
        targetSdk 36           // Latest Android
        versionName "1.0.0-walmart-tablet"

        ndk {
            abiFilters 'arm64-v8a'  // ARM64 only
        }
    }
}
```

### Build Variants

- **Debug**: For development and testing
  - Package: `com.apax.core.debug`
  - Debuggable: Yes
  - Optimizations: Minimal

- **Release**: For production use
  - Package: `com.apax.core`
  - Debuggable: No
  - Optimizations: Enabled (when configured)

---

## Native Library Compilation

The native `apax_core` library is automatically compiled during the build process.

### CMake Configuration
- **Location**: `app/src/main/cpp/CMakeLists.txt`
- **C++ Standard**: C++17
- **Target ABI**: arm64-v8a
- **Output**: `libapax_core.so`

### Verify Native Build
Check build output for:
```
> Task :app:buildCMakeDebug[arm64-v8a]
Build apax_core arm64-v8a
```

---

## Build Output

### APK Locations

**Debug APK**:
```
app/build/outputs/apk/debug/app-debug.apk
```

**Release APK**:
```
app/build/outputs/apk/release/app-release.apk
```

### APK Size
- **Expected size**: ~5-10 MB (debug), ~3-7 MB (release)
- **Optimized for**: Single ABI (arm64-v8a only)

---

## Installing on Tablet

### Method 1: Direct Install from Android Studio

1. Connect Walmart 100011886 tablet via USB
2. Enable USB Debugging on tablet (see WALMART_TABLET_GUIDE.md)
3. In Android Studio: Run → Run 'app'
4. Select your tablet from device list
5. App will build and install automatically

### Method 2: ADB Install

```bash
# Connect tablet via USB
adb devices

# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Or install release APK
adb install app/build/outputs/apk/release/app-release.apk

# If app is already installed, use -r to reinstall
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Method 3: Manual Install

1. Copy APK to tablet (USB, cloud storage, etc.)
2. On tablet, enable "Install from Unknown Sources"
3. Use file manager to locate APK
4. Tap APK to install

---

## Troubleshooting

### Issue: "JAVA_HOME is not set"

**Solution**:
1. Find your JDK installation:
   - Android Studio's JDK: `C:\Program Files\Android\Android Studio\jbr`
   - Or standalone JDK installation

2. Set JAVA_HOME:
   ```powershell
   # Temporary (current session)
   $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

   # Or set permanently in System Environment Variables
   ```

### Issue: "SDK location not found"

**Solution**:
1. Open Android Studio
2. File → Project Structure → SDK Location
3. Set Android SDK location (usually `C:\Users\<username>\AppData\Local\Android\Sdk`)
4. Click OK and sync project

### Issue: "NDK not configured"

**Solution**:
1. In Android Studio: Tools → SDK Manager
2. Go to SDK Tools tab
3. Check "NDK (Side by side)"
4. Check "CMake"
5. Click Apply to install
6. Sync project

### Issue: "Build failed: CMake error"

**Solution**:
1. Verify CMake is installed (SDK Manager → SDK Tools)
2. Check `app/src/main/cpp/CMakeLists.txt` exists
3. Clean and rebuild: Build → Clean Project, then Build → Rebuild Project

### Issue: "Unsupported class file major version"

**Solution**:
- Your JDK version is too new or too old
- Use JDK 11 or 17 (recommended)
- Set in File → Project Structure → SDK Location → JDK location

### Issue: Native library not found at runtime

**Solution**:
1. Verify native library compiled: Check build output
2. Ensure ABI matches: Should be arm64-v8a for Walmart tablet
3. Check library name: Should be `libapax_core.so`
4. Rebuild native library: Build → Refresh Linked C++ Projects

---

## Verification

### After Building

1. **Check APK exists**:
   - Navigate to `app/build/outputs/apk/debug/`
   - Verify `app-debug.apk` is present

2. **Check APK contents** (optional):
   ```bash
   # Extract APK (it's a ZIP file)
   unzip app-debug.apk -d apk_contents

   # Verify native library
   ls apk_contents/lib/arm64-v8a/libapax_core.so
   ```

3. **Install and test**:
   - Install on Walmart 100011886 tablet
   - Launch app
   - Verify status display shows device information
   - Check logcat for successful native library load

### Expected Logcat Output

```
ApaxMainActivity: MainActivity onCreate - Walmart 100011886 Tablet Edition
ApaxMainActivity: Loaded apax_core native library for ARM64 architecture
ApaxCore: ApaxCore constructor called - Walmart 100011886 Tablet Edition
ApaxCore: Initializing ApaxCore v1.0.0-walmart-tablet
ApaxJNI: apaxStatus() called from Java layer
```

---

## Build Optimization

### For Smaller APK Size

Already configured:
- ✅ Single ABI (arm64-v8a only)
- ✅ Resource filtering (English only, xxhdpi)
- ✅ No unused libraries

### For Faster Builds

In `gradle.properties`, add:
```properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

### For Release Builds

Enable ProGuard/R8 in `app/build.gradle`:
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

---

## Continuous Integration (Optional)

### GitHub Actions Example

```yaml
name: Build APK

on: [push]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Build with Gradle
        run: ./gradlew assembleDebug
      - name: Upload APK
        uses: actions/upload-artifact@v2
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

---

## Next Steps

After successful build:

1. **Install on tablet**: See WALMART_TABLET_GUIDE.md
2. **Test functionality**: Verify all features work
3. **Configure Device Owner** (optional): See DEVICE_OWNER_SETUP.md
4. **Develop features**: Add your custom assistant logic

---

## Build Checklist

Before building for production:

- [ ] All code changes committed
- [ ] Version number updated in `build.gradle`
- [ ] Native library compiles without errors
- [ ] No linter errors in Java/XML files
- [ ] Tested on actual Walmart 100011886 tablet
- [ ] All permissions documented
- [ ] User documentation updated
- [ ] Release notes prepared

---

## Support

### Build Issues
- Check Android Studio's Build Output panel
- Review Gradle Console for detailed errors
- Check logcat for runtime issues

### Documentation
- **Project Overview**: README.md
- **Architecture**: ARCHITECTURE.md
- **Device Setup**: WALMART_TABLET_GUIDE.md
- **Development**: DEVELOPER_GUIDE.md

---

**Version**: 1.0.0-walmart-tablet
**Target Device**: Walmart 100011886 Tablet
**Build System**: Gradle 8.x with Android Gradle Plugin 8.x
