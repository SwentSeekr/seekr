package com.swentseekr.seekr.ui.hunt.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the AddHunt screen */
data class AddHuntUIState(
    val title: String = "",
    val description: String = "",
    val points: List<Location> = emptyList(),
    val time: String = "",
    val distance: String = "",
    val difficulty: Difficulty? = null,
    val status: HuntStatus? = null,
    val image: Int = 0,
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

/** ViewModel for the AddHunt screen */
class AddHuntViewModel(
    private val repository: HuntsRepository = HuntRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(AddHuntUIState())
  val uiState: StateFlow<AddHuntUIState> = _uiState.asStateFlow()
  private var testMode: Boolean = false

  /** Clears error message */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Sets error message */
  private fun setErrorMsg(error: String) {
    _uiState.value = _uiState.value.copy(errorMsg = error)
  }

  /** Reset save success flag to avoid repeated navigation */
  fun resetSaveSuccess() {
    _uiState.value = _uiState.value.copy(saveSuccessful = false)
  }

  /** Adds a new Hunt */
  fun addHunt(): Boolean {
    val state = _uiState.value

    if (!state.isValid) {
      setErrorMsg("Please fill all required fields before creating a hunt.")
      return false
    }

    if (testMode) {
      _uiState.value = _uiState.value.copy(errorMsg = null, saveSuccessful = true)
      return true
    }

    if (FirebaseAuth.getInstance().currentUser?.uid == null) {
      setErrorMsg("You must be logged in to create a hunt.")
      return false
    }

    val hunt =
        Hunt(
            uid = repository.getNewUid(),
            start = state.points.first(),
            end = state.points.last(),
            middlePoints = state.points.drop(1).dropLast(1),
            status = state.status!!,
            title = state.title,
            description = state.description,
            time = state.time.toDouble(),
            distance = state.distance.toDouble(),
            difficulty = state.difficulty!!,
            authorId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown",
            image = state.image,
            reviewRate = state.reviewRate)

    viewModelScope.launch {
      try {
        repository.addHunt(hunt)
        _uiState.value = _uiState.value.copy(errorMsg = null, saveSuccessful = true)
      } catch (e: Exception) {
        Log.e("AddHuntViewModel", "Error adding Hunt", e)
        _uiState.value =
            _uiState.value.copy(
                errorMsg = "Failed to add Hunt: ${e.message}", saveSuccessful = false)
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

  fun setImage(image: Int) {
    _uiState.value = _uiState.value.copy(image = image)
  }

  fun setPoints(points: List<Location>): Boolean {
    if (points.size < 2) {
      setErrorMsg("A hunt must have at least a start and end point.")
      return false
    }
    _uiState.value = _uiState.value.copy(points = points)
    clearErrorMsg()
    return true
  }

  fun setIsSelectingPoints(isSelecting: Boolean) {
    _uiState.value = _uiState.value.copy(isSelectingPoints = isSelecting)
  }

  // For testing purposes
  fun setTestMode(enabled: Boolean) {
    testMode = enabled
  }
}
