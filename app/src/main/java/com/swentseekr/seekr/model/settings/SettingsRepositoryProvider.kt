package com.swentseekr.seekr.model.settings

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Singleton object providing a single instance of [SettingsRepositoryFirestore] for the app.
 *
 * The repository is lazily initialized to ensure Firestore and Auth instances are available when
 * first used.
 */
object SettingsRepositoryProvider {
  private val _repositoryFirestore by lazy {
    SettingsRepositoryFirestore(
        firestore = FirebaseFirestore.getInstance(), auth = FirebaseAuth.getInstance())
  }

  val repository: SettingsRepositoryFirestore = _repositoryFirestore
}
