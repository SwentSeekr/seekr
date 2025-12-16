package com.swentseekr.seekr.model.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.RemoteMessage
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test

const val NEW_NOTIFICATION = "New Notification"
const val HELLO = "Hello"
const val WORLD = "World"
const val EMPTY_STRING = ""

@OptIn(ExperimentalCoroutinesApi::class)
class MyFirebaseMessagingServiceTest {
  private lateinit var service: MyFirebaseMessagingService
  private val mockFirebaseAuth = mockk<FirebaseAuth>(relaxed = true)
  private val mockFirebaseUser = mockk<FirebaseUser>()

  @Before
  fun setup() {
    service = spyk(MyFirebaseMessagingService(), recordPrivateCalls = true)

    mockkStatic(FirebaseAuth::class)
    every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
    every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

    mockkObject(NotificationTokenService)

    mockkStatic(Log::class)
    every { Log.d(any(), any()) } returns 0
    every { Log.e(any(), any(), any()) } returns 0

    mockkObject(NotificationHelper)
    every { NotificationHelper.sendNotification(any(), any(), any(), any()) } just Runs
  }

  @After
  fun tearDown() {
    unmockkObject(NotificationTokenService)
    unmockkAll()
  }

  @Test
  fun onMessageReceived_sends_notification_with_title_and_body() {
    val remoteMessage = mockk<RemoteMessage>(relaxed = true)
    val notification = mockk<RemoteMessage.Notification>()
    every { notification.title } returns HELLO
    every { notification.body } returns WORLD
    every { remoteMessage.notification } returns notification

    service.onMessageReceived(remoteMessage)

    verify { NotificationHelper.sendNotification(service, HELLO, WORLD, null) }
  }

  @Test
  fun onMessageReceived_sends_notification_with_default_title_when_null() {
    val remoteMessage = mockk<RemoteMessage>(relaxed = true)
    every { remoteMessage.notification } returns null

    service.onMessageReceived(remoteMessage)

    verify { NotificationHelper.sendNotification(service, NEW_NOTIFICATION, EMPTY_STRING, null) }
  }

  @Test
  fun onNewToken_updates_firestore_when_user_exists() {
    every { mockFirebaseUser.uid } returns "uid123"
    every { NotificationTokenService.persistToken("uid123", "token123") } returns
        mockk(relaxed = true)

    service.onNewToken("token123")

    verify { NotificationTokenService.persistToken("uid123", "token123") }
  }

  @Test
  fun onNewToken_does_nothing_when_user_is_null() {
    every { mockFirebaseAuth.currentUser } returns null

    service.onNewToken("token123")

    verify(exactly = 0) { NotificationTokenService.persistToken(any(), any()) }
  }

  @Test
  fun onNewToken_logs_success_when_firestore_update_succeeds() {
    every { mockFirebaseUser.uid } returns "uid123"

    val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>()
    every { NotificationTokenService.persistToken("uid123", "token123") } returns mockTask
    every { mockTask.addOnSuccessListener(any()) } answers
        {
          val listener = firstArg<com.google.android.gms.tasks.OnSuccessListener<Void>>()
          listener.onSuccess(null)
          mockTask
        }
    every { mockTask.addOnFailureListener(any()) } returns mockTask

    service.onNewToken("token123")

    verify { Log.d(NotificationConstants.TAG_FCM, NotificationConstants.LOG_TOKEN_SAVED) }
  }

  @Test
  fun onNewToken_logs_error_when_firestore_update_fails() {
    every { mockFirebaseUser.uid } returns "uid123"

    val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>()
    every { NotificationTokenService.persistToken("uid123", "token123") } returns mockTask
    every { mockTask.addOnSuccessListener(any()) } returns mockTask
    every { mockTask.addOnFailureListener(any()) } answers
        {
          val listener = firstArg<com.google.android.gms.tasks.OnFailureListener>()
          listener.onFailure(Exception("fail"))
          mockTask
        }

    service.onNewToken("token123")

    verify { Log.e(NotificationConstants.TAG_FCM, NotificationConstants.LOG_TOKEN_FAILED, any()) }
  }
}
