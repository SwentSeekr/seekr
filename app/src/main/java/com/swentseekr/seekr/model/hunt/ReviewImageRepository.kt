package com.swentseekr.seekr.model.hunt

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ReviewImageRepository(private val storage: FirebaseStorage = FirebaseStorage.getInstance()) :
    IReviewImageRepository {
  private val rootRef =
      storage.reference.child(ReviewImageRepositoryConstants.REVIEW_IMAGES_COLLECTION)

  override suspend fun uploadReviewPhoto(userId: String, uri: android.net.Uri): String {
    val ref =
        rootRef.child(
            "${userId}_${System.currentTimeMillis()}${ReviewImageRepositoryConstants.FORMAT_JPG}")
    try {
      ref.putFile(uri).await()
      return ref.downloadUrl.await().toString()
    } catch (e: Exception) {
      Log.e(
          ReviewImageRepositoryConstants.TAG,
          "${ReviewImageRepositoryConstants.UPLOAD_IMAGE_ERROR} $userId",
          e)
      throw e
    }
  }

  override suspend fun deleteReviewPhoto(url: String) {
    if (url.isNotEmpty()) {
      try {
        storage.getReferenceFromUrl(url).delete().await()
      } catch (e: Exception) {
        Log.e(
            ReviewImageRepositoryConstants.TAG,
            "${ReviewImageRepositoryConstants.DELETE_IMAGE_ERROR} $url",
            e)
      }
    }
  }
}
