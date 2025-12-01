package com.swentseekr.seekr.model.hunt

open class ReviewImageRepositoryLocal : IReviewImageRepository {
  private var id = 0

  override suspend fun uploadReviewPhoto(userId: String, uri: android.net.Uri): String {
    // In a local repository, we can simulate the upload by returning a fake URL.
    return "${ReviewImageRepositoryConstants.PATH_LOCALE}${userId}_${id++}"
  }

  override suspend fun deleteReviewPhoto(url: String) {
    // In a local repository, there's no actual deletion needed.
    // This method can be left empty or log the deletion action if necessary.
  }
}
