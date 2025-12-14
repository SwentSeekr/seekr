package com.swentseekr.seekr.model.hunt

import android.net.Uri

/**
 * In-memory implementation of [HuntsRepository].
 *
 * This repository stores hunts in a local mutable list and is mainly intended for:
 * - Testing
 * - Prototyping
 * - Offline or non-persistent usage
 *
 * Images are deliberately ignored in this implementation. All data is lost when the repository
 * instance is destroyed.
 */
class HuntsRepositoryLocal : HuntsRepository {

  /** Internal storage of hunts. */
  private val hunts = mutableListOf<Hunt>()

  /** Counter used to generate unique hunt identifiers. */
  private var id = 0

  /**
   * Generates a new unique hunt identifier.
   *
   * @return A locally generated unique ID.
   */
  override fun getNewUid(): String {
    return (id++).toString()
  }

  /**
   * Retrieves all hunts stored locally.
   *
   * @return A list of all hunts.
   */
  override suspend fun getAllHunts(): List<Hunt> {
    return hunts.toList()
  }

  /**
   * Retrieves all hunts authored by the given user.
   *
   * @param authorID The ID of the hunt author.
   * @return A list of hunts created by the specified author.
   */
  override suspend fun getAllMyHunts(authorID: String): List<Hunt> {
    return hunts.filter { it.authorId == authorID }
  }

  /**
   * Retrieves a single hunt by its ID.
   *
   * @param huntID The ID of the hunt to retrieve.
   * @return The corresponding [Hunt].
   * @throws IllegalArgumentException if the hunt does not exist.
   */
  override suspend fun getHunt(huntID: String): Hunt {
    return hunts.find { it.uid == huntID }
        ?: throw IllegalArgumentException("Hunt with ID $huntID is not found")
  }

  /**
   * Adds a new hunt to the local repository.
   *
   * Image parameters are ignored, as this implementation does not manage images.
   *
   * @param hunt The hunt to add.
   * @param mainImageUri Ignored.
   * @param otherImageUris Ignored.
   */
  override suspend fun addHunt(hunt: Hunt, mainImageUri: Uri?, otherImageUris: List<Uri>) {
    hunts.add(hunt)
  }

  /**
   * Updates an existing hunt.
   *
   * Image-related parameters are ignored.
   *
   * @param huntID The ID of the hunt to update.
   * @param newValue The new hunt data.
   * @throws IllegalArgumentException if the hunt does not exist.
   */
  override suspend fun editHunt(
      huntID: String,
      newValue: Hunt,
      mainImageUri: Uri?,
      addedOtherImages: List<Uri>,
      removedOtherImages: List<String>,
      removedMainImageUrl: String?
  ) {
    val index = hunts.indexOfFirst { it.uid == huntID }
    if (index != -1) {
      hunts[index] = newValue
    } else {
      throw IllegalArgumentException("Hunt with ID $huntID is not found")
    }
  }

  /**
   * Deletes a hunt from the local repository.
   *
   * @param huntID The ID of the hunt to delete.
   * @throws IllegalArgumentException if the hunt does not exist.
   */
  override suspend fun deleteHunt(huntID: String) {
    val wasRemoved = hunts.removeIf { it.uid == huntID }
    if (!wasRemoved) {
      throw IllegalArgumentException("Hunt with ID $huntID is not found")
    }
  }
}
