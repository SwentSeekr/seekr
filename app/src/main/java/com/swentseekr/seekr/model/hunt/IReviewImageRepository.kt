package com.swentseekr.seekr.model.hunt

import android.net.Uri

interface IReviewImageRepository {
  suspend fun uploadReviewPhoto(userId: String, uri: Uri): String

  suspend fun deleteReviewPhoto(url: String)
}
