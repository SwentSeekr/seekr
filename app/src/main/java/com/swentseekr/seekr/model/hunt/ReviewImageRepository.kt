package com.swentseekr.seekr.model.hunt

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Firebase Storage implementation of [IReviewImageRepository].
 *
 * Manages uploading and deleting review photos associated with hunts.
 *
 * @property storage The [FirebaseStorage] instance used to store review images.
 */
class ReviewImageRepository(private val storage: FirebaseStorage = FirebaseStorage.getInstance()) :
    IReviewImageRepository {

  /** Root reference for review images in Firebase Storage. */
  private val rootRef =
      storage.reference.child(ReviewImageRepositoryConstants.REVIEW_IMAGES_COLLECTION)

  /**
   * Uploads a review photo to Firebase Storage.
   *
   * The image is stored under a path combining the user ID and a timestamp to ensure uniqueness.
   *
   * @param userId The ID of the user who owns the photo.
   * @param uri The local URI of the photo to upload.
   * @return The public download URL of the uploaded photo.
   * @throws Exception if the upload fails.
   */
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

  /**
   * Deletes a review photo from Firebase Storage.
   *
   * If the URL is empty, the operation is skipped. Failures are logged but do not throw.
   *
   * @param url The public download URL of the photo to delete.
   */
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
