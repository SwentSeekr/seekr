package com.swentseekr.seekr.model.hunt

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class HuntsImageRepository(private val storage: FirebaseStorage = FirebaseStorage.getInstance()) :
    IHuntsImageRepository {

  private val rootRef = storage.reference.child("hunts_images")

  override suspend fun uploadMainImage(huntId: String, imageUri: Uri): String {
    val ref = rootRef.child("$huntId/main_${System.currentTimeMillis()}.jpg")
    ref.putFile(imageUri).await()
    return ref.downloadUrl.await().toString()
  }

  override suspend fun uploadOtherImages(huntId: String, imageUris: List<Uri>): List<String> {
    val urls = mutableListOf<String>()
    for (u in imageUris) {
      val ref =
          rootRef.child(
              "$huntId/other_${System.currentTimeMillis()}_${u.lastPathSegment ?: "img"}.jpg")
      ref.putFile(u).await()
      urls += ref.downloadUrl.await().toString()
    }
    return urls
  }

  override suspend fun deleteAllHuntImages(huntId: String) {
    try {
      val folder = rootRef.child(huntId)
      val list = folder.listAll().await()
      list.items.forEach { it.delete().await() }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
