package com.swentseekr.seekr.utils

// import com.google.firebase.storage.FirebaseStorage
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.Socket

/**
 * Centralized Firebase test environment configuration.
 * - Detects if emulators are running locally.
 * - Connects Firebase modules (Auth, Firestore, Storage) to them if available.
 * - Allows other fake helpers (e.g., [FakeAuthEmulator]) to rely on this setup.
 *
 * Scalable: Add more Firebase modules (Storage, Functions, etc.) as needed.
 */
object FirebaseTestEnvironment {

  private const val HOST = "10.0.2.2" // Android emulator's loopback to localhost
  private const val AUTH_PORT = 9099
  private const val FIRESTORE_PORT = 8080
  private const val STORAGE_PORT = 9199

  private var initialized = false
  private var emulatorAvailable = false

  /** Initialize Firebase emulator connections if available. Safe to call multiple times. */
  fun setup() {
    if (initialized) return
    initialized = true

    // Ensure a FirebaseApp instance exists before using modules
    try {
      if (FirebaseApp.getApps(androidx.test.core.app.ApplicationProvider.getApplicationContext())
          .isEmpty()) {
        FirebaseApp.initializeApp(
            androidx.test.core.app.ApplicationProvider.getApplicationContext())
        Log.i("FirebaseTestEnv", "FirebaseApp initialized manually.")
      }
    } catch (e: Exception) {
      Log.w("FirebaseTestEnv", "Failed to initialize FirebaseApp: ${e.message}")
    }

    emulatorAvailable = isPortOpen(HOST, AUTH_PORT)

    if (emulatorAvailable) {
      try {
        FirebaseAuth.getInstance().useEmulator(HOST, AUTH_PORT)
        FirebaseFirestore.getInstance().useEmulator(HOST, FIRESTORE_PORT)
        // FirebaseStorage.getInstance().useEmulator(HOST, STORAGE_PORT)
        Log.i("FirebaseTestEnv", "Connected to Firebase emulators at $HOST.")
      } catch (e: Exception) {
        Log.w("FirebaseTestEnv", "Failed to connect to emulators: ${e.message}")
        emulatorAvailable = false
      }
    } else {
      Log.i("FirebaseTestEnv", "Firebase emulators not running — using offline fake mode.")
    }
  }

  /** Returns true if emulators are detected and connected. */
  fun isEmulatorActive(): Boolean = emulatorAvailable

  /** Checks whether a TCP port is open — used to detect running emulators. */
  private fun isPortOpen(host: String, port: Int): Boolean {
    return try {
      Socket(host, port).use { true }
    } catch (_: Exception) {
      false
    }
  }
}
