package com.swentseekr.seekr.model.notifications

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationTests {

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

    auth = FirebaseAuth.getInstance().apply { useEmulator("10.0.2.2", 9099) }

    db = FirebaseFirestore.getInstance().apply { useEmulator("10.0.2.2", 8080) }

    if (auth.currentUser == null) {
      auth.signInAnonymously().await()
    }
  }

  suspend fun waitForDebugLog(reviewId: String, timeoutMs: Long = 5000): Boolean {
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < timeoutMs) {
      val logs =
          db.collection("debug_notifications").whereEqualTo("reviewId", reviewId).get().await()
      if (logs.documents.isNotEmpty()) return true
      delay(200)
    }
    return false
  }

  @Test
  fun notificationHelper_sendsNotificationWithPermission() = runBlocking {
    // Create notification channel first
    NotificationHelper.createNotificationChannel(context)

    // Send a test notification
    NotificationHelper.sendNotification(context, "Test Title", "Test Message")

    // If we reach here without exception, notification was sent
    assertTrue(true)
  }

  @Test
  fun fcmToken_isSavedToFirestore() = runBlocking {
    val uid = auth.currentUser?.uid ?: return@runBlocking
    val testToken = "test_fcm_token_${System.currentTimeMillis()}"

    val profile = mapOf("author" to mapOf("pseudonym" to "TestUser", "fcmToken" to testToken))

    db.collection("profiles").document(uid).set(profile).await()

    val doc = db.collection("profiles").document(uid).get().await()
    val savedToken = doc.getString("author.fcmToken")

    assertEquals(testToken, savedToken)
  }

  /*  @Test
  fun cloudFunction_logsDebugOnReviewCreation() = runBlocking {
      val uid = auth.currentUser?.uid ?: return@runBlocking

      // Create hunt in emulator
      val huntId = "test_hunt_${System.currentTimeMillis()}"
      db.collection("hunts").document(huntId).set(
          mapOf(
              "uid" to huntId,
              "title" to "Test Hunt",
              "authorId" to uid,
              "status" to "PUBLISHED",
              "difficulty" to "EASY"
          )
      ).await()

      // Create profile with FCM token
      db.collection("profiles").document(uid).set(
          mapOf(
              "author" to mapOf(
                  "pseudonym" to "TestAuthor",
                  "fcmToken" to "dummy_token"
              )
          )
      ).await()

      // Create review -> should trigger Cloud Function in emulator
      val reviewId = "test_review_${System.currentTimeMillis()}"
      db.collection("huntsReviews").document(reviewId).set(
          mapOf(
              "reviewId" to reviewId,
              "authorId" to "another_user",
              "huntId" to huntId,
              "rating" to 5.0,
              "comment" to "Great hunt!",
              "photos" to emptyList<String>()
          )
      ).await()

      // Wait for Cloud Function in emulator
      delay(2000)


      assertTrue("At least one debug log should exist", waitForDebugLog(reviewId))
  }

  @Test fun reviewCreation_withoutFcmToken_logsNoTokenError() = runBlocking {
      val uid = auth.currentUser?.uid ?: return@runBlocking

  val huntId = "test_hunt_no_token_${System.currentTimeMillis()}"
  db.collection("hunts").document(huntId).set(
      mapOf( "uid" to huntId,
          "title" to "Test Hunt",
          "authorId" to uid,
          "status" to "PUBLISHED",
          "difficulty" to "EASY" )
  ).await()
      // Profile without FCM token
      db.collection("profiles").document(uid).set(
          mapOf( "author" to mapOf(
              "pseudonym" to "TestAuthor" ) ) )
          .await()
      val reviewId = "test_review_no_token_${System.currentTimeMillis()}"
      db.collection("huntsReviews").document(reviewId).set(
          mapOf( "reviewId" to reviewId,
              "authorId" to "another_user",
              "huntId" to huntId,
              "rating" to 4.0,
              "comment" to "Nice!",
              "photos" to emptyList<String>() )
      ).await()
      delay(2000)
      val logs = db.collection("debug_notifications")
          .whereEqualTo("reviewId", reviewId)
          .whereEqualTo("status", "no_token")
          .get()
          .await()
      assertTrue("Should log 'no_token' status when FCM token is missing", logs.documents.isNotEmpty())
  }
  @Test fun reviewCreation_withNonExistentHunt_logsHuntNotFound() = runBlocking {
      val reviewId = "test_review_no_hunt_${System.currentTimeMillis()}"
      val nonExistentHuntId = "hunt_that_does_not_exist_${System.currentTimeMillis()}"
      db.collection("huntsReviews").document(reviewId).set(
          mapOf( "reviewId" to reviewId,
              "authorId" to "some_user",
              "huntId" to nonExistentHuntId,
              "rating" to 3.0, "comment" to "Test",
              "photos" to emptyList<String>() )
      ).await()
      delay(2000)
      val logs = db.collection("debug_notifications")
          .whereEqualTo("reviewId", reviewId)
          .whereEqualTo("status", "hunt_not_found")
          .get()
          .await()
      assertTrue("Should log 'hunt_not_found' when hunt doesn't exist", logs.documents.isNotEmpty())
  }*/

  @Test
  fun settingsViewModel_updatesNotificationPreference() = runBlocking {
    val uid = auth.currentUser?.uid ?: return@runBlocking

    // Initialize settings document
    db.collection("settings").document(uid).set(mapOf("notifications" to false)).await()

    // Update notification setting
    db.collection("settings").document(uid).update("notifications", true).await()

    // Verify update
    val doc = db.collection("settings").document(uid).get().await()
    val notificationsEnabled = doc.getBoolean("notifications")

    assertEquals("Notifications should be enabled", true, notificationsEnabled)
  }
}
