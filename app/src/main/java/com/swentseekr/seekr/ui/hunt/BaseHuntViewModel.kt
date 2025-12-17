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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

abstract class BaseHuntViewModel(
    protected val repository: HuntsRepository = HuntRepositoryProvider.repository
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

  fun registerCheckpointImage(location: Location, uri: Uri?) {
    if (uri == null) return
    checkpointImages.add(location to uri)
  }

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

  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  protected fun setErrorMsg(error: String) {
    _uiState.value = _uiState.value.copy(errorMsg = error)
  }

  fun resetSaveSuccess() {
    _uiState.value = _uiState.value.copy(saveSuccessful = false)
  }

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

  // --- Field setters with validation ---
  fun setTitle(title: String) {
    _uiState.value =
        _uiState.value.copy(
            title = title,
            invalidTitleMsg = if (title.isBlank()) BaseHuntViewModelMessages.TITLE_EMPTY else null)
  }

  fun setDescription(desc: String) {
    _uiState.value =
        _uiState.value.copy(
            description = desc,
            invalidDescriptionMsg =
                if (desc.isBlank()) BaseHuntViewModelMessages.DESCRIPTION_EMPTY else null)
  }

  fun setTime(time: String) {
    userOverrodeTime = time.isNotBlank() && time != lastSuggestedTime

    _uiState.value =
        _uiState.value.copy(
            time = time,
            invalidTimeMsg =
                if (time.toDoubleOrNull() == null) BaseHuntViewModelMessages.INVALID_TIME else null)
  }

  fun setDistance(distance: String) {
    userOverrodeDistance = distance.isNotBlank() && distance != lastSuggestedDistance

    _uiState.value =
        _uiState.value.copy(
            distance = distance,
            invalidDistanceMsg =
                if (distance.toDoubleOrNull() == null) BaseHuntViewModelMessages.INVALID_DISTANCE
                else null)
  }

  fun setDifficulty(difficulty: Difficulty) {
    _uiState.value = _uiState.value.copy(difficulty = difficulty)
  }

  fun setStatus(status: HuntStatus) {
    _uiState.value = _uiState.value.copy(status = status)

    applyAutoTimeAndDistanceSuggestions(_uiState.value.points, statusChanged = true)
  }

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

  fun updateMainImageUri(uri: Uri?) {
    when (this) {
      is AddHuntViewModel -> this.mainImageUri = uri
      is EditHuntViewModel -> this.mainImageUri = uri
    }
    _uiState.value = _uiState.value.copy(mainImageUrl = uri?.toString() ?: "")
  }

  fun updateOtherImagesUris(uris: List<Uri>) {
    otherImagesUris = otherImagesUris + uris

    _uiState.value = _uiState.value.copy(otherImagesUris = otherImagesUris)
  }

  fun removeOtherImage(uri: Uri) {
    otherImagesUris = otherImagesUris - uri
    _uiState.value = _uiState.value.copy(otherImagesUris = otherImagesUris)
  }

  open fun removeMainImage() {
    updateMainImageUri(null)
  }

  open fun removeExistingOtherImage(url: String) {
    // defaut no-op
  }

  fun setIsSelectingPoints(isSelecting: Boolean) {
    _uiState.value = _uiState.value.copy(isSelectingPoints = isSelecting)
  }

  // For testing purposes
  fun setTestMode(enabled: Boolean) {
    testMode = enabled
  }

  abstract fun buildHunt(state: HuntUIState): Hunt

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
            withContext(Dispatchers.IO) {
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
