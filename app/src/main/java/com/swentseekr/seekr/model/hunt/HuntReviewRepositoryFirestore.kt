package com.swentseekr.seekr.model.hunt

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

const val HUNT_REVIEW_COLLECTION_PATH = "huntsReviews"

class HuntReviewRepositoryFirestore(private val db: FirebaseFirestore) : HuntReviewRepository {
  override fun getNewUid(): String {
    return db.collection(HUNT_REVIEW_COLLECTION_PATH).document().id
  }

  override suspend fun getReviewHunt(reviewId: String): HuntReview {
    val document = db.collection(HUNT_REVIEW_COLLECTION_PATH).document(reviewId).get().await()
    return documentToHuntReview(document)
        ?: throw IllegalArgumentException("Hunt with Id ${reviewId} is not found")
  }

  override suspend fun addReviewHunt(review: HuntReview) {
    require(review.reviewId.isNotBlank()) { "Review ID cannot be blank." }
    db.collection(HUNT_REVIEW_COLLECTION_PATH).document(review.reviewId).set(review).await()
  }

  override suspend fun updateReviewHunt(reviewId: String, newReview: HuntReview) {
    db.collection(HUNT_REVIEW_COLLECTION_PATH).document(reviewId).set(newReview).await()
  }

  override suspend fun deleteReviewHunt(reviewId: String) {
    db.collection(HUNT_REVIEW_COLLECTION_PATH).document(reviewId).delete().await()
  }

  override suspend fun getHuntReviews(huntId: String): List<HuntReview> {
    val currentUserId =
        FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not logged in")
    val snapshot =
        db.collection(HUNT_REVIEW_COLLECTION_PATH).whereEqualTo("huntId", huntId).get().await()
    return snapshot.mapNotNull { documentToHuntReview(it) }
  }

  private fun documentToHuntReview(document: DocumentSnapshot): HuntReview? {
    return try {
      val reviewID = document.id
      val authorID = document.getString("authorId") ?: return null
      val huntID = document.getString("huntId") ?: return null
      val rating = document.getDouble("rating") ?: return null
      val comment = document.getString("comment") ?: return null
      val photosData = document.get("photos") as? List<Map<String, Any>> ?: emptyList()
      val photos =
          photosData.map {
            PhotoFile(
                url = it["url"] as? String ?: "", description = it["description"] as? String ?: "")
          }
      HuntReview(
          reviewId = reviewID,
          authorId = authorID,
          huntId = huntID,
          rating = rating,
          comment = comment,
          photos = photos)
    } catch (e: Exception) {
      Log.e("HuntReviewRepositoryFirestore", "Error converting document to HuntReview", e)
      null
    }
  }
}
