package com.swentseekr.seekr.ui.hunt

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.add.AddHuntViewModel
import com.swentseekr.seekr.ui.hunt.edit.EditHuntViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the current UI state for a Hunt creation or editing screen.
 *
 * @property title The hunt title entered by the user.
 * @property description The hunt description entered by the user.
 * @property points List of [Location] points defining the hunt's path.
 * @property time The estimated time to complete the hunt (in hours).
 * @property distance The estimated distance of the hunt (in km).
 * @property difficulty The hunt difficulty level.
 * @property status The hunt status (active, inactive, etc.).
 * @property mainImageUrl URL of the main image for the hunt.
 * @property otherImagesUrls URLs of additional images for the hunt.
 * @property otherImagesUris URIs of additional images that are not yet uploaded.
 * @property reviewRate Optional review rating for the hunt.
 * @property errorMsg Optional error message to display in the UI.
 * @property invalidTitleMsg Optional validation message for the title field.
 * @property invalidDescriptionMsg Optional validation message for the description field.
 * @property invalidTimeMsg Optional validation message for the time field.
 * @property invalidDistanceMsg Optional validation message for the distance field.
 * @property isSelectingPoints Whether the user is currently selecting points on the map.
 * @property saveSuccessful Whether the last save operation succeeded.
 */
data class HuntUIState(
    val title: String = "",
    val description: String = "",
    val points: List<Location> = emptyList(),
    val time: String = "",
    val distance: String = "",
    val difficulty: Difficulty? = null,
    val status: HuntStatus? = null,
    val mainImageUrl: String = "",
    val otherImagesUrls: List<String> = emptyList(),
    val otherImagesUris: List<Uri> = emptyList(),
    val reviewRate: Double = 0.0,
    val errorMsg: String? = null,
    val invalidTitleMsg: String? = null,
    val invalidDescriptionMsg: String? = null,
    val invalidTimeMsg: String? = null,
    val invalidDistanceMsg: String? = null,
    val isSelectingPoints: Boolean = false,
    val saveSuccessful: Boolean = false
) {
  val isValid: Boolean
    get() =
        title.isNotBlank() &&
            description.isNotBlank() &&
            time.toDoubleOrNull() != null &&
            distance.toDoubleOrNull() != null &&
            difficulty != null &&
            status != null &&
            invalidTitleMsg == null &&
            invalidDescriptionMsg == null &&
            invalidTimeMsg == null &&
            invalidDistanceMsg == null &&
            points.size >= 2
}

/**
 * Base ViewModel for Hunt creation and editing screens.
 *
 * Provides state management, field validation, image handling, and persistence logic for
 * Hunt-related screens.
 *
 * @param repository The [HuntsRepository] used to persist and retrieve Hunt data. Defaults to
 *   [HuntRepositoryProvider.repository].
 */
