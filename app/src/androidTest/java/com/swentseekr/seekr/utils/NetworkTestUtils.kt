package com.swentseekr.seekr.utils

import androidx.test.platform.app.InstrumentationRegistry

/**
 * Simple helper to toggle emulator network on/off in instrumentation tests.
 *
 * This uses shell commands via UiAutomation, which works on the Android emulator.
 */
object NetworkTestUtils {

  fun goOffline() {
    val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
    uiAutomation.executeShellCommand("svc wifi disable").close()
    uiAutomation.executeShellCommand("svc data disable").close()
  }

  fun goOnline() {
    val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
    uiAutomation.executeShellCommand("svc wifi enable").close()
    uiAutomation.executeShellCommand("svc data enable").close()
  }
}
