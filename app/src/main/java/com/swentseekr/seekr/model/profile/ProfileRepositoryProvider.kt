package com.swentseekr.seekr.model.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestore

object ProfileRepositoryProvider {
  private val _repositoryFirestore: ProfileRepository by lazy {
    ProfileRepositoryFirestore(
        db = FirebaseFirestore.getInstance(),
        auth = FirebaseAuth.getInstance(),
        huntsRepository = HuntsRepositoryFirestore(FirebaseFirestore.getInstance()))
  }

  var repository: ProfileRepository = _repositoryFirestore
}
