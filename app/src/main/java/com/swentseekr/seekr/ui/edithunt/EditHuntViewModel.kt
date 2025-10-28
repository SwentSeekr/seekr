package com.swentseekr.seekr.ui.edithunt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the AddHunt screen */
data class EditHuntUIState(
    val uid: String = "",
    val title: String = "",
    val description: String = "",
    val points: List<Location> = emptyList(),
    val time: String = "",
    val distance: String = "",
    val difficulty: Difficulty? = null,
    val status: HuntStatus? = null,
    val image: Int = 0,
    val reviewRate: Double = 0.0,
    val authorId: String = "",
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

class EditHuntViewModel (
    private val repository: HuntsRepository = HuntRepositoryProvider.repository
): ViewModel(){

    private val _uiState = MutableStateFlow(EditHuntUIState())
    val uiState: StateFlow<EditHuntUIState> = _uiState.asStateFlow()

    private fun setErrorMsg(msg: String) {
        _uiState.value = _uiState.value.copy(errorMsg = msg)
    }

    fun loadHunt(huntId: String) {
        viewModelScope.launch{
            try {
                _uiState.value = _uiState.value.copy(
                    errorMsg = null,
                )
                val hunt = repository.getHunt(huntId)
                _uiState.value = _uiState.value.copy(
                    uid = hunt.uid,
                    title = hunt.title,
                    description = hunt.description,
                    points = listOf(hunt.start) + hunt.middlePoints + listOf(hunt.end),
                    time = hunt.time.toString(),
                    distance = hunt.distance.toString(),
                    difficulty = hunt.difficulty,
                    status = hunt.status,
                    image = hunt.image,
                    reviewRate = hunt.reviewRate,
                    authorId = hunt.authorId,
                    errorMsg = null
                )
            } catch (e: Exception) {
                Log.e("EditHuntViewModel", "Error loading hunt", e)
                _uiState.value = _uiState.value.copy(
                    errorMsg = "Failed to load hunt: ${e.message}",
                )
            }

        }
    }

    fun editHunt(): Boolean {
        val state = _uiState.value

        if (!state.isValid) {
            setErrorMsg("Please fill all required fields before saving.")
            return false
        }

        val updatedHunt = Hunt(
            uid = state.uid,
            start = state.points.first(),
            end = state.points.last(),
            middlePoints = state.points.drop(1).dropLast(1),
            status = state.status!!,
            title = state.title,
            description = state.description,
            time = state.time.toDouble(),
            distance = state.distance.toDouble(),
            difficulty = state.difficulty!!,
            authorId = state.authorId,
            image = state.image,
            reviewRate = state.reviewRate
        )

        viewModelScope.launch {
            try {
                repository.editHunt(state.uid, updatedHunt)
                _uiState.value = _uiState.value.copy(errorMsg = null, saveSuccessful = true)
            } catch (e: Exception) {
                Log.e("EditHuntViewModel", "Error updating hunt", e)
                _uiState.value = _uiState.value.copy(
                    errorMsg = "Failed to update hunt: ${e.message}",
                    saveSuccessful = false
                )
            }
        }

        return true
    }

    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            invalidTitleMsg = if (title.isBlank()) "Title cannot be empty" else null
        )
    }

    fun setDescription(desc: String) {
        _uiState.value = _uiState.value.copy(
            description = desc,
            invalidDescriptionMsg = if (desc.isBlank()) "Description cannot be empty" else null
        )
    }

    fun setTime(time: String) {
        _uiState.value = _uiState.value.copy(
            time = time,
            invalidTimeMsg = if (time.toDoubleOrNull() == null) "Invalid time format" else null
        )
    }

    fun setDistance(distance: String) {
        _uiState.value = _uiState.value.copy(
            distance = distance,
            invalidDistanceMsg = if (distance.toDoubleOrNull() == null) "Invalid distance format" else null
        )
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
        _uiState.value = _uiState.value.copy(errorMsg = null)
        return true
    }

    fun setIsSelectingPoints(isSelecting: Boolean) {
        _uiState.value = _uiState.value.copy(isSelectingPoints = isSelecting)
    }




}
