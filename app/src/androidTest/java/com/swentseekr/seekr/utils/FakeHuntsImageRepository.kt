package com.swentseekr.seekr.utils

import android.net.Uri
import com.swentseekr.seekr.model.hunt.IHuntsImageRepository

class FakeHuntsImageRepository : IHuntsImageRepository {

  override suspend fun uploadMainImage(huntId: String, imageUri: Uri): String {
    // Simule un upload instantané
    return "fake://main_image_url_for_$huntId"
  }

  override suspend fun uploadOtherImages(huntId: String, imageUris: List<Uri>): List<String> {
    // Simule plusieurs URLs
    return imageUris.mapIndexed { i, _ -> "fake://other_image_${i}_for_$huntId" }
  }

  override suspend fun deleteAllHuntImages(huntId: String) {
    // Ne fait rien
  }
}
