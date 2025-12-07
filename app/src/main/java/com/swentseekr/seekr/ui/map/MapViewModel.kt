package com.swentseekr.seekr.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.swentseekr.seekr.BuildConfig
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.map.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI state for the entire map screen.
 *
 * @property target initial camera target position on the map.
 * @property hunts list of hunts available to display.
 * @property selectedHunt the currently selected hunt (from marker or popup).
 * @property isFocused whether the UI is currently showing a focused hunt view.
 * @property errorMsg a user-facing error message to display, or null if none.
 * @property route polyline points representing the current route (overview or next checkpoint).
 * @property isRouteLoading whether route generation is in progress.
 * @property isHuntStarted whether the user has begun performing the selected hunt.
 * @property validatedCount number of checkpoints validated so far.
 * @property validationRadiusMeters threshold for validating checkpoint proximity.
 * @property currentDistanceToNextMeters distance from user to next checkpoint in meters.
 */
data class MapUIState(
    val target: LatLng = LatLng(MapConfig.DefaultLat, MapConfig.DefaultLng),
    val hunts: List<Hunt> = emptyList(),
    val selectedHunt: Hunt? = null,
    val isFocused: Boolean = false,
    val errorMsg: String? = null,
    val route: List<LatLng> = emptyList(),
    val isRouteLoading: Boolean = false,
    val isHuntStarted: Boolean = false,
    val validatedCount: Int = MapConfig.DefaultValidatedCount,
    val validationRadiusMeters: Int = MapConfig.ValidationRadiusMeters,
    val currentDistanceToNextMeters: Int? = null
)

/**
 * ViewModel for the map screen.
 *
 * Responsible for:
 * - Loading hunts from the repository
 * - Managing hunt selection & focus mode
 * - Computing routes using the Directions API
 * - Validating user progress through checkpoints
 * - Tracking distance to next checkpoint
 *
 * The ViewModel exposes state via [uiState] and performs all asynchronous data operations using
 * [viewModelScope].
 *
 * @param repository repository providing hunts data (defaults to app provider).
 * @param ioDispatcher dispatcher used for network/disk IO.
 */
