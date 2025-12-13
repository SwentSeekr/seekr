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
        ?: throw IllegalArgumentException(
            "${HuntReviewRepositoryFirestoreConstantsStrings.HUNT_START} ${reviewId} ${HuntReviewRepositoryFirestoreConstantsStrings.NOT_FOUND}")
  }

  override suspend fun addReviewHunt(review: HuntReview) {
    require(review.reviewId.isNotBlank()) {
      HuntReviewRepositoryFirestoreConstantsStrings.REVIEW_NOT_BLANK
    }
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
            ?: throw IllegalStateException(
                HuntReviewRepositoryFirestoreConstantsStrings.USER_NOT_LONGIN)
    val snapshot =
        db.collection(HUNT_REVIEW_COLLECTION_PATH)
            .whereEqualTo(HuntReviewRepositoryFirestoreConstantsStrings.FIELD, huntId)
            .get()
            .await()
    return snapshot.mapNotNull { documentToHuntReview(it) }
  }

  private fun documentToHuntReview(document: DocumentSnapshot): HuntReview? {
    return try {
      val reviewID = document.id
      val authorID =
          document.getString(HuntReviewRepositoryFirestoreConstantsStrings.AUTHOR_ID) ?: return null
      val huntID =
          document.getString(HuntReviewRepositoryFirestoreConstantsStrings.FIELD) ?: return null
      val rating =
          document.getDouble(HuntReviewRepositoryFirestoreConstantsStrings.RATING) ?: return null
      val comment =
          document.getString(HuntReviewRepositoryFirestoreConstantsStrings.COMMENT) ?: return null
      val photos =
          document.get(HuntReviewRepositoryFirestoreConstantsStrings.PHOTOS) as? List<String>
              ?: emptyList()

      HuntReview(
          reviewId = reviewID,
          authorId = authorID,
          huntId = huntID,
          rating = rating,
          comment = comment,
          photos = photos)
    } catch (e: Exception) {
      Log.e(
          HuntReviewRepositoryFirestoreConstantsStrings.TAG,
          HuntReviewRepositoryFirestoreConstantsStrings.ERROR_MSG,
          e)
      null
    }
  }
}
