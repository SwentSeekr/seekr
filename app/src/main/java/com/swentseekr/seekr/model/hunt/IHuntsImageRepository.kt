package com.swentseekr.seekr.model.hunt

import android.net.Uri

interface IHuntsImageRepository {
  suspend fun uploadMainImage(huntId: String, imageUri: Uri): String

  suspend fun uploadOtherImages(huntId: String, imageUris: List<Uri>): List<String>

  suspend fun deleteAllHuntImages(huntId: String)
}
