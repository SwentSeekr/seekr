package com.swentseekr.seekr.model.hunt

import com.google.firebase.Firebase
import com.google.firebase.storage.storage

object ReviewImageRepositoryProvider {
  val repository: IReviewImageRepository by lazy { ReviewImageRepository(Firebase.storage) }
}
