package com.swentseekr.seekr.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.map.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class representing the UI state for the Map screen.
 *
 * @param target The geographical coordinates to center the map on. Defaults to Lausanne's location.
 * @param hunts A list of hunts to be displayed on the map.
 * @param errorMsg An optional error message to be displayed to the user.
 */
data class MapUIState(
    val target: LatLng = LatLng(46.519962, 6.633597),
    val hunts: List<Hunt> = emptyList(),
    val errorMsg: String? = null
)

/**
 * ViewModel for the Map screen.
 *
 * This ViewModel is responsible for fetching hunt data and preparing it for display on the map. It
 * observes authentication state changes to fetch hunts when a user logs in.
 *
 * @param repository The repository used to fetch hunt data. Defaults to the standard
 *   HuntsRepository.
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

  /**
   * Fetches all hunts from the repository and updates the UI state.
   *
   * On success, it updates the list of hunts and sets the map target to the start of the first
   * hunt. On failure, it sets an appropriate error message.
   */
  private fun fetchHunts() {
    viewModelScope.launch {
      try {
        val hunts = repository.getAllHunts()
        val target = hunts.firstOrNull()!!.start
        _uiState.value = MapUIState(target = toLatLng(target), hunts = hunts)
      } catch (e: Exception) {
        setErrorMsg("Failed to load hunts: ${e.message}")
      }
    }
  }
}
