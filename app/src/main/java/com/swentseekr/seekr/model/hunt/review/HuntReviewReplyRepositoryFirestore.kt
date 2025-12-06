package com.swentseekr.seekr.model.hunt.review

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

const val HUNT_REVIEW_REPLY_COLLECTION_PATH = "huntReviewReplies"

/** Firestore-backed implementation of [HuntReviewReplyRepository]. */
class HuntReviewReplyRepositoryFirestore(
    private val db: FirebaseFirestore,
) : HuntReviewReplyRepository {

  override fun getNewReplyId(): String {
    return db.collection(HUNT_REVIEW_REPLY_COLLECTION_PATH).document().id
  }

  override suspend fun getRepliesForReview(reviewId: String): List<HuntReviewReply> {
    val snapshot =
        db.collection(HUNT_REVIEW_REPLY_COLLECTION_PATH)
            .whereEqualTo("reviewId", reviewId)
            .get()
            .await()

    return snapshot.documents.mapNotNull { documentToReply(it) }
  }

  override suspend fun addReply(reply: HuntReviewReply) {
    require(reply.replyId.isNotBlank()) { "Reply ID cannot be blank." }

    db.collection(HUNT_REVIEW_REPLY_COLLECTION_PATH).document(reply.replyId).set(reply).await()
  }

  override suspend fun updateReply(replyId: String, newReply: HuntReviewReply) {
    db.collection(HUNT_REVIEW_REPLY_COLLECTION_PATH).document(replyId).set(newReply).await()
  }

  override suspend fun deleteReply(replyId: String) {
    db.collection(HUNT_REVIEW_REPLY_COLLECTION_PATH).document(replyId).delete().await()
  }

  override fun listenToReplies(reviewId: String): Flow<List<HuntReviewReply>> = callbackFlow {
    val registration =
        db.collection(HUNT_REVIEW_REPLY_COLLECTION_PATH)
            .whereEqualTo("reviewId", reviewId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                Log.e(
                    "HuntReviewReplyRepositoryFirestore",
                    "listenToReplies error",
                    error,
                )
                return@addSnapshotListener
              }

              if (snapshot != null) {
                trySend(snapshot.documents.mapNotNull(::documentToReply))
              }
            }

    awaitClose { registration.remove() }
  }

  @VisibleForTesting
  internal fun documentToReply(document: DocumentSnapshot): HuntReviewReply? {
    return try {
      val replyId = document.id
      val reviewId = document.getString("reviewId") ?: return null
      val parentReplyId = document.getString("parentReplyId")
      val authorId = document.getString("authorId") ?: return null
      val comment = document.getString("comment") ?: ""
      val createdAt = document.getLong("createdAt") ?: 0L
      val updatedAt = document.getLong("updatedAt")
      val isDeleted = document.getBoolean("isDeleted") ?: false

      HuntReviewReply(
          replyId = replyId,
          reviewId = reviewId,
          parentReplyId = parentReplyId,
          authorId = authorId,
          comment = comment,
          createdAt = createdAt,
          updatedAt = updatedAt,
          isDeleted = isDeleted,
      )
    } catch (e: Exception) {
      Log.e(
          "HuntReviewReplyRepositoryFirestore",
          "Error converting document to HuntReviewReply",
          e,
      )
      null
    }
  }
}
