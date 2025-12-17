package com.swentseekr.seekr.model.notifications

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.tasks.await

object NotificationTokenService {
  /**
   * Persists an FCM registration token for a given user.
   *
   * The token is stored in the user's profile document using merge semantics, ensuring that
   * existing profile fields are not overwritten.
   *
   * Storage location:
   * - Collection: profiles
   * - Document: [userId]
   * - Field path: author.fcmToken
   *
   * This function does not perform authentication checks and assumes the caller has already
   * validated the user identity.
   *
   * @param userId The UID of the user whose token should be stored.
   * @param token The Firebase Cloud Messaging registration token for the device.
   * @return A [Task] that completes when the token has been written to Firestore.
   */
  @Suppress("KotlinUnitInsteadOfVoid")
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

  /**
   * Fetches the current device's FCM registration token and persists it to the given user's profile
   * document.
   *
   * This function suspends until the token is retrieved from Firebase Messaging and the Firestore
   * write completes.
   *
   * Intended to be called after a successful sign-in or when restoring an authenticated session to
   * ensure the backend always has the latest token for push notifications.
   *
   * @param userId The UID of the currently authenticated user.
   * @throws Exception If token retrieval or Firestore persistence fails.
   */
  suspend fun syncCurrentToken(userId: String) {
    val token = Firebase.messaging.token.await()
    persistToken(userId, token).await()
  }
}
