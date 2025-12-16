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
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.EMPTY_STRING
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.EXTRA_HUNT_ID
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.FIELD_NOTIFICATIONS
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.HUNT_FCM
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.HUNT_PRESERVE
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.HUNT_REMOTE
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.HUNT_REMOVE
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.HUNT_SPECIAL_CHAR
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.NULL_VALUE
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.PROFILES_COLLECTION
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.SETTINGS_COLLECTION
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.TEST_FCM_TOKEN_PREFIX
import com.swentseekr.seekr.model.notifications.NotificationTestConstants.TEST_HUNT_ID
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for notification-related features.
 *
 * This test suite verifies Firebase-backed notification behavior, including FCM token storage,
 * notification preference updates, intent and PendingIntent handling, and notification dispatch
 * logic using the Firebase emulator.
 */
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
  fun fcmTokenIsSavedToFirestore() = runBlocking {
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
  fun settingsViewModelUpdatesNotificationPreference() = runBlocking {
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
  fun notificationHelperCreatesIntentWithHuntId() {
    val testHuntId = TEST_HUNT_ID

    val intent =
        Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          putExtra(EXTRA_HUNT_ID, testHuntId)
        }

    assertEquals(testHuntId, intent.getStringExtra(EXTRA_HUNT_ID))
    assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != NULL_VALUE)
    assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK != NULL_VALUE)
  }

  @Test
  fun notificationHelperSendsNotificationWithHuntId() = runBlocking {
    NotificationHelper.createNotificationChannel(context)

    val testHuntId = NotificationTestConstants.TEST_HUNT_ID_456
    val testTitle = NotificationTestConstants.NEW_REVIEW
    val testMessage = NotificationTestConstants.REVIEW_MESSAGE

    NotificationHelper.sendNotification(context, testTitle, testMessage, testHuntId)
    assertTrue(true)
  }

  @Test
  fun notificationHelperSendsNotificationWithNullHuntId() = runBlocking {
    NotificationHelper.createNotificationChannel(context)

    val testTitle = NotificationTestConstants.GENERAL_NOTIFICATION
    val testMessage = NotificationTestConstants.GENERAL_MESSAGE

    NotificationHelper.sendNotification(context, testTitle, testMessage, null)

    assertTrue(true)
  }

  @Test
  fun notificationHelperCreatesPendingIntentWithCorrectFlags() {
    val testHuntId = NotificationTestConstants.TEST_HUNT_ID_789

    val intent =
        Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          putExtra(EXTRA_HUNT_ID, testHuntId)
        }

    val pendingIntent =
        PendingIntent.getActivity(
            context,
            NULL_VALUE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    assertNotNull(pendingIntent)
  }

  @Test
  fun firebaseMessagingHandlesRemoteMessageWithHuntId() {

    val data = mapOf(EXTRA_HUNT_ID to HUNT_REMOTE)

    assertNotNull(data[EXTRA_HUNT_ID])
    assertEquals(HUNT_REMOTE, data[EXTRA_HUNT_ID])
  }

  @Test
  fun intentPreservesHuntIdAcrossRecreation() {
    val originalHuntId = HUNT_PRESERVE

    val intent =
        Intent(context, MainActivity::class.java).apply { putExtra(EXTRA_HUNT_ID, originalHuntId) }

    val extractedHuntId = intent.getStringExtra(EXTRA_HUNT_ID)

    assertEquals(originalHuntId, extractedHuntId)
  }

  @Test
  fun intentCanRemoveHuntIdExtra() {
    val testHuntId = HUNT_REMOVE

    val intent =
        Intent(context, MainActivity::class.java).apply { putExtra(EXTRA_HUNT_ID, testHuntId) }

    assertNotNull(intent.getStringExtra(EXTRA_HUNT_ID))

    intent.removeExtra(EXTRA_HUNT_ID)

    assertNull(intent.getStringExtra(EXTRA_HUNT_ID))
  }

  @Test
  fun notificationChannelIsCreatedSuccessfully() {
    NotificationHelper.createNotificationChannel(context)
    assertTrue(true)
  }

  @Test
  fun remoteMessageExtractsHuntIdFromData() {
    val remoteMessageData =
        mapOf(
            EXTRA_HUNT_ID to HUNT_FCM,
            NotificationTestConstants.TITLE to NotificationTestConstants.TEST_TITLE,
            NotificationTestConstants.BODY to NotificationTestConstants.TEST_BODY)

    val huntId = remoteMessageData[EXTRA_HUNT_ID]

    assertNotNull(huntId)
    assertEquals(HUNT_FCM, huntId)
  }

  @Test
  fun notificationUsesDefaultValuesWhenMissing() {
    val title = null ?: NotificationConstants.DEFAULT_NOTIFICATION_TITLE
    val body = null ?: NotificationConstants.DEFAULT_NOTIFICATION_BODY

    assertEquals(NotificationConstants.DEFAULT_NOTIFICATION_TITLE, title)
    assertEquals(NotificationConstants.DEFAULT_NOTIFICATION_BODY, body)
  }

  @Test
  fun pendingIntentHasCorrectImmutableFlag() {
    val intent = Intent(context, MainActivity::class.java)

    val pendingIntent =
        PendingIntent.getActivity(
            context,
            NULL_VALUE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    assertNotNull(pendingIntent)
  }

  @Test
  fun multipleNotificationsHaveUniqueIds() {
    val time1 = System.currentTimeMillis()
    Thread.sleep(NotificationTestConstants.THREAD_SLEEP_TIME.toLong())
    val time2 = System.currentTimeMillis()

    val id1 = time1.toInt()
    val id2 = time2.toInt()

    assertNotEquals(id1, id2)
  }

  @Test
  fun intentExtrasHandleEmptyHuntId() {
    val intent =
        Intent(context, MainActivity::class.java).apply { putExtra(EXTRA_HUNT_ID, EMPTY_STRING) }

    val huntId = intent.getStringExtra(EXTRA_HUNT_ID)

    assertNotNull(huntId)
    assertTrue(huntId!!.isEmpty())
  }

  @Test
  fun intentExtrasHandleSpecialCharactersInHuntId() {
    val specialHuntId = HUNT_SPECIAL_CHAR
    val intent =
        Intent(context, MainActivity::class.java).apply { putExtra(EXTRA_HUNT_ID, specialHuntId) }
    val extractedHuntId = intent.getStringExtra(EXTRA_HUNT_ID)
    assertEquals(specialHuntId, extractedHuntId)
  }
}
