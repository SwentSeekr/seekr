package com.swentseekr.seekr.utils

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Fake authentication helper used for UI and integration tests.
 * - Automatically integrates with [FirebaseTestEnvironment] to connect to Firebase emulators.
 * - Falls back to an in-memory fake mode if emulators are not running.
 * - Used in Compose + instrumentation tests to simulate a signed-in user.
 */
object FakeAuthEmulator {

  private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

  private var fakeUserId: String? = null
  private var fakeEmail: String? = null
  private var fakeToken: String? = null
  private var initialized = false

  /** Ensures FirebaseApp is initialized once per test process. */
  private fun ensureInitialized() {
    if (initialized) return
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
      Log.i("FakeAuthEmulator", "FirebaseApp initialized manually for tests.")
    }
    initialized = true
  }

  /**
   * Simulate signing in with a fake Google token.
   *
   * Uses Firebase Auth Emulator if detected, otherwise operates purely in fake mode.
   */
  suspend fun signInWithFakeGoogleToken(fakeIdToken: String, email: String = "test@example.com") {
    ensureInitialized()
    FirebaseTestEnvironment.setup()

    fakeToken = fakeIdToken
    fakeEmail = email
    fakeUserId = "fake-${System.currentTimeMillis()}"

    if (FirebaseTestEnvironment.isEmulatorActive()) {
      try {
        auth.signInAnonymously().await()
        Log.i("FakeAuthEmulator", "Signed in via Firebase Emulator: ${auth.currentUser?.uid}")
      } catch (e: Exception) {
        Log.w("FakeAuthEmulator", "Emulator sign-in failed, fallback to fake mode: ${e.message}")
      }
    } else {
      Log.i("FakeAuthEmulator", "Signed in (pure fake mode, no emulator).")
    }

    Log.i("FakeAuthEmulator", "Auth current user: ${auth.currentUser?.uid}, fakeToken=$fakeToken")
  }

  /** Simulate logout and clear all fake state. */
  suspend fun signOut() {
    fakeToken = null
    fakeUserId = null
    fakeEmail = null
    try {
      auth.signOut()
    } catch (_: Exception) {}
    Log.i("FakeAuthEmulator", "Signed out.")
  }

  /** Returns whether the fake or emulator user is authenticated. */
  fun isAuthenticated(): Boolean {
    return (FirebaseTestEnvironment.isEmulatorActive() && auth.currentUser != null) ||
        fakeToken != null
  }
}
