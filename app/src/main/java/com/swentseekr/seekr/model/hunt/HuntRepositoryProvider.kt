package com.swentseekr.seekr.model.hunt

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object HuntRepositoryProvider {
  /** Firestore-backed repository lazily initialized for production use. */
  private val _repositoryFirestore: HuntsRepository by lazy {
    HuntsRepositoryFirestore(Firebase.firestore)
  }
  var repository: HuntsRepository = _repositoryFirestore
}
