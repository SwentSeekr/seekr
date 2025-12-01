package com.swentseekr.seekr.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.maps.android.PolyUtil
import com.swentseekr.seekr.BuildConfig
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.map.Location
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

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
    val validationRadiusMeters: Int = MapConfig.ValidationRadiusMeters
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

  private fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
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
            validatedCount = MapConfig.DefaultValidatedCount)
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
                travelMode = travelMode)
          }
      setErrorMsg("")
      _uiState.value = _uiState.value.copy(route = points, isRouteLoading = false)
    } catch (e: Exception) {
      setErrorMsg(MapScreenStrings.ErrorRoutePrefix + (e.message ?: ""))
      _uiState.value = _uiState.value.copy(isRouteLoading = false, route = emptyList())
    }
  }

  private fun requestDirectionsPolyline(
      originLat: Double,
      originLng: Double,
      destLat: Double,
      destLng: Double,
      waypoints: List<Pair<Double, Double>>,
      travelMode: String
  ): List<LatLng> {
    val url = buildDirectionsUrl(originLat, originLng, destLat, destLng, waypoints, travelMode)
    val conn = openDirectionsConnection(url)

    return conn.inputStream.use { stream ->
      val body = stream.bufferedReader().readText()
      val json = JSONObject(body)
      parseDirectionsResponse(json)
    }
  }

  private fun buildDirectionsUrl(
      originLat: Double,
      originLng: Double,
      destLat: Double,
      destLng: Double,
      waypoints: List<Pair<Double, Double>>,
      travelMode: String
  ): URL {
    val origin = "$originLat,$originLng"
    val destination = "$destLat,$destLng"

    val waypointParam =
        if (waypoints.isNotEmpty()) {
          waypoints.joinToString(separator = "|") { (lat, lng) -> "via:$lat,$lng" }
        } else {
          null
        }

    val base = MapConfig.DirectionsBaseUrl
    val params =
        buildList {
              add("origin=" + URLEncoder.encode(origin, StandardCharsets.UTF_8.name()))
              add("destination=" + URLEncoder.encode(destination, StandardCharsets.UTF_8.name()))
              add("mode=" + URLEncoder.encode(travelMode, StandardCharsets.UTF_8.name()))
              waypointParam?.let {
                add("waypoints=" + URLEncoder.encode(it, StandardCharsets.UTF_8.name()))
              }
              add(
                  "key=" +
                      URLEncoder.encode(BuildConfig.MAPS_API_KEY, StandardCharsets.UTF_8.name()))
            }
            .joinToString("&")

    return URL("$base?$params")
  }

  private fun openDirectionsConnection(url: URL): HttpURLConnection =
      (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = MapConfig.DirectionsConnectTimeoutMs
        readTimeout = MapConfig.DirectionsReadTimeoutMs
        doInput = true
      }

  private fun parseDirectionsResponse(json: JSONObject): List<LatLng> {
    ensureStatusOk(json)

    val routes = json.getJSONArray("routes")
    if (routes.length() == 0) return emptyList()

    val firstRoute = routes.getJSONObject(0)
    val legs = firstRoute.getJSONArray("legs")

    val fullPath = buildFullPath(legs)
    if (fullPath.isNotEmpty()) return fullPath

    val overview = firstRoute.getJSONObject("overview_polyline").getString("points")
    return PolyUtil.decode(overview)
  }

  private fun ensureStatusOk(json: JSONObject) {
    val status = json.optString("status")
    if (status != "OK") {
      val message = json.optString("error_message", status)
      throw IllegalStateException("Directions API error: $message")
    }
  }

  private fun buildFullPath(legs: JSONArray): List<LatLng> {
    val fullPath = mutableListOf<LatLng>()

    for (i in 0 until legs.length()) {
      val leg = legs.getJSONObject(i)
      val steps = leg.getJSONArray("steps")
      appendStepsToPath(steps, fullPath)
    }

    return fullPath
  }

  private fun appendStepsToPath(steps: JSONArray, fullPath: MutableList<LatLng>) {
    for (j in 0 until steps.length()) {
      val step = steps.getJSONObject(j)
      val poly = step.getJSONObject("polyline").getString("points")
      val stepPoints = PolyUtil.decode(poly).toList() // immutable as per earlier Sonar hint

      if (fullPath.isNotEmpty()) {
        fullPath.addAll(stepPoints.drop(1))
      } else {
        fullPath.addAll(stepPoints)
      }
    }
  }

  fun startHunt() {
    _uiState.value.selectedHunt ?: return
    _uiState.value =
        _uiState.value.copy(
            isFocused = true,
            route = emptyList(),
            isHuntStarted = true,
            validatedCount = MapConfig.DefaultValidatedCount)
    viewModelScope.launch { computeRouteForSelectedHunt(travelMode = MapConfig.TravelModeWalking) }
  }

  fun validateCurrentPoint(currentLocation: LatLng) {
    val state = _uiState.value
    val hunt = state.selectedHunt ?: return
    if (!state.isHuntStarted) return

    val ordered = buildList {
      add(LatLng(hunt.start.latitude, hunt.start.longitude))
      hunt.middlePoints.forEach { add(LatLng(it.latitude, it.longitude)) }
      add(LatLng(hunt.end.latitude, hunt.end.longitude))
    }

    val nextIdx = state.validatedCount
    if (nextIdx >= ordered.size) return

    val nextPoint = ordered[nextIdx]
    val within =
        try {
          com.google.maps.android.SphericalUtil.computeDistanceBetween(
              currentLocation, nextPoint) <= state.validationRadiusMeters
        } catch (e: Exception) {
          false
        }

    if (within) _uiState.value = state.copy(validatedCount = state.validatedCount + 1)
  }

  fun finishHunt(onPersist: suspend (Hunt) -> Unit = {}) {
    val state = _uiState.value
    val hunt = state.selectedHunt ?: return
    val total = 2 + hunt.middlePoints.size
    if (state.validatedCount < total) {
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
                route = emptyList())
      } catch (e: Exception) {
        setErrorMsg(MapScreenStrings.ErrorFinishHuntPrefix + (e.message ?: ""))
      }
    }
  }
}
