package com.swentseekr.seekr.model.hunt.review

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Provides a singleton instance of [HuntReviewReplyRepository].
 *
 * By default, it uses [HuntReviewReplyRepositoryFirestore] backed by Firebase Firestore.
 */
object HuntReviewReplyRepositoryProvider {

  @Volatile private var overrideRepository: HuntReviewReplyRepository? = null

  private val defaultRepository: HuntReviewReplyRepository by lazy {
    HuntReviewReplyRepositoryFirestore(FirebaseFirestore.getInstance())
  }

  /** Public access point for the repository. In tests, this will return the override if set. */
  val repository: HuntReviewReplyRepository
    get() = overrideRepository ?: defaultRepository

  /**
   * Sets a custom repository for unit tests.
   *
   * @param repo The repository to use during testing.
   */
  fun setTestRepository(repo: HuntReviewReplyRepository) {
    overrideRepository = repo
  }

  /** Clears the test repository override and goes back to the default. */
  fun clearTestRepository() {
    overrideRepository = null
  }
}
