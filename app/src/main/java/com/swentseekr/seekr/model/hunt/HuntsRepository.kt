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
   * Retrieves a specific Hunt item by its unique identifier.
   *
   * @param huntID The unique identifier of the Hunt item to retrieve.
   * @return The Hunt item with the specified identifier.
   * @throws Exception if the Hunt item is not found.
   */
  suspend fun getHunt(huntID: String): Hunt

  /**
   * Adds a new Hunt item to the repository.
   *
   * @param hunt The Hunt item to add.
   */
  suspend fun addHunt(hunt: Hunt)

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
