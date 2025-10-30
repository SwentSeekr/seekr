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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Immutable UI model for the Map screen.
 *
 * @property target Map camera target used for the initial/overview position. Defaults to Lausanne.
 * @property hunts All hunts currently available for display on the map.
 * @property selectedHunt The hunt selected in overview mode (null when none is selected).
 * @property isFocused Whether the UI shows only the selected hunt’s route (start/middle/end).
 * @property errorMsg Optional human-readable error for transient failures (e.g., loading).
 */
data class MapUIState(
    val target: LatLng = LatLng(46.519962, 6.633597),
    val hunts: List<Hunt> = emptyList(),
    val selectedHunt: Hunt? = null,
    val isFocused: Boolean = false,
    val errorMsg: String? = null,
    val route: List<LatLng> = emptyList(),
    val isRouteLoading: Boolean = false
)

/**
 * ViewModel orchestrating map data and UI state transitions.
 *
 * Responsibilities:
 * - Fetch hunts from a [HuntsRepository] and expose them via [uiState].
 * - Observe authentication changes to refresh data upon login.
 * - Handle map interactions: marker selection, “View Hunt”, and “Back to all hunts”.
 *
 * Construction:
 * - By default uses [HuntRepositoryProvider.repository], but any [HuntsRepository] can be injected
 *   in tests.
 *
 * Threading:
 * - All data loading is launched in [viewModelScope]; UI state updates are emitted to a
 *   [MutableStateFlow] and exposed as [StateFlow].
 *
 * @param repository Source of truth for hunts; defaults to the shared app repository provider.
 */
class MapViewModel(private val repository: HuntsRepository = HuntRepositoryProvider.repository) :
    ViewModel() {

  companion object {
    private fun toLatLng(location: Location): LatLng {
      return LatLng(location.latitude, location.longitude)
    }
  }

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

  /** Sets a transient error message in the UI state. */
  private fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  /** Clears any existing error message from the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Forces a data reload (e.g., pull-to-refresh from UI). */
  fun refreshUIState() {
    fetchHunts()
  }

  /**
   * Handles a marker selection in overview mode.
   *
   * Sets [MapUIState.selectedHunt] to the tapped hunt; the map composable reacts by showing a
   * preview popup and animating the camera to the hunt’s start.
   */
  fun onMarkerClick(hunt: Hunt) {
    _uiState.value = _uiState.value.copy(selectedHunt = hunt)
  }

  /**
   * Enters focused mode for the current [MapUIState.selectedHunt].
   *
   * In focused mode, only start/middle/end markers for the selected hunt are rendered and the
   * camera fits all checkpoints.
   */
  fun onViewHuntClick() {
    _uiState.value = _uiState.value.copy(isFocused = true, route = emptyList())
    viewModelScope.launch { computeRouteForSelectedHunt(travelMode = "walking") }
  }

  /**
   * Leaves focused/preview state and clears any current selection.
   *
   * After this call the map returns to the overview rendering (one marker per hunt). Camera
   * movement is controlled by the composable (e.g., no forced reset).
   */
  fun onBackToAllHunts() {
    _uiState.value =
        _uiState.value.copy(isFocused = false, selectedHunt = null, route = emptyList())
  }

  /**
   * Loads hunts from the repository and updates [uiState].
   *
   * Success:
   * - Updates [MapUIState.hunts].
   * - Sets [MapUIState.target] to the start of the first hunt, or Lausanne when no hunts exist.
   *
   * Failure:
   * - Populates [MapUIState.errorMsg] with a user-facing message.
   */
  private fun fetchHunts() {
    viewModelScope.launch {
      try {
        val hunts = repository.getAllHunts()
        val target = hunts.firstOrNull()?.start ?: Location(46.519962, 6.633597, "Lausanne")
        _uiState.value = MapUIState(target = toLatLng(target), hunts = hunts)
      } catch (e: Exception) {
        setErrorMsg("Failed to load hunts: ${e.message}")
      }
    }
  }

  private suspend fun computeRouteForSelectedHunt(travelMode: String = "walking") {
    val hunt = _uiState.value.selectedHunt ?: return
    _uiState.value = _uiState.value.copy(isRouteLoading = true)

    try {
      val points =
          withContext(Dispatchers.IO) {
            requestDirectionsPolyline(
                originLat = hunt.start.latitude,
                originLng = hunt.start.longitude,
                destLat = hunt.end.latitude,
                destLng = hunt.end.longitude,
                waypoints = hunt.middlePoints.map { it.latitude to it.longitude },
                travelMode = travelMode)
          }
      _uiState.value = _uiState.value.copy(route = points, isRouteLoading = false)
    } catch (e: Exception) {
      setErrorMsg("Failed to get route: ${e.message}")
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

    val origin = "${originLat},${originLng}"
    val destination = "${destLat},${destLng}"

    val waypointParam =
        if (waypoints.isNotEmpty()) {
          waypoints.joinToString(separator = "|") { (lat, lng) -> "via:$lat,$lng" }
        } else null

    val base = "https://maps.googleapis.com/maps/api/directions/json"
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

    val url = URL("$base?$params")
    val conn =
        (url.openConnection() as HttpURLConnection).apply {
          requestMethod = "GET"
          connectTimeout = 15000
          readTimeout = 15000
          doInput = true
        }

    conn.inputStream.use { stream ->
      val body = stream.bufferedReader().readText()
      val json = JSONObject(body)

      val status = json.optString("status")
      if (status != "OK") {
        val message = json.optString("error_message", status)
        throw IllegalStateException("Directions API error: $message")
      }

      val routes = json.getJSONArray("routes")
      if (routes.length() == 0) return emptyList()

      val firstRoute = routes.getJSONObject(0)
      val legs = firstRoute.getJSONArray("legs")

      val fullPath = mutableListOf<LatLng>()

      for (i in 0 until legs.length()) {
        val leg = legs.getJSONObject(i)
        val steps = leg.getJSONArray("steps")
        for (j in 0 until steps.length()) {
          val step = steps.getJSONObject(j)
          val poly = step.getJSONObject("polyline").getString("points")
          val stepPoints = PolyUtil.decode(poly)

          if (fullPath.isNotEmpty()) {
            fullPath.addAll(stepPoints.drop(1))
          } else {
            fullPath.addAll(stepPoints)
          }
        }
      }

      if (fullPath.isEmpty()) {
        val overview = firstRoute.getJSONObject("overview_polyline").getString("points")
        return PolyUtil.decode(overview)
      }

      return fullPath
    }
  }
}