abstract class BaseHuntViewModel(
    protected val repository: HuntsRepository = HuntRepositoryProvider.repository
) : ViewModel() {

  protected val _uiState = MutableStateFlow(HuntUIState())
  open val uiState: StateFlow<HuntUIState> = _uiState.asStateFlow()

  private var testMode: Boolean = false

  protected var otherImagesUris: List<Uri> = emptyList()

  private val checkpointImages: MutableList<Pair<Location, Uri>> = mutableListOf()

  /**
   * Attaches registered checkpoint images to the given points and updates their image indexes.
   *
   * @param points List of [Location] points.
   * @return List of [Location] points with updated image indexes assigned.
   */
  fun registerCheckpointImage(location: Location, uri: Uri?) {
    if (uri == null) return
    checkpointImages.add(location to uri)
  }

  /**
   * Attaches registered checkpoint images to the given points and updates their image indexes.
   *
   * @param points List of [Location] points.
   * @return List of [Location] points with updated image indexes assigned.
   */
  fun attachCheckpointImages(points: List<Location>): List<Location> {
    if (checkpointImages.isEmpty()) return points
    val startIndex = otherImagesUris.size
    otherImagesUris = otherImagesUris + checkpointImages.map { it.second }

    val indexByLocation = mutableMapOf<Location, Int>()
    checkpointImages.forEachIndexed { offset, (loc, _) ->
      indexByLocation[loc] = startIndex + offset
    }

    checkpointImages.clear()
    _uiState.value = _uiState.value.copy(otherImagesUris = otherImagesUris)
    return points.map { p ->
      val index = indexByLocation[p]
      if (index != null) p.copy(imageIndex = index) else p
    }
  }

  /** Clears the current error message from the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /**
   * Sets a new error message in the UI state.
   *
   * @param error The error message to display.
   */
  protected fun setErrorMsg(error: String) {
    _uiState.value = _uiState.value.copy(errorMsg = error)
  }

  /** Resets the save success flag in the UI state. */
  fun resetSaveSuccess() {
    _uiState.value = _uiState.value.copy(saveSuccessful = false)
  }

  /**
   * Submits the current Hunt state.
   *
   * Validates fields, handles test mode, checks authentication, builds the Hunt model, and persists
   * it via [persist]. Updates [HuntUIState] with success or error messages.
   *
   * @return true if submission started (state valid), false if validation or authentication failed.
   */
  fun submit(): Boolean {
    val state = _uiState.value

    if (!state.isValid) {
      setErrorMsg(BaseHuntViewModelMessages.NOT_ALL_FIELD_FILL)
      return false
    }

    if (testMode) {
      _uiState.value = _uiState.value.copy(errorMsg = null, saveSuccessful = true)
      return true
    }

    if (FirebaseAuth.getInstance().currentUser?.uid == null) {
      setErrorMsg(BaseHuntViewModelMessages.MUST_LOGIN)
      return false
    }

    val hunt =
        try {
          buildHunt(state)
        } catch (e: Exception) {
          setErrorMsg(e.message ?: BaseHuntViewModelMessages.FAIL_BUILD)
          return false
        }

    viewModelScope.launch {
      try {
        persist(hunt)
        _uiState.value = _uiState.value.copy(errorMsg = null, saveSuccessful = true)
      } catch (e: Exception) {
        Log.e(
            this@BaseHuntViewModel::class.simpleName ?: BaseHuntViewModelMessages.BASE_VIEW_MODEL,
            BaseHuntViewModelMessages.ERROR_SAVING,
            e)
        _uiState.value =
            _uiState.value.copy(
                errorMsg = "${BaseHuntViewModelMessages.FAIL_SAVE} ${e.message}",
                saveSuccessful = false)
      }
    }
    return true
  }

  /**
   * Sets the hunt title and validates it.
   *
   * @param title The hunt title entered by the user.
   */
  fun setTitle(title: String) {
    _uiState.value =
        _uiState.value.copy(
            title = title,
            invalidTitleMsg = if (title.isBlank()) BaseHuntViewModelMessages.TITLE_EMPTY else null)
  }

  /**
   * Sets the hunt description and validates it.
   *
   * @param desc The hunt description entered by the user.
   */
  fun setDescription(desc: String) {
    _uiState.value =
        _uiState.value.copy(
            description = desc,
            invalidDescriptionMsg =
                if (desc.isBlank()) BaseHuntViewModelMessages.DESCRIPTION_EMPTY else null)
  }

  /**
   * Sets the estimated time and validates the format.
   *
   * @param time The estimated time in hours as a string.
   */
  fun setTime(time: String) {
    _uiState.value =
        _uiState.value.copy(
            time = time,
            invalidTimeMsg =
                if (time.toDoubleOrNull() == null) BaseHuntViewModelMessages.INVALID_TIME else null)
  }

  /**
   * Sets the estimated distance and validates the format.
   *
   * @param distance The estimated distance in km as a string.
   */
  fun setDistance(distance: String) {
    _uiState.value =
        _uiState.value.copy(
            distance = distance,
            invalidDistanceMsg =
                if (distance.toDoubleOrNull() == null) BaseHuntViewModelMessages.INVALID_DISTANCE
                else null)
  }

  /**
   * Sets the hunt difficulty.
   *
   * @param difficulty The [Difficulty] level of the hunt.
   */
  fun setDifficulty(difficulty: Difficulty) {
    _uiState.value = _uiState.value.copy(difficulty = difficulty)
  }

  /**
   * Sets the hunt status.
   *
   * @param status The [HuntStatus] of the hunt.
   */
  fun setStatus(status: HuntStatus) {
    _uiState.value = _uiState.value.copy(status = status)
  }

  /**
   * Sets the hunt points and validates minimum point count.
   *
   * @param points List of [Location] points.
   * @return true if points are valid, false if not enough points.
   */
  fun setPoints(points: List<Location>): Boolean {
    if (points.size < BaseHuntViewModelDefault.MIN_SET_POINT) {
      setErrorMsg(BaseHuntViewModelMessages.INVALID_SET_POINT)
      return false
    }
    _uiState.value = _uiState.value.copy(points = points)
    clearErrorMsg()
    return true
  }

  /**
   * Updates the main image URI and state.
   *
   * @param uri The URI of the main image, or null to remove it.
   */
  fun updateMainImageUri(uri: Uri?) {
    when (this) {
      is AddHuntViewModel -> this.mainImageUri = uri
      is EditHuntViewModel -> this.mainImageUri = uri
    }
    _uiState.value = _uiState.value.copy(mainImageUrl = uri?.toString() ?: "")
  }

  /**
   * Adds other images URIs to the state.
   *
   * @param uris List of image URIs to add.
   */
  fun updateOtherImagesUris(uris: List<Uri>) {
    otherImagesUris = otherImagesUris + uris

    _uiState.value = _uiState.value.copy(otherImagesUris = otherImagesUris)
  }

  /**
   * Removes an image URI from the other images state.
   *
   * @param uri The URI to remove.
   */
  fun removeOtherImage(uri: Uri) {
    otherImagesUris = otherImagesUris - uri
    _uiState.value = _uiState.value.copy(otherImagesUris = otherImagesUris)
  }

  /** Removes the main image. Can be overridden by subclasses. */
  open fun removeMainImage() {
    updateMainImageUri(null)
  }

  /** Removes an existing other image (default no-op). Override in subclasses. */
  open fun removeExistingOtherImage(url: String) {}

  /**
   * Updates the selecting points mode.
   *
   * @param isSelecting true if the user is selecting points on the map, false otherwise.
   */
  fun setIsSelectingPoints(isSelecting: Boolean) {
    _uiState.value = _uiState.value.copy(isSelectingPoints = isSelecting)
  }

  /**
   * Enables or disables test mode. In test mode, submission is automatically successful.
   *
   * @param enabled true to enable test mode, false to disable.
   */
  fun setTestMode(enabled: Boolean) {
    testMode = enabled
  }

  /**
   * Builds a [Hunt] model from the current UI state.
   *
   * @param state The current [HuntUIState].
   * @return A [Hunt] object to be persisted.
   */
  abstract fun buildHunt(state: HuntUIState): Hunt

  /**
   * Persists a [Hunt] object in the repository.
   *
   * @param hunt The [Hunt] to persist.
   */
  protected abstract suspend fun persist(hunt: Hunt)
}
