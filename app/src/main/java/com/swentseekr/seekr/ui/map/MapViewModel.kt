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

class MapViewModel(
    private val repository: HuntsRepository = HuntRepositoryProvider.repository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  private val _uiState = MutableStateFlow(MapUIState())
  val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()

  init {
    fetchHunts()
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        fetchHunts()
      }
    }
  }

  private fun setErrorMsg(errorMsg: String?) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  fun clearErrorMsg() {
    setErrorMsg(null)
  }

  fun refreshUIState() {
    fetchHunts()
  }

  fun onMarkerClick(hunt: Hunt) {
    _uiState.value = _uiState.value.copy(selectedHunt = hunt)
  }

  fun onViewHuntClick() {
    _uiState.value = _uiState.value.copy(isFocused = true, route = emptyList())
    viewModelScope.launch { computeRouteForSelectedHunt(travelMode = MapConfig.TravelModeWalking) }
  }

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

  fun updateCurrentDistanceToNext(currentLocation: LatLng) {
    val state = _uiState.value
    val hunt = state.selectedHunt ?: return

    val distance =
        computeDistanceToNextPoint(
            hunt = hunt, validatedCount = state.validatedCount, currentLocation = currentLocation)

    _uiState.value = state.copy(currentDistanceToNextMeters = distance)
  }

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

  fun validateCurrentPoint(currentLocation: LatLng) {
    val state = _uiState.value
    val hunt = state.selectedHunt ?: return
    if (!state.isHuntStarted) return

    val nextPoint = nextPointFor(hunt, state.validatedCount) ?: return

    val distanceMetersDouble: Double =
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
