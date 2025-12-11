package com.swentseekr.seekr.ui.hunt.edit

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.ui.hunt.BaseHuntViewModel
import com.swentseekr.seekr.ui.hunt.HuntUIState

/**
 * ViewModel responsible for editing an existing [Hunt].
 *
 * This ViewModel:
 * - Loads an existing hunt from the [HuntsRepository].
 * - Maps persisted data into [HuntUIState] for the edit form.
 * - Builds updated [Hunt] instances from the current UI state.
 * - Handles image updates (main and other images) and removal.
 * - Provides an operation to permanently delete the current hunt.
 */
class EditHuntViewModel(
    repository: HuntsRepository = HuntRepositoryProvider.repository,
) : BaseHuntViewModel(repository) {

  /** Identifier of the hunt currently being edited. `null` until [load] succeeds. */
  private var huntId: String? = null

  /**
   * Optional URI of the new main image selected by the user.
   *
   * If `null`, the existing main image is kept.
   */
  var mainImageUri: Uri? = null

  /**
   * URLs of existing "other images" that are marked for deletion from storage.
   *
   * When the user removes an already stored image from the UI, its URL is added here so the
   * repository can delete it from Firebase Storage.
   *
   * This list is:
   * - Cleared whenever [load] is called.
   * - Snapshotted via [toList] when persisting, to avoid exposing internal mutability.
   */
  private val pendingDeletionUrls = mutableListOf<String>()

  private var pendingMainImageDeletionUrl: String? = null

  /**
   * Loads the hunt with the given [id] and initializes the UI state for editing.
   *
   * On success:
   * - Populates [_uiState] with the existing hunt data.
   * - Sets [huntId] so subsequent edit/delete operations know which hunt to target.
   * - Clears any pending deletion state ([pendingDeletionUrls]).
   *
   * On failure:
   * - Resets [huntId] to `null`.
   * - Exposes an error message via [setErrorMsg].
   */
  suspend fun load(id: String) {
    // Reset deletion state when loading a new hunt.
    pendingDeletionUrls.clear()

    try {
      val hunt = repository.getHunt(id)

      _uiState.value =
          _uiState.value.copy(
              title = hunt.title,
              description = hunt.description,
              points = listOf(hunt.start) + hunt.middlePoints + listOf(hunt.end),
              time = hunt.time.toString(),
              distance = hunt.distance.toString(),
              difficulty = hunt.difficulty,
              status = hunt.status,
              mainImageUrl = hunt.mainImageUrl,
              otherImagesUrls = hunt.otherImagesUrls,
              reviewRate = hunt.reviewRate,
          )

      huntId = id
    } catch (e: Exception) {
      huntId = null
      setErrorMsg("Failed to load hunt: ${e.message}")
    }
  }

  /**
   * Builds a [Hunt] instance from the provided [state] for persistence.
   *
   * The [huntId] must be set (i.e., [load] must have succeeded); otherwise an exception is thrown.
   * The author is resolved from [FirebaseAuth] and falls back to `"unknown"` if not authenticated.
   */
  override fun buildHunt(state: HuntUIState): Hunt {
    val id = requireNotNull(huntId) { "No hunt loaded to edit." }
    val authorId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

    return Hunt(
        uid = id,
        start = state.points.first(),
        end = state.points.last(),
        middlePoints = state.points.drop(1).dropLast(1),
        status = state.status!!,
        title = state.title,
        description = state.description,
        time = state.time.toDouble(),
        distance = state.distance.toDouble(),
        difficulty = state.difficulty!!,
        authorId = authorId,
        mainImageUrl = state.mainImageUrl,
        // Actual URLs will be updated after image uploads.
        otherImagesUrls = state.otherImagesUrls,
        reviewRate = state.reviewRate,
    )
  }

  /**
   * Marks an existing "other image" for removal and updates the UI state accordingly.
   *
   * The URL is:
   * - Added to [pendingDeletionUrls] so the repository can delete it from storage.
   * - Removed from [_uiState.value.otherImagesUrls] so it no longer appears in the UI.
   */
  override fun removeExistingOtherImage(url: String) {
    pendingDeletionUrls += url

    val newList = _uiState.value.otherImagesUrls - url
    _uiState.value = _uiState.value.copy(otherImagesUrls = newList)
  }

  /**
   * Removes the currently selected main image from the hunt.
   *
   * This function performs two actions:
   * 1. If a remote image exists (i.e., `mainImageUrl` is not blank), its URL is stored in
   *    [pendingMainImageDeletionUrl] so that it can be deleted from the backend later.
   * 2. Updates the UI state by clearing the main image preview with [updateMainImageUri].
   *
   * This ensures that the UI reflects the removal immediately while still allowing the repository
   * or ViewModel to handle the actual remote deletion when appropriate.
   */
  override fun removeMainImage() {
    val currentUrl = uiState.value.mainImageUrl
    if (currentUrl.isNotBlank()) {
      pendingMainImageDeletionUrl = currentUrl
    }

    // Remove from UI
    updateMainImageUri(null)
  }

  /**
   * Persists the edited [hunt] using the underlying [HuntsRepository].
   *
   * This method:
   * - Requires that a hunt has been loaded (i.e. [huntId] is not `null`).
   * - Applies main image updates via [mainImageUri] if provided.
   * - Uploads any newly added "other images".
   * - Deletes images referenced in [pendingDeletionUrls].
   *
   * After calling the repository, [pendingDeletionUrls] is cleared.
   */
  override suspend fun persist(hunt: Hunt) {
    val id = requireNotNull(huntId)

    val removedOtherImages = pendingDeletionUrls.toList()
    val removedMain = pendingMainImageDeletionUrl

    repository.editHunt(
        huntID = id,
        newValue = hunt,
        mainImageUri = mainImageUri,
        addedOtherImages = otherImagesUris,
        removedOtherImages = removedOtherImages,
        removedMainImageUrl = removedMain)

    pendingDeletionUrls.clear()
    pendingMainImageDeletionUrl = null
  }

  /**
   * Permanently deletes the currently loaded hunt, including:
   * - The Firestore document.
   * - All associated images from storage.
   *
   * If no hunt has been loaded, an exception is thrown. On failure, an error message is exposed via
   * [setErrorMsg].
   */
  suspend fun deleteCurrentHunt() {
    val id = requireNotNull(huntId) { "No hunt loaded to delete." }
    try {
      repository.deleteHunt(id)
    } catch (e: Exception) {
      setErrorMsg("Failed to delete hunt: ${e.message}")
    }
  }
}
