package com.swentseekr.seekr.utils

import android.net.Uri
import com.swentseekr.seekr.model.hunt.IHuntsImageRepository

/** A fake implementation of [IHuntsImageRepository] for testing purposes. */
class FakeHuntsImageRepository(var shouldFail: Boolean = false) : IHuntsImageRepository {

  val uploadedMainImages = mutableMapOf<String, String>()
  val uploadedOtherImages = mutableMapOf<String, MutableList<String>>()
  val deletedImages = mutableListOf<String>()
  val deletedAllForHunt = mutableListOf<String>()

  override suspend fun uploadMainImage(huntId: String, imageUri: Uri): String {
    if (shouldFail) throw Exception("Simulated upload main image failure")

    val url = "fake://main_image_url_for_$huntId"
    uploadedMainImages[huntId] = url
    return url
  }

  override suspend fun uploadOtherImages(huntId: String, imageUris: List<Uri>): List<String> {
    if (shouldFail) throw Exception("Simulated upload other images failure")

    val urls = imageUris.mapIndexed { i, _ -> "fake://other_image_${i}_for_$huntId" }

    val list = uploadedOtherImages.getOrPut(huntId) { mutableListOf() }
    list += urls

    return urls
  }

  override suspend fun deleteImageByUrl(url: String) {
    if (shouldFail) throw Exception("Simulated delete image failure")

    deletedImages += url
  }

  override suspend fun deleteAllHuntImages(huntId: String) {
    if (shouldFail) throw Exception("Simulated delete all images failure")

    deletedAllForHunt += huntId
    uploadedMainImages.remove(huntId)
    uploadedOtherImages.remove(huntId)
  }
}
