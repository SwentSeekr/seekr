package com.swentseekr.seekr.model.hunt

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

object HuntReviewRepositoryProvider {

  private val _repositoryFirestore: HuntReviewRepository by lazy {
    HuntReviewRepositoryFirestore(Firebase.firestore)
  }
  var repository: HuntReviewRepository = _repositoryFirestore
}
