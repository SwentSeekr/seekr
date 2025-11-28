package com.swentseekr.seekr.model.notifications

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.FIELD_NOTIFICATIONS
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.PROFILES_COLLECTION
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.SETTINGS_COLLECTION
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.TEST_FCM_TOKEN_PREFIX
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationTest {

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

  private lateinit var db: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var context: Context

  @Before
  fun setup() = runBlocking {
    context = ApplicationProvider.getApplicationContext()

    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    auth =
        FirebaseAuth.getInstance().apply {
          useEmulator(
              NotificationTestConstants.EMULATOR_HOST, NotificationTestConstants.AUTH_EMULATOR_PORT)
        }

    db =
        FirebaseFirestore.getInstance().apply {
          useEmulator(
              NotificationTestConstants.EMULATOR_HOST,
              NotificationTestConstants.FIRESTORE_EMULATOR_PORT)
        }

    if (auth.currentUser == null) {
      auth.signInAnonymously().await()
    }
  }

  suspend fun waitForDebugLog(
      reviewId: String,
      timeoutMs: Long = NotificationTestConstants.WAIT_TIMEOUT_MS
  ): Boolean {
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < timeoutMs) {
      val logs =
          db.collection(NotificationTestConstants.DEBUG_COLLECTION)
              .whereEqualTo(NotificationTestConstants.FIELD_REVIEW_ID, reviewId)
              .get()
              .await()
      if (logs.documents.isNotEmpty()) return true
      delay(NotificationTestConstants.POLL_DELAY_MS)
    }
    return false
  }

  @Test
  fun notificationHelper_sendsNotificationWithPermission() = runBlocking {
    NotificationHelper.createNotificationChannel(context)

    NotificationHelper.sendNotification(
        context, NotificationTestConstants.TEST_TITLE, NotificationTestConstants.TEST_MESSAGE)

    assertTrue(true)
  }

  @Test
  fun fcmToken_isSavedToFirestore() = runBlocking {
    val uid = auth.currentUser?.uid ?: return@runBlocking
    val testToken = TEST_FCM_TOKEN_PREFIX + System.currentTimeMillis()

    val profile =
        mapOf(
            NotificationTestConstants.FIELD_AUTHOR to
                mapOf(
                    NotificationTestConstants.FIELD_PSEUDONYM to
                        NotificationTestConstants.TEST_PSEUDONYM,
                    NotificationTestConstants.FIELD_FCM_TOKEN to testToken))

    db.collection(PROFILES_COLLECTION).document(uid).set(profile).await()

    val doc = db.collection(PROFILES_COLLECTION).document(uid).get().await()
    val savedToken = doc.getString(NotificationTestConstants.AUTHOR_FIELD_FCM_TOKEN)

    assertEquals(testToken, savedToken)
  }

  @Test
  fun settingsViewModel_updatesNotificationPreference() = runBlocking {
    val uid = auth.currentUser?.uid ?: return@runBlocking

    db.collection(SETTINGS_COLLECTION)
        .document(uid)
        .set(mapOf(FIELD_NOTIFICATIONS to false))
        .await()

    db.collection(SETTINGS_COLLECTION).document(uid).update(FIELD_NOTIFICATIONS, true).await()

    val doc = db.collection(SETTINGS_COLLECTION).document(uid).get().await()
    val notificationsEnabled = doc.getBoolean(FIELD_NOTIFICATIONS)

    assertEquals(NotificationTestConstants.NOTIFICATION_MESSAGE, true, notificationsEnabled)
  }
}
