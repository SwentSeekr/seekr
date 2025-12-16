package com.swentseekr.seekr.model.hunt

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides a singleton instance of [HuntReviewRepository].
 *
 * By default, this provider exposes a Firestore-backed implementation
 * ([HuntReviewRepositoryFirestore]).
 *
 * The [repository] property is mutable to allow dependency substitution in tests or alternative
 * implementations.
 */
object HuntReviewRepositoryProvider {

  /**
   * Default Firestore implementation of [HuntReviewRepository].
   *
   * Lazily initialized to avoid unnecessary Firebase setup.
   */
  private val _repositoryFirestore: HuntReviewRepository by lazy {
    HuntReviewRepositoryFirestore(Firebase.firestore)
  }

  /**
   * The active [HuntReviewRepository] instance used by the application.
   *
   * Can be overridden for testing or local development.
   */
  var repository: HuntReviewRepository = _repositoryFirestore
}
