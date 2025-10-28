package com.swentseekr.seekr.model.profile

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestore

object ProfileRepositoryProvider {
  private val _repositoryFirestore: ProfileRepository by lazy {
    ProfileRepositoryFirestore(Firebase.firestore)
  }
  var repository: ProfileRepository = _repositoryFirestore
}
