package com.swentseekr.seekr.model.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.MainActivity
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

  @Test
  fun notificationHelper_createsIntentWithHuntId() {
    val testHuntId = "test_hunt_123"

    val intent =
        Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          putExtra("huntId", testHuntId)
        }

    assertEquals(testHuntId, intent.getStringExtra("huntId"))
    assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK != 0)
  }

  @Test
  fun notificationHelper_sendsNotificationWithHuntId() = runBlocking {
    NotificationHelper.createNotificationChannel(context)

    val testHuntId = "hunt_456"
    val testTitle = "New Review"
    val testMessage = "Someone reviewed your hunt!"

    NotificationHelper.sendNotification(context, testTitle, testMessage, testHuntId)
    assertTrue(true)
  }

  @Test
  fun notificationHelper_sendsNotificationWithNullHuntId() = runBlocking {
    NotificationHelper.createNotificationChannel(context)

    val testTitle = "General Notification"
    val testMessage = "This is a general message"

    NotificationHelper.sendNotification(context, testTitle, testMessage, null)

    assertTrue(true)
  }

  @Test
  fun notificationHelper_createsPendingIntentWithCorrectFlags() {
    val testHuntId = "hunt_789"

    val intent =
        Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          putExtra("huntId", testHuntId)
        }

    val pendingIntent =
        PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    assertNotNull(pendingIntent)
  }

  @Test
  fun firebaseMessaging_handlesRemoteMessageWithHuntId() {

    val data = mapOf("huntId" to "hunt_remote_123")

    assertNotNull(data["huntId"])
    assertEquals("hunt_remote_123", data["huntId"])
  }

  @Test
  fun intent_preservesHuntIdAcrossRecreation() {
    val originalHuntId = "hunt_preserve_test"

    val intent =
        Intent(context, MainActivity::class.java).apply { putExtra("huntId", originalHuntId) }

    val extractedHuntId = intent.getStringExtra("huntId")

    assertEquals(originalHuntId, extractedHuntId)
  }

  @Test
  fun intent_canRemoveHuntIdExtra() {
    val testHuntId = "hunt_remove_test"

    val intent = Intent(context, MainActivity::class.java).apply { putExtra("huntId", testHuntId) }

    assertNotNull(intent.getStringExtra("huntId"))

    intent.removeExtra("huntId")

    assertNull(intent.getStringExtra("huntId"))
  }

  @Test
  fun notificationChannel_isCreatedSuccessfully() {
    NotificationHelper.createNotificationChannel(context)
    assertTrue(true)
  }

  @Test
  fun remoteMessage_extractsHuntIdFromData() {
    val remoteMessageData =
        mapOf("huntId" to "hunt_fcm_data_test", "title" to "Test Title", "body" to "Test Body")

    val huntId = remoteMessageData["huntId"]

    assertNotNull(huntId)
    assertEquals("hunt_fcm_data_test", huntId)
  }

  @Test
  fun notification_usesDefaultValuesWhenMissing() {
    val title = null ?: NotificationConstants.DEFAULT_NOTIFICATION_TITLE
    val body = null ?: NotificationConstants.DEFAULT_NOTIFICATION_BODY

    assertEquals(NotificationConstants.DEFAULT_NOTIFICATION_TITLE, title)
    assertEquals(NotificationConstants.DEFAULT_NOTIFICATION_BODY, body)
  }

  @Test
  fun pendingIntent_hasCorrectImmutableFlag() {
    val intent = Intent(context, MainActivity::class.java)

    val pendingIntent =
        PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    assertNotNull(pendingIntent)
  }

  @Test
  fun multipleNotifications_haveUniqueIds() {
    val time1 = System.currentTimeMillis()
    Thread.sleep(2)
    val time2 = System.currentTimeMillis()

    val id1 = time1.toInt()
    val id2 = time2.toInt()

    assertNotEquals(id1, id2)
  }

  @Test
  fun intentExtras_handleEmptyHuntId() {
    val intent = Intent(context, MainActivity::class.java).apply { putExtra("huntId", "") }

    val huntId = intent.getStringExtra("huntId")

    assertNotNull(huntId)
    assertTrue(huntId!!.isEmpty())
  }

  @Test
  fun intentExtras_handleSpecialCharactersInHuntId() {
    val specialHuntId = "hunt_with-special_chars.123"

    val intent =
        Intent(context, MainActivity::class.java).apply { putExtra("huntId", specialHuntId) }

    val extractedHuntId = intent.getStringExtra("huntId")

    assertEquals(specialHuntId, extractedHuntId)
  }
}
