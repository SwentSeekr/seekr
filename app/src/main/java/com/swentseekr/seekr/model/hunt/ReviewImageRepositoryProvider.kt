package com.swentseekr.seekr.model.hunt

import com.google.firebase.Firebase
import com.google.firebase.storage.storage

/**
 * Provides a singleton instance of [IReviewImageRepository].
 *
 * Uses [ReviewImageRepository] backed by Firebase Storage. The repository is lazily initialized on
 * first access.
 */
object ReviewImageRepositoryProvider {

  /** Singleton repository instance for uploading and deleting review images. */
  val repository: IReviewImageRepository by lazy { ReviewImageRepository(Firebase.storage) }
}
