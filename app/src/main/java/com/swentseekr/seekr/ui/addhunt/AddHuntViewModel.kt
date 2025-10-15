package com.swentseekr.seekr.ui.addhunt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.map.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the AddHunt screen */
data class AddHuntUIState(
    val title: String = "",
    val description: String = "",
    val startLocation: Location? = null,
    val endLocation: Location? = null,
    val middlePoints: List<Location> = emptyList(),
    val time: String = "",
    val distance: String = "",
    val difficulty: Difficulty? = null,
    val status: HuntStatus? = null,
    val image: Int = 0,
    val reviewRate: Double = 0.0,
    val author: Author? = null,
    val errorMsg: String? = null,
    val invalidTitleMsg: String? = null,
    val invalidDescriptionMsg: String? = null,
    val invalidTimeMsg: String? = null,
    val invalidDistanceMsg: String? = null,
) {
  val isValid: Boolean
    get() =
        title.isNotBlank() &&
            description.isNotBlank() &&
            startLocation != null &&
            endLocation != null &&
            time.toDoubleOrNull() != null &&
            distance.toDoubleOrNull() != null &&
            difficulty != null &&
            status != null &&
            invalidTitleMsg == null &&
            invalidDescriptionMsg == null &&
            invalidTimeMsg == null &&
            invalidDistanceMsg == null
}

/** ViewModel for the AddHunt screen */
class AddHuntViewModel(
    private val repository: HuntsRepository = HuntRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(AddHuntUIState())
  val uiState: StateFlow<AddHuntUIState> = _uiState.asStateFlow()

  private val EMPTY_BIO = "Not bio yet"
  private val EMPTY_PSEUDONYM = "Anonymous"
  private val EMPTY_PROFILE_PIC = R.drawable.empty_user
  private val EMPTY_REVIEW_RATE = 0.0
  private val EMPTY_SPORT_RATE = 0.0

  /** Clears error message */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Sets error message */
  private fun setErrorMsg(error: String) {
    _uiState.value = _uiState.value.copy(errorMsg = error)
  }

  /** Adds a new Hunt */
  fun addHunt(): Boolean {
    val state = _uiState.value
    if (!state.isValid) {
      setErrorMsg("Please fill all required fields before creating a hunt.")
      return false
    }

    val hunt =
        Hunt(
            uid = repository.getNewUid(),
            start = state.startLocation!!,
            end = state.endLocation!!,
            middlePoints = state.middlePoints,
            status = state.status!!,
            title = state.title,
            description = state.description,
            time = state.time.toDouble(),
            distance = state.distance.toDouble(),
            difficulty = state.difficulty!!,
            author =
                state.author
                    ?: Author(
                        EMPTY_PSEUDONYM,
                        EMPTY_BIO,
                        EMPTY_PROFILE_PIC,
                        EMPTY_REVIEW_RATE,
                        EMPTY_SPORT_RATE),
            image = state.image,
            reviewRate = state.reviewRate)

    viewModelScope.launch {
      try {
        repository.addHunt(hunt)
        clearErrorMsg()
      } catch (e: Exception) {
        Log.e("AddHuntViewModel", "Error adding Hunt", e)
        setErrorMsg("Failed to add Hunt: ${e.message}")
      }
    }
    return true
  }

  // --- Field setters with validation ---
  fun setTitle(title: String) {
    _uiState.value =
        _uiState.value.copy(
            title = title, invalidTitleMsg = if (title.isBlank()) "Title cannot be empty" else null)
  }

  fun setDescription(desc: String) {
    _uiState.value =
        _uiState.value.copy(
            description = desc,
            invalidDescriptionMsg = if (desc.isBlank()) "Description cannot be empty" else null)
  }

  fun setTime(time: String) {
    _uiState.value =
        _uiState.value.copy(
            time = time,
            invalidTimeMsg = if (time.toDoubleOrNull() == null) "Invalid time format" else null)
  }

  fun setDistance(distance: String) {
    _uiState.value =
        _uiState.value.copy(
            distance = distance,
            invalidDistanceMsg =
                if (distance.toDoubleOrNull() == null) "Invalid distance format" else null)
  }

  fun setDifficulty(difficulty: Difficulty) {
    _uiState.value = _uiState.value.copy(difficulty = difficulty)
  }

  fun setStatus(status: HuntStatus) {
    _uiState.value = _uiState.value.copy(status = status)
  }

  fun setAuthor(author: Author) {
    _uiState.value = _uiState.value.copy(author = author)
  }

  fun setImage(image: Int) {
    _uiState.value = _uiState.value.copy(image = image)
  }

  // --- Map-based location setters ---
  fun setStartLocation(location: Location) {
    _uiState.value = _uiState.value.copy(startLocation = location)
  }

  fun setEndLocation(location: Location) {
    _uiState.value = _uiState.value.copy(endLocation = location)
  }

  fun addMiddlePoint(location: Location) {
    val updatedPoints = _uiState.value.middlePoints + location
    _uiState.value = _uiState.value.copy(middlePoints = updatedPoints)
  }

  fun removeMiddlePoint(location: Location) {
    val updatedPoints = _uiState.value.middlePoints - location
    _uiState.value = _uiState.value.copy(middlePoints = updatedPoints)
  }
}
