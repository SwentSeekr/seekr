package com.swentseekr.seekr.ui.hunt

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.add.AddHuntViewModel
import com.swentseekr.seekr.ui.hunt.edit.EditHuntViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    _uiState.value =
        _uiState.value.copy(
            time = time,
            invalidTimeMsg =
                if (time.toDoubleOrNull() == null) BaseHuntViewModelMessages.INVALID_TIME else null)
  }

  fun setDistance(distance: String) {
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
  }

  fun setPoints(points: List<Location>): Boolean {
    if (points.size < BaseHuntViewModelDefault.MIN_SET_POINT) {
      setErrorMsg(BaseHuntViewModelMessages.INVALID_SET_POINT)
      return false
    }
    _uiState.value = _uiState.value.copy(points = points)
    clearErrorMsg()
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
}
