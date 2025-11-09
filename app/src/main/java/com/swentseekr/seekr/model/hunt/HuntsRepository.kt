package com.swentseekr.seekr.model.hunt

/** Represents a repository that manages Hunt items. */
interface HuntsRepository {
  /** Generates and returns a new unique identifier for a Hunt item. */
  fun getNewUid(): String

  /**
   * Retrieves all Hunt items from the repository.
   *
   * @return A list of all Hunt items.
   */
  suspend fun getAllHunts(): List<Hunt>

  /**
   * Retrieves all Hunt items created by a specific author.
   *
   * @param authorID The unique identifier of the author whose hunts are to be retrieved.
   * @return A list of Hunt items created by the specified author.
   */
  suspend fun getAllMyHunts(authorID: String): List<Hunt>

  /**
   * Retrieves a specific Hunt item by its unique identifier.
   *
   * @param huntID The unique identifier of the Hunt item to retrieve.
   * @return The Hunt item with the specified identifier.
   * @throws Exception if the Hunt item is not found.
   */
  suspend fun getHunt(huntID: String): Hunt

  /**
   * Adds a Hunt by managing its images.
   *
   * @param hunt the hunt to retrieve.
   * @param mainImageUri URI optional of principal images (can be `null`).
   * @param otherImageUris List of secondary image URIs (may be empty).
   *
   * The function is suspended: it must upload/associate the images
   * then persist the Hunt. May raise exceptions in case of upload or storage errors.
   */
  suspend fun addHunt(hunt: Hunt, mainImageUri: android.net.Uri? = null,
                      otherImageUris: List<android.net.Uri> = emptyList())


  /**
   * Edits an existing Hunt item in the repository.
   *
   * @param huntID The unique identifier of the item to edit.
   * @param newValue The new value for the Hunt item.
   * @throws Exception if the Hunt item is not found.
   */
  suspend fun editHunt(huntID: String, newValue: Hunt)

  /**
   * Deletes a Hunt item from the repository.
   *
   * @param huntID The unique identifier of the item to delete.
   * @throws Exception if the Hunt item is not found.
   */
  suspend fun deleteHunt(huntID: String)
}
