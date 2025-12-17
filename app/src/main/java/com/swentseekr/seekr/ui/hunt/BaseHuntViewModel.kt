package com.swentseekr.seekr.ui.hunt

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.BuildConfig
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.add.AddHuntViewModel
import com.swentseekr.seekr.ui.hunt.edit.EditHuntViewModel
import com.swentseekr.seekr.ui.map.MapConfig
import com.swentseekr.seekr.ui.map.computeDistanceMetersRaw
import com.swentseekr.seekr.ui.map.requestDirectionsPolyline
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    protected val repository: HuntsRepository = HuntRepositoryProvider.repository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  protected val _uiState = MutableStateFlow(HuntUIState())
  open val uiState: StateFlow<HuntUIState> = _uiState.asStateFlow()

  private var testMode: Boolean = false

  private var lastSuggestedTime: String? = null
  private var lastSuggestedDistance: String? = null
  private var userOverrodeTime: Boolean = false
  private var userOverrodeDistance: Boolean = false

  private var cachedPointsKey: String? = null
  private var cachedRouteDistanceKm: Double? = null

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
    userOverrodeTime = time.isNotBlank() && time != lastSuggestedTime

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
    userOverrodeDistance = distance.isNotBlank() && distance != lastSuggestedDistance

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

    applyAutoTimeAndDistanceSuggestions(_uiState.value.points, statusChanged = true)
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

    cachedPointsKey = null
    cachedRouteDistanceKm = null

    applyAutoTimeAndDistanceSuggestions(points, statusChanged = false)

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

  /** Removes an existing other image. Override in subclasses. */
  open fun removeExistingOtherImage(url: String) {
    // default, no operations needed
  }

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

  /**
   * Computes and applies suggested distance and time values based on the current hunt points.
   *
   * Distance is estimated using the Google Directions API in WALKING mode, summing the resulting
   * polyline segments to obtain an actual walking distance through streets.
   *
   * Time is derived from the computed distance and an assumed walking speed that depends on the
   * hunt status (FUN, DISCOVER, SPORT). If no status is set, a default speed is used.
   *
   * This method respects user input:
   * - Suggested values are only applied if the corresponding field has not been manually overridden
   *   by the user, or if the field is currently blank.
   * - Manually edited values are never overwritten automatically.
   *
   * To improve responsiveness, the walking distance is cached for the current set of points. When
   * this method is triggered by a status change, the cached distance is reused and only the time is
   * recomputed.
   *
   * Asynchronous Direction API results are ignored if the hunt points change while the request is
   * in flight, preventing stale suggestions from being applied.
   *
   * @param points The ordered list of hunt checkpoints (start → waypoints → end).
   * @param statusChanged Whether this invocation was triggered by a status change. When true, the
   *   cached distance (if available) is reused instead of issuing a new Directions request.
   */
  private fun applyAutoTimeAndDistanceSuggestions(points: List<Location>, statusChanged: Boolean) {
    if (points.size < BaseHuntConstantsDefault.POLYLINE) return

    val state = _uiState.value
    val pointsKey = pointsKey(points)

    val cachedKm =
        if (statusChanged && cachedPointsKey == pointsKey) cachedRouteDistanceKm else null

    if (cachedKm != null) {
      applySuggestionsFromDistanceKm(cachedKm, state.status)
      return
    }

    val origin = points.first()
    val dest = points.last()
    val waypoints = points.subList(1, points.size - 1).map { it.latitude to it.longitude }
    val snapshot = points.toList()

    viewModelScope.launch {
      val polyline: List<LatLng> =
          try {
            withContext(ioDispatcher) {
              requestDirectionsPolyline(
                  originLat = origin.latitude,
                  originLng = origin.longitude,
                  destLat = dest.latitude,
                  destLng = dest.longitude,
                  waypoints = waypoints,
                  travelMode = MapConfig.TRAVEL_MODE_WALKING,
                  apiKey = BuildConfig.MAPS_API_KEY)
            }
          } catch (_: Exception) {
            emptyList()
          }

      if (_uiState.value.points != snapshot) return@launch
      if (polyline.size < BaseHuntConstantsDefault.POLYLINE) return@launch

      val distanceKm = polylineDistanceKm(polyline)
      if (distanceKm <= BaseHuntConstantsDefault.EMPTY_DISTANCE) return@launch

      cachedPointsKey = pointsKey
      cachedRouteDistanceKm = distanceKm

      applySuggestionsFromDistanceKm(distanceKm, _uiState.value.status)
    }
  }

  private fun applySuggestionsFromDistanceKm(distanceKm: Double, status: HuntStatus?) {
    val state = _uiState.value

    val suggestedDistance = formatDecimal(distanceKm)
    val speed = suggestedSpeedKmh(status)
    val suggestedTimeHours = distanceKm / speed
    val suggestedTime = formatDecimal(suggestedTimeHours)

    val shouldUpdateDistance =
        !userOverrodeDistance || state.distance.isBlank() || state.distance == lastSuggestedDistance
    val shouldUpdateTime =
        !userOverrodeTime || state.time.isBlank() || state.time == lastSuggestedTime

    if (shouldUpdateDistance) {
      lastSuggestedDistance = suggestedDistance
      _uiState.value =
          _uiState.value.copy(
              distance = suggestedDistance,
              invalidDistanceMsg =
                  if (suggestedDistance.toDoubleOrNull() == null)
                      BaseHuntViewModelMessages.INVALID_DISTANCE
                  else null)
    }

    if (shouldUpdateTime) {
      lastSuggestedTime = suggestedTime
      _uiState.value =
          _uiState.value.copy(
              time = suggestedTime,
              invalidTimeMsg =
                  if (suggestedTime.toDoubleOrNull() == null) BaseHuntViewModelMessages.INVALID_TIME
                  else null)
    }

    if (state.distance.isBlank()) userOverrodeDistance = false
    if (state.time.isBlank()) userOverrodeTime = false
  }

  private fun suggestedSpeedKmh(status: HuntStatus?): Double {
    return when (status) {
      HuntStatus.FUN -> BaseHuntConstantsDefault.FUN_SPEED
      HuntStatus.DISCOVER -> BaseHuntConstantsDefault.DISCOVER_SPEED
      HuntStatus.SPORT -> BaseHuntConstantsDefault.SPORT_SPEED
      null -> BaseHuntConstantsDefault.DEFAULT_SPEED
    }
  }

  private fun polylineDistanceKm(path: List<LatLng>): Double {
    var meters = 0.0
    for (i in 0 until path.lastIndex) {
      meters += computeDistanceMetersRaw(path[i], path[i + 1])
    }
    return meters / BaseHuntConstantsDefault.KILOMETER
  }

  private fun pointsKey(points: List<Location>): String =
      points.joinToString("|") { "${it.latitude},${it.longitude}" }

  private fun formatDecimal(value: Double): String {
    val s = String.format(Locale.US, "%.2f", value)
    return s.trimEnd('0').trimEnd('.')
  }
}
