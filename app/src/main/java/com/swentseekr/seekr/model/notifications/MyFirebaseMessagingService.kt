package com.swentseekr.seekr.model.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val title = remoteMessage.notification?.title ?: "New Notification"
    val body = remoteMessage.notification?.body ?: ""
    NotificationHelper.sendNotification(this, title, body)
  }

  override fun onNewToken(token: String) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    FirebaseFirestore.getInstance()
        .collection("profiles")
        .document(uid)
        .update("author.fcmToken", token)
        .addOnSuccessListener { Log.d("FCM", "Token saved") }
        .addOnFailureListener { e -> Log.e("FCM", "Failed to save token", e) }
  }
}
