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

/**
 * Firebase Storage implementation of [IHuntsImageRepository] that handles uploading and deletion of
 * hunt-related images in Firebase Storage.
 *
 * Storage structure:
 * - hunts_images/{huntId}/main_<timestamp>.jpg
 * - hunts_images/{huntId}/other_<timestamp>_<name>.jpg
 *
 * @property storage The Firebase Storage instance used for image operations.
 */
class HuntsImageRepository(private val storage: FirebaseStorage = FirebaseStorage.getInstance()) :
    IHuntsImageRepository {

  /** Root reference for all hunt images in Firebase Storage. */
  private val rootRef = storage.reference.child(HuntsImageRepositoryConstantsString.PATH)

  /**
   * Uploads the main image for a hunt that is stored under the hunt’s folder with a timestamp-based
   * filename.
   *
   * @param huntId The ID of the hunt.
   * @param imageUri The URI of the image to upload.
   * @return The public download URL of the uploaded image.
   */
  override suspend fun uploadMainImage(huntId: String, imageUri: Uri): String {
    val ref =
        rootRef.child(
            "$huntId${HuntsImageRepositoryConstantsString.MAIN}${System.currentTimeMillis()}${HuntsImageRepositoryConstantsString.FORMAT}")
    ref.putFile(imageUri).await()
    return ref.downloadUrl.await().toString()
  }

  /**
   * Uploads additional images for a hunt concurrently, with a maximum of 5 parallel uploads to
   * avoid overwhelming network or storage resources.
   *
   * @param huntId The ID of the hunt.
   * @param imageUris A list of image URIs to upload.
   * @return A list of download URLs corresponding to the uploaded images.
   */
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
                          "$huntId${HuntsImageRepositoryConstantsString.OTHER}${System.currentTimeMillis()}${HuntsImageRepositoryConstantsString.UNDERSCORE}${uri.lastPathSegment ?: HuntsImageRepositoryConstantsString.IMAGE}${HuntsImageRepositoryConstantsString.FORMAT}")

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

  /**
   * Deletes all images associated with a hunt.
   *
   * Attempts to list and delete all files under the hunt’s image folder. A timeout is applied to
   * avoid indefinite blocking.
   *
   * Failures are logged but do not throw exceptions.
   *
   * @param huntId The ID of the hunt whose images should be deleted.
   */
  override suspend fun deleteAllHuntImages(huntId: String) {
    try {
      val folder = rootRef.child(huntId)
      // Avoid infity waits by setting a timeout for listing files
      val list =
          withTimeoutOrNull(HuntsImageRepositoryConstantsDefault.TIME_OUT) {
            folder.listAll().await()
          }

      if (list != null) {
        list.items.forEach {
          runCatching { it.delete().await() }
              .onFailure { e ->
                Log.w(
                    HuntsImageRepositoryConstantsString.TAG,
                    "${HuntsImageRepositoryConstantsString.ERROR_DELETE} ${it.name}: ${e.message}")
              }
        }
      } else {
        Log.w(
            HuntsImageRepositoryConstantsString.TAG,
            "${HuntsImageRepositoryConstantsString.ERROR_TIMEOUT} $huntId ${HuntsImageRepositoryConstantsString.ERROR_POSSIBLE}")
      }
    } catch (e: Exception) {
      Log.w(
          HuntsImageRepositoryConstantsString.TAG,
          "${HuntsImageRepositoryConstantsString.ERROR_UNEXPECTED} $huntId: ${e.message}")
    }
  }

  /**
   * Deletes a single image using its download URL.
   *
   * If the deletion fails, the error is logged and the operation completes silently.
   *
   * @param url The Firebase Storage download URL of the image to delete.
   */
  override suspend fun deleteImageByUrl(url: String) {
    try {
      val ref = storage.getReferenceFromUrl(url)
      ref.delete().await()
    } catch (e: Exception) {
      Log.w(
          HuntsImageRepositoryConstantsString.TAG_FIRESTORE,
          "${HuntsImageRepositoryConstantsString.ERROR_DELETING} $url",
          e)
    }
  }
}
