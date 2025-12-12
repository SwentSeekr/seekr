package com.swentseekr.seekr.model.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.swentseekr.seekr.model.notifications.NotificationConstants.HUNT_ID

class MyFirebaseMessagingService : FirebaseMessagingService() {

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val title =
        remoteMessage.notification?.title ?: NotificationConstants.DEFAULT_NOTIFICATION_TITLE
    val body = remoteMessage.notification?.body ?: NotificationConstants.DEFAULT_NOTIFICATION_BODY
    val huntId = remoteMessage.data[HUNT_ID]
    NotificationHelper.sendNotification(this, title, body, huntId)
  }

  override fun onNewToken(token: String) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    FirebaseFirestore.getInstance()
        .collection(NotificationConstants.COLLECTION_PROFILES)
        .document(uid)
        .update(NotificationConstants.FIELD_AUTHOR_FCM_TOKEN, token)
        .addOnSuccessListener {
          Log.d(NotificationConstants.TAG_FCM, NotificationConstants.LOG_TOKEN_SAVED)
        }
        .addOnFailureListener { e ->
          Log.e(NotificationConstants.TAG_FCM, NotificationConstants.LOG_TOKEN_FAILED, e)
        }
  }
}
