package com.swentseekr.seekr.model.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestore

/**
 * Provides a singleton instance of [ProfileRepository] for the application.
 *
 * This object lazily initializes a [ProfileRepositoryFirestore] using Firebase services.
 */
object ProfileRepositoryProvider {
  private val _repositoryFirestore: ProfileRepository by lazy {
    ProfileRepositoryFirestore(
        db = FirebaseFirestore.getInstance(),
        auth = FirebaseAuth.getInstance(),
        huntsRepository = HuntsRepositoryFirestore(FirebaseFirestore.getInstance()),
        storage = FirebaseStorage.getInstance())
  }

  var repository: ProfileRepository = _repositoryFirestore
}
