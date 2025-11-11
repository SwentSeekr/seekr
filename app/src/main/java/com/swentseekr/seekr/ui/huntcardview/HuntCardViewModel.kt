package com.swentseekr.seekr.ui.huntcardview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.model.hunt.HuntReviewRepository
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HuntCardUiState(
    val hunt: Hunt? = null,
    val reviewList: List<HuntReview> = emptyList(),
    val isLiked: Boolean = false,
    val isAchieved: Boolean = false
)

open class HuntCardViewModel(
    private val repository: HuntsRepository = HuntRepositoryProvider.repository,
    private val repositoryReview: HuntReviewRepository = HuntReviewRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(HuntCardUiState())
  open val uiState: StateFlow<HuntCardUiState> = _uiState.asStateFlow()

  /** Loads reviews for a specific hunt.* */
  fun loadOtherReview(huntID: String) {
    viewModelScope.launch {
      try {
        val reviews = repositoryReview.getHuntReviews(huntID)
        _uiState.value = _uiState.value.copy(reviewList = reviews)
      } catch (e: Exception) {
        Log.e("ReviewHuntViewModel", "Error loading reviews for Hunt ID: $huntID", e)
      }
    }
  }

  /**
   * Loads a Hunt by its ID and updates the UI state.
   *
   * @param huntID The ID of the Hunt to be loaded.
   */
  fun loadHunt(huntID: String) {
    viewModelScope.launch {
      try {
        val hunt = repository.getHunt(huntID)
        _uiState.value =
            HuntCardUiState(
                hunt = hunt, isLiked = false, isAchieved = false, reviewList = emptyList())
      } catch (e: Exception) {
        Log.e("HuntCardViewModel", "Error loading Hunt by ID: $huntID", e)
      }
    }
  }

  /** Loads the Author of a Hunt by its ID. */
  fun loadHuntAuthor(huntID: String) {
    viewModelScope.launch {
      try {
        val hunt = repository.getHunt(huntID)
        val authorId = hunt.authorId
        // repositoryAuthor.getPseudo(authorId)
      } catch (e: Exception) {
        Log.e("HuntCardViewModel", "Error loading Author by ID: $huntID", e)
      }
    }
  }
  /** Deletes a Hunt by its ID. */
  fun deleteHunt(huntID: String) {
    viewModelScope.launch {
      try {
        repository.deleteHunt(huntID)
      } catch (e: Exception) {
        Log.e("HuntCardViewModel", "Error in deleting Hunt by ID: $huntID", e)
      }
    }
  }
  /** Edits a Hunt by its ID. */
  fun editHunt(huntID: String, newValue: Hunt) {
    viewModelScope.launch {
      try {
        repository.editHunt(huntID, newValue)
      } catch (e: Exception) {
        Log.e("HuntCardViewModel", "Error in editing Hunt by ID: $huntID", e)
      }
    }
  }
  /**
   * Toggles the 'like' botton of a hunt item identified by [huntID] and adds it to the profile
   * likesList. Will be modify later
   */
  fun onLikeClick(huntID: String) {
    val currentHuntUiState = _uiState.value
    // This will be added to the likesList in the profile
    // or remove if already liked
    val updatedHuntUiState = currentHuntUiState.copy(isLiked = !currentHuntUiState.isLiked)
    _uiState.value = updatedHuntUiState
  }

  /**
   * Filters the hunts to show only those that have been achieved by the user and adds it to the
   * profile AchievedList. Will be modify later
   */
  fun onDoneClick() {
    val currentHuntUiState = _uiState.value
    // This will be added to the AchivedList in the profile
    _uiState.value = currentHuntUiState.copy(isAchieved = true)
  }
}
