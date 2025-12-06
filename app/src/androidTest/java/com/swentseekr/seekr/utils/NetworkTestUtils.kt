package com.swentseekr.seekr.utils

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Simple helper to toggle emulator network on/off in instrumentation tests.
 *
 * This uses shell commands via UiAutomation, which works on the Android emulator. All calls are
 * wrapped in try/catch so a failure won't crash instrumentation.
 */
object NetworkTestUtils {

  private const val TAG = "NetworkTestUtils"

  private fun safeExec(cmd: String) {
    try {
      val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
      // executeShellCommand can return null on some APIs, so we null-check.
      uiAutomation.executeShellCommand(cmd)?.close()
      Log.i(TAG, "Executed shell command: $cmd")
    } catch (t: Throwable) {
      // Don't let this crash tests; just log and continue.
      Log.w(TAG, "Failed to execute shell command '$cmd': ${t.message}")
    }
  }

  fun goOffline() {
    safeExec("svc wifi disable")
    safeExec("svc data disable")
  }

  fun goOnline() {
    safeExec("svc wifi enable")
    safeExec("svc data enable")
  }
}
