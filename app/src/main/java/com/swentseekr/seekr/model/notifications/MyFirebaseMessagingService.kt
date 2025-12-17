package com.swentseekr.seekr.model.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.swentseekr.seekr.model.notifications.NotificationConstants.HUNT_ID

/** Firebase Messaging Service to handle incoming FCM messages and token updates. */
class MyFirebaseMessagingService : FirebaseMessagingService() {

  /**
   * Processes the remote message and sends a system notification using [NotificationHelper]. If the
   * notification title or body is missing, default values from [NotificationConstants] are used.
   *
   * @param remoteMessage The incoming [RemoteMessage] from Firebase Cloud Messaging.
   */
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val title =
        remoteMessage.notification?.title ?: NotificationConstants.DEFAULT_NOTIFICATION_TITLE
    val body = remoteMessage.notification?.body ?: NotificationConstants.DEFAULT_NOTIFICATION_BODY
    val huntId = remoteMessage.data[HUNT_ID]
    NotificationHelper.sendNotification(this, title, body, huntId)
  }

  /**
   * Updates the current user's document in Firestore with the new token. If no user is signed in,
   * the update is skipped.
   *
   * @param token The new FCM registration token.
   */
  override fun onNewToken(token: String) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    NotificationTokenService.persistToken(uid, token)
        .addOnSuccessListener {
          Log.d(NotificationConstants.TAG_FCM, NotificationConstants.LOG_TOKEN_SAVED)
        }
        .addOnFailureListener { e ->
          Log.e(NotificationConstants.TAG_FCM, NotificationConstants.LOG_TOKEN_FAILED, e)
        }
  }
}
