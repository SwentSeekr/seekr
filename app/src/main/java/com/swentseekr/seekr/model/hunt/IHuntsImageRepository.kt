package com.swentseekr.seekr.model.hunt

import android.net.Uri

/**
 * Defines the contract for managing image storage related to hunts.
 *
 * Implementations of this interface handle uploading and deleting images associated with individual
 * hunts. Typically, this involves uploading to a cloud storage provider (e.g., Firebase Storage)
 * and returning public URLs that can be persisted in Firestore or other databases.
 */
interface IHuntsImageRepository {

  /**
   * Uploads the main image for a given hunt.
   *
   * This image usually serves as the hunt’s cover or thumbnail and replaces any previously uploaded
   * main image for the same hunt ID.
   *
   * @param huntId The unique identifier of the hunt (used to determine the storage path).
   * @param imageUri The [Uri] of the image to upload.
   * @return The public download URL of the uploaded main image.
   * @throws Exception If the upload fails (e.g., network error, permission denied).
   */
  suspend fun uploadMainImage(huntId: String, imageUri: Uri): String

  /**
   * Uploads a list of additional images associated with a hunt.
   *
   * Each image is uploaded in parallel, and the function returns when all uploads have completed
   * successfully.
   *
   * @param huntId The unique identifier of the hunt (used to determine the storage path).
   * @param imageUris The list of [Uri] objects to upload.
   * @return A list of public download URLs corresponding to the uploaded images, in the same order
   *   as [imageUris].
   * @throws Exception If one or more uploads fail. Implementations may handle partial success
   *   internally (e.g., by cleaning up incomplete uploads).
   */
  suspend fun uploadOtherImages(huntId: String, imageUris: List<Uri>): List<String>

  /**
   * Deletes all images (main and additional) associated with a given hunt.
   *
   * Implementations should remove all files stored under the directory or prefix for the specified
   * [huntId].
   *
   * @param huntId The unique identifier of the hunt whose images should be deleted.
   * @throws Exception If deletion fails (e.g., network error or missing permissions).
   */
  suspend fun deleteAllHuntImages(huntId: String)

  /**
   * Deletes a single image identified by its public URL.
   *
   * Implementations should remove the file corresponding to the provided public download URL from
   * the storage backend. If the URL points to an image that does not exist, implementations may
   * either succeed silently or throw an exception according to their policy.
   *
   * @param url The public download URL of the image to delete.
   * @throws Exception If the deletion fails (e.g., network error, permission denied).
   */
  suspend fun deleteImageByUrl(url: String)
}
