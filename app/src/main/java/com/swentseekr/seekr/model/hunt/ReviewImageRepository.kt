package com.swentseekr.seekr.model.hunt

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ReviewImageRepository(private val storage: FirebaseStorage = FirebaseStorage.getInstance()) :
    IReviewImageRepository {
  private val rootRef = storage.reference.child("review_photos")

  private fun FirebaseStorage.fromDownloadUrl(url: String) =
      this.getReference(url.substringAfter("/o/").substringBefore("?").replace("%2F", "/"))

  override suspend fun uploadReviewPhoto(userId: String, uri: android.net.Uri): String {
    val ref = rootRef.child("${userId}_${System.currentTimeMillis()}.jpg")
    ref.putFile(uri).await()
    return ref.downloadUrl.await().toString()
  }

  override suspend fun deleteReviewPhoto(url: String) {
    val ref = storage.fromDownloadUrl(url)
    // val ref = storage.getReferenceFromUrl(url)
    ref.delete().await()
  }
}
