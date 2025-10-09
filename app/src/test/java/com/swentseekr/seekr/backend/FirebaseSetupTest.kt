package com.swentseekr.seekr.backend

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FirebaseSetupTest {
  @Test
  fun firebase_shouldInitializeSuccessfully() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val app =
        try {
          FirebaseApp.getInstance()
        } catch (e: IllegalStateException) {
          FirebaseApp.initializeApp(context)
        }
    assertNotNull("FirebaseApp should be initialized", app)
  }
}
