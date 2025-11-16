package com.swentseekr.seekr.model.hunt

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class HuntsImageRepository(private val storage: FirebaseStorage = FirebaseStorage.getInstance()) :
    IHuntsImageRepository {

  private val rootRef = storage.reference.child("hunts_images")

  override suspend fun uploadMainImage(huntId: String, imageUri: Uri): String {
    val ref = rootRef.child("$huntId/main_${System.currentTimeMillis()}.jpg")
    ref.putFile(imageUri).await()
    return ref.downloadUrl.await().toString()
  }

  override suspend fun uploadOtherImages(huntId: String, imageUris: List<Uri>): List<String> =
      coroutineScope {
        // Limit to 5 parallel uploads at a time
        val semaphore = Semaphore(5)

        val uploadJobs =
            imageUris.map { uri ->
              async {
                semaphore.withPermit {
                  val ref =
                      rootRef.child(
                          "$huntId/other_${System.currentTimeMillis()}_${uri.lastPathSegment ?: "img"}.jpg")

                  // Upload the file
                  ref.putFile(uri).await()

                  // Retrieve and return the download URL
                  ref.downloadUrl.await().toString()
                }
              }
            }

        // Wait for all uploads to complete and return their URLs
        uploadJobs.awaitAll()
      }

  override suspend fun deleteAllHuntImages(huntId: String) {
    try {
      val folder = rootRef.child(huntId)
      // Avoid infity waits by setting a timeout for listing files
      val list = withTimeoutOrNull(2_000) { folder.listAll().await() }

      if (list != null) {
        list.items.forEach {
          runCatching { it.delete().await() }
              .onFailure { e ->
                Log.w("HuntsImageRepository", "Failed to delete ${it.name}: ${e.message}")
              }
        }
      } else {
        Log.w(
            "HuntsImageRepository", "Timeout listing images for hunt $huntId (likely empty folder)")
      }
    } catch (e: Exception) {
      Log.w("HuntsImageRepository", "Unexpected error deleting images for $huntId: ${e.message}")
    }
  }

  override suspend fun deleteImageByUrl(url: String) {
    try {
      val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
      ref.delete().await()
    } catch (e: Exception) {
      Log.w("HuntsRepositoryFirestore", "Failed to delete image: $url", e)
    }
  }
}