class MapViewModel(
    private val repository: HuntsRepository = HuntRepositoryProvider.repository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  private val _uiState = MutableStateFlow(MapUIState())
  /** Immutable UI state exposed to UI. */
  val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()

  init {
    fetchHunts()

    // Reload hunts when authentication state changes (e.g., user logs in).
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        fetchHunts()
      }
    }
  }

  /** Updates the UI error message. */
  private fun setErrorMsg(errorMsg: String?) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  /** Clears any displayed error message. */
  fun clearErrorMsg() {
    setErrorMsg(null)
  }

  /** Refreshes all hunt content by re-fetching from repository. */
  fun refreshUIState() {
    fetchHunts()
  }

  /**
   * Selects a hunt when a marker is clicked.
   *
   * @param hunt the hunt the user clicked.
   */
  fun onMarkerClick(hunt: Hunt) {
    _uiState.value = _uiState.value.copy(selectedHunt = hunt)
  }

  /** Enters focused mode for the selected hunt and triggers route computation. */
  fun onViewHuntClick() {
    _uiState.value = _uiState.value.copy(isFocused = true, route = emptyList())
    viewModelScope.launch { computeRouteForSelectedHunt(travelMode = MapConfig.TravelModeWalking) }
  }

  /**
   * Selects a hunt by its id, like clicking its marker on the map.
   *
   * @param huntId the id of the hunt to select.
   */
  fun selectHuntById(huntId: String) {
    val state = _uiState.value
    val hunt = state.hunts.find { it.uid == huntId } ?: return

    _uiState.value =
        state.copy(
            selectedHunt = hunt,
            isFocused = false,
            route = emptyList(),
            isHuntStarted = false,
            validatedCount = MapConfig.DefaultValidatedCount,
            currentDistanceToNextMeters = null,
            errorMsg = null,
        )
  }

  /** Exits focused mode and resets hunt-related state. */
  fun onBackToAllHunts() {
    _uiState.value =
        _uiState.value.copy(
            isFocused = false,
            selectedHunt = null,
            route = emptyList(),
            isHuntStarted = false,
            validatedCount = MapConfig.DefaultValidatedCount,
            currentDistanceToNextMeters = null)
  }

  /**
   * Retrieves all hunts from the repository and updates map state.
   *
   * If no hunts are available, the default target location is used.
   */
  private fun fetchHunts() {
    viewModelScope.launch {
      try {
        val hunts = repository.getAllHunts()

        val targetLocation =
            hunts.firstOrNull()?.start
                ?: Location(MapConfig.DefaultLat, MapConfig.DefaultLng, MapConfig.DefaultCityName)

        _uiState.value = MapUIState(target = targetLocation.toLatLng(), hunts = hunts)
      } catch (e: Exception) {
        setErrorMsg(MapScreenStrings.ErrorLoadHuntsPrefix + (e.message ?: ""))
      }
    }
  }

  /**
   * Computes the route for the currently selected hunt (full hunt path).
   *
   * @param travelMode mode used by Directions API (e.g., "walking").
   */
  private suspend fun computeRouteForSelectedHunt(
      travelMode: String = MapConfig.TravelModeWalking
  ) {
    val hunt = _uiState.value.selectedHunt ?: return
    _uiState.value = _uiState.value.copy(isRouteLoading = true)

    try {
      val points =
          withContext(ioDispatcher) {
            requestDirectionsPolyline(
                originLat = hunt.start.latitude,
                originLng = hunt.start.longitude,
                destLat = hunt.end.latitude,
                destLng = hunt.end.longitude,
                waypoints = hunt.middlePoints.map { it.latitude to it.longitude },
                travelMode = travelMode,
                apiKey = BuildConfig.MAPS_API_KEY)
          }

      clearErrorMsg()
      _uiState.value = _uiState.value.copy(route = points, isRouteLoading = false)
    } catch (e: Exception) {
      setErrorMsg(MapScreenStrings.ErrorRoutePrefix + (e.message ?: ""))
      _uiState.value = _uiState.value.copy(isRouteLoading = false, route = emptyList())
    }
  }

  /**
   * Updates the distance between the user and the next checkpoint.
   *
   * Called continuously when location updates are received.
   *
   * @param currentLocation the user's current coordinates.
   */
  fun updateCurrentDistanceToNext(currentLocation: LatLng) {
    val state = _uiState.value
    val hunt = state.selectedHunt ?: return

    val distance =
        computeDistanceToNextPoint(
            hunt = hunt, validatedCount = state.validatedCount, currentLocation = currentLocation)

    _uiState.value = state.copy(currentDistanceToNextMeters = distance)
  }

  /** Starts the selected hunt and computes the route from start to end. */
  fun startHunt() {
    _uiState.value.selectedHunt ?: return

    _uiState.value =
        _uiState.value.copy(
            isFocused = true,
            route = emptyList(),
            isHuntStarted = true,
            validatedCount = MapConfig.DefaultValidatedCount,
            currentDistanceToNextMeters = null)

    viewModelScope.launch { computeRouteForSelectedHunt(travelMode = MapConfig.TravelModeWalking) }
  }

  /**
   * Validates whether the user is close enough to the next checkpoint.
   *
   * Updates:
   * - validatedCount (if success)
   * - currentDistanceToNextMeters
   * - error message if too far
   */
  fun validateCurrentPoint(currentLocation: LatLng) {
    val state = _uiState.value
    val hunt = state.selectedHunt ?: return
    if (!state.isHuntStarted) return

    val nextPoint = nextPointFor(hunt, state.validatedCount) ?: return

    val distanceMetersDouble =
        try {
          computeDistanceMetersRaw(currentLocation, nextPoint)
        } catch (_: Exception) {
          return
        }

    val distanceInt = distanceMetersDouble.toInt()
    val within = distanceMetersDouble <= state.validationRadiusMeters

    if (within) {
      val newValidated = state.validatedCount + 1
      val newDistance =
          computeDistanceToNextPoint(
              hunt = hunt, validatedCount = newValidated, currentLocation = currentLocation)

      _uiState.value =
          state.copy(
              validatedCount = newValidated,
              currentDistanceToNextMeters = newDistance,
              errorMsg = null)
    } else {
      _uiState.value =
          state.copy(
              currentDistanceToNextMeters = distanceInt,
              errorMsg =
                  MapScreenStrings.ErrorTooFarPrefix +
                      MapConfig.ValidationRadiusMeters +
                      MapScreenStrings.DistanceMetersSuffix)
    }
  }

  /**
   * Computes and updates a route from the user's current location to the next checkpoint.
   *
   * Used during active hunts when navigation between points is needed.
   *
   * @param currentLocation user's current map position.
   */
  fun routeFromCurrentToNext(currentLocation: LatLng) {
    val state = _uiState.value
    val hunt = state.selectedHunt ?: return
    val nextPoint = nextPointFor(hunt, state.validatedCount) ?: return

    _uiState.value = state.copy(isRouteLoading = true)

    viewModelScope.launch {
      try {
        val points =
            withContext(ioDispatcher) {
              requestDirectionsPolyline(
                  originLat = currentLocation.latitude,
                  originLng = currentLocation.longitude,
                  destLat = nextPoint.latitude,
                  destLng = nextPoint.longitude,
                  waypoints = emptyList(),
                  travelMode = MapConfig.TravelModeWalking,
                  apiKey = BuildConfig.MAPS_API_KEY)
            }

        _uiState.value =
            _uiState.value.copy(route = points, isRouteLoading = false, errorMsg = null)
      } catch (e: Exception) {
        setErrorMsg(MapScreenStrings.ErrorRoutePrefix + (e.message ?: ""))
        _uiState.value = _uiState.value.copy(isRouteLoading = false, route = emptyList())
      }
    }
  }

  /**
   * Completes a hunt only if all checkpoints have been validated.
   *
   * @param onPersist optional persistence operation for saving completion to a database.
   */
  fun finishHunt(onPersist: suspend (Hunt) -> Unit = {}) {
    val state = _uiState.value
    val hunt = state.selectedHunt ?: return

    if (!isHuntFullyValidated(hunt, state.validatedCount)) {
      setErrorMsg(MapScreenStrings.ErrorIncompleteHunt)
      return
    }

    viewModelScope.launch {
      try {
        onPersist(hunt)
        _uiState.value =
            state.copy(
                isHuntStarted = false,
                validatedCount = MapConfig.DefaultValidatedCount,
                isFocused = false,
                selectedHunt = null,
                route = emptyList(),
                currentDistanceToNextMeters = null)
      } catch (e: Exception) {
        setErrorMsg(MapScreenStrings.ErrorFinishHuntPrefix + (e.message ?: ""))
      }
    }
  }
}
