package com.apax.core;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Apax Instrumented Tests
 *
 * These tests run on an Android device or emulator.
 * Use these for testing Android-specific functionality.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.apax.core", appContext.getPackageName());
    }

    // Future tests to add:
    // - Test native library loading
    // - Test JNI method calls
    // - Test service start/stop
    // - Test accessibility service
    // - Test UI components
}
