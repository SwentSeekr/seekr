package com.swentseekr.seekr.model.hunt

import android.net.Uri

/** Repository interface for managing images associated with hunt reviews. */
interface IReviewImageRepository {

  /**
   * Uploads a review photo for a given user.
   *
   * @param userId The ID of the user who owns the photo.
   * @param uri The local URI of the photo to upload.
   * @return The public URL of the uploaded photo.
   * @throws Exception if the upload fails.
   */
  suspend fun uploadReviewPhoto(userId: String, uri: Uri): String

  /**
   * Deletes a previously uploaded review photo.
   *
   * @param url The public URL of the photo to delete.
   * @throws Exception if the deletion fails.
   */
  suspend fun deleteReviewPhoto(url: String)
}
