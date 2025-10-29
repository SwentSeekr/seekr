package com.swentseekr.seekr.model.hunt

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

const val HUNT_REVIEW_COLLECTION_PATH = "huntsReviews"

class HuntReviewRepositoryFirestore(private val db: FirebaseFirestore) : HuntReviewRepository {
  override fun getNewUid(): String {
    return db.collection(HUNT_REVIEW_COLLECTION_PATH).document().id
  }

  override suspend fun getReviewHunt(reviewID: String): HuntReview? {
    val document = db.collection(HUNT_REVIEW_COLLECTION_PATH).document(reviewID).get().await()
    return documentToHuntReview(document)
        ?: throw IllegalArgumentException("Hunt with ID ${reviewID} is not found")
  }

  override suspend fun addReviewHunt(review: HuntReview) {
    db.collection(HUNT_REVIEW_COLLECTION_PATH).document(review.reviewID).set(review).await()
  }

  override suspend fun updateReviewHunt(reviewID: String, newReview: HuntReview) {
    db.collection(HUNT_REVIEW_COLLECTION_PATH).document(reviewID).set(newReview).await()
  }

  override suspend fun deleteReviewHunt(reviewID: String) {
    db.collection(HUNT_REVIEW_COLLECTION_PATH).document(reviewID).delete().await()
  }

  override suspend fun getHuntReviews(huntID: String): List<HuntReview> {
    // Implementation for fetching hunt reviews
    // val currentUserId =
    //    FirebaseAuth.getInstance().currentUser?.uid
    //       ?: throw IllegalStateException("User not logged in")
    val snapshot =
        db.collection(HUNT_REVIEW_COLLECTION_PATH).whereEqualTo("huntID", huntID).get().await()
    return snapshot.mapNotNull { documentToHuntReview(it) }
  }

  private fun documentToHuntReview(document: DocumentSnapshot): HuntReview? {
    return try {
      val reviewID = document.id
      val authorID = document.getString("authorID") ?: return null
      val huntID = document.getString("huntID") ?: return null
      val rating = document.getDouble("rating") ?: return null
      val comment = document.getString("comment") ?: return null
      val photosData = document.get("photos") as? List<Map<String, Any>> ?: emptyList()
      val photos =
          photosData.map {
            PhotoFile(
                url = it["url"] as? String ?: "", description = it["description"] as? String ?: "")
          }
      HuntReview(
          reviewID = reviewID,
          authorID = authorID,
          huntID = huntID,
          rating = rating,
          comment = comment,
          photos = photos)
    } catch (e: Exception) {
      Log.e("HuntReviewRepositoryFirestore", "Error converting document to HuntReview", e)
      null
    }
  }
}
