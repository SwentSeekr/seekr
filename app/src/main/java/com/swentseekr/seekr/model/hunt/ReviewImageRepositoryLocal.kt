package com.swentseekr.seekr.model.hunt

/**
 * Local in-memory implementation of [IReviewImageRepository].
 *
 * This repository is mainly intended for testing or prototyping. No actual image upload or deletion
 * is performed. Uploads return a simulated local URL, and deletions are ignored.
 */
open class ReviewImageRepositoryLocal : IReviewImageRepository {
  private var id = 0

  /**
   * Simulates uploading a review photo.
   *
   * Returns a locally generated URL using [ReviewImageRepositoryConstants.PATH_LOCALE], the user
   * ID, and a unique counter.
   *
   * @param userId The ID of the user who "owns" the photo.
   * @param uri Ignored in this implementation.
   * @return A simulated local URL representing the uploaded photo.
   */
  override suspend fun uploadReviewPhoto(userId: String, uri: android.net.Uri): String {
    return "${ReviewImageRepositoryConstants.PATH_LOCALE}${userId}_${id++}"
  }

  /**
   * Simulates deleting a review photo.
   *
   * In this local repository, the method does nothing.
   *
   * @param url The URL of the photo to "delete". Ignored.
   */
  override suspend fun deleteReviewPhoto(url: String) {
    // No actual deletion needed for local simulation.
  }
}
