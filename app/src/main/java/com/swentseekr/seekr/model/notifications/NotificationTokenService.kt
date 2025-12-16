package com.swentseekr.seekr.model.notifications

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.tasks.await

object NotificationTokenService {

  fun persistToken(userId: String, token: String): Task<Void> {
    val payload =
        mapOf(
            NotificationConstants.FIELD_AUTHOR to
                mapOf(NotificationConstants.FIELD_FCM_TOKEN to token))

    return FirebaseFirestore.getInstance()
        .collection(NotificationConstants.COLLECTION_PROFILES)
        .document(userId)
        .set(payload, SetOptions.merge())
  }

  suspend fun syncCurrentToken(userId: String) {
    val token = Firebase.messaging.token.await()
    persistToken(userId, token).await()
  }
}
