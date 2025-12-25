# Apax Native Core - CMake Build Configuration
# This builds the native C++ reasoning and decision core for Apax
# The native layer is isolated from Android and communicates only via JNI

cmake_minimum_required(VERSION 3.22.1)

# Project name: apax_core
# This is the native library name that will be loaded via System.loadLibrary("apax_core")
project("apax_core")

# Enable C++17 for modern C++ features
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Build the apax_core shared library
add_library(${CMAKE_PROJECT_NAME} SHARED
        # Core native implementation files
        apax_core.cpp
        apax_jni_bridge.cpp
)

# Link required Android libraries
target_link_libraries(${CMAKE_PROJECT_NAME}
        # Android native APIs
        android
        # Logging support for native debugging
        log
)