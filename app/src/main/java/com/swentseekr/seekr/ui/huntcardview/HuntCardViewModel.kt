package com.swentseekr.seekr.ui.huntcardview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.model.hunt.HuntReviewRepository
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.ui.profile.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HuntCardUiState(
    val hunt: Hunt? = null,
    val reviewList: List<HuntReview> = emptyList(),
    val isLiked: Boolean = false,
    val isAchieved: Boolean = false,
    val errorMsg: String? = null,
    val currentUserId: String? = null,
    val authorProfile: Profile? = null
)

open class HuntCardViewModel(
    private val huntRepository: HuntsRepository = HuntRepositoryProvider.repository,
    private val reviewRepository: HuntReviewRepository = HuntReviewRepositoryProvider.repository,
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(HuntCardUiState())
  open val uiState: StateFlow<HuntCardUiState> = _uiState.asStateFlow()

  /** Clears any existing error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }
  /** Sets an error message in the UI state. */
  fun setErrorMsg(error: String) {
    _uiState.value = _uiState.value.copy(errorMsg = error)
  }

  /** Loads the profile of the Maker of the hunt */
  fun loadAuthorProfile(userID: String) {
    viewModelScope.launch {
      try {
        val profile = profileRepository.getProfile(userID)
        _uiState.value = _uiState.value.copy(authorProfile = profile)
      } catch (e: Exception) {
        Log.e("HuntCardViewModel", "Error loading user profile for User ID: $userID", e)
      }
    }
  }

  /** Loads current user ID in the UI state. */
  fun loadCurrentUserID() {
    viewModelScope.launch {
      try {
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
        _uiState.value = _uiState.value.copy(currentUserId = userID)
      } catch (e: Exception) {
        Log.e("HuntCardViewModel", "Error loading current user ID", e)
      }
    }
  }

  /** Loads reviews for a specific hunt.* */
  fun loadOtherReview(huntID: String) {
    viewModelScope.launch {
      try {
        val reviews = reviewRepository.getHuntReviews(huntID)
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
        val hunt = huntRepository.getHunt(huntID)
        val reviews = reviewRepository.getHuntReviews(huntID)
        val userId = _uiState.value.currentUserId
        val currentUserLikes =
            if (userId != null) {
              profileRepository.getLikedHunts(userId)
            } else emptyList<String>()
        val isLiked = currentUserLikes.contains(huntID)
        val currentUserAchieved =
            if (userId != null) {
              profileRepository.getDoneHunts(userId)
            } else emptyList<Hunt>()
        val isAchieved = currentUserAchieved.any { it.uid == huntID }
        // isLiked will be chnged later when we will have an addLike in the profile repository
        _uiState.value =
            _uiState.value.copy(
                hunt = hunt, isLiked = false, isAchieved = isAchieved, reviewList = reviews)
      } catch (e: Exception) {
        Log.e("HuntCardViewModel", "Error loading Hunt by ID: $huntID", e)
      }
    }
  }

  /** Loads the Author of a Hunt by its ID. */
  fun loadHuntAuthor(huntID: String) {
    viewModelScope.launch {
      try {
        val hunt = huntRepository.getHunt(huntID)
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
        huntRepository.deleteHunt(huntID)
      } catch (e: Exception) {
        Log.e("HuntCardViewModel", "Error in deleting Hunt by ID: $huntID", e)
      }
    }
  }

  /** Deletes a review if the user is the author. */
  fun deleteReview(
      huntID: String,
      reviewID: String,
      userID: String,
      currentUserId: String? = Firebase.auth.currentUser?.uid
  ) {
    viewModelScope.launch {
      try {
        val currentUid = currentUserId ?: "None (B2)"
        if (userID == currentUid) {
          reviewRepository.deleteReviewHunt(reviewId = reviewID)
          loadOtherReview(huntID)
        } else {
          setErrorMsg("You can only delete your own review.")
        }
      } catch (e: Exception) {
        Log.e("ReviewHuntViewModel", "Error deleting Review for hunt", e)
        setErrorMsg("Failed to delete Hunt: ${e.message}")
      }
    }
  }
  /** Edits a Hunt by its ID. */
  fun editHunt(huntID: String, newValue: Hunt) {
    viewModelScope.launch {
      try {
        huntRepository.editHunt(huntID, newValue)
      } catch (e: Exception) {
        Log.e("HuntCardViewModel", "Error in editing Hunt by ID: $huntID", e)
      }
    }
  }
  /**
   * Toggles the 'like' botton of a hunt item identified by [huntID] and adds it to the profile
   * likesList. Will be modify later
   */
  open fun onLikeClick(huntID: String) {
    val currentHuntUiState = _uiState.value
    val currentUserId = _uiState.value.currentUserId
    val currentlyLiked = _uiState.value.isLiked
    // This will be added to the likesList in the profile
    // or remove if already liked
    /*
    if(currentlyLiked){
      profileRepository.removeLikedHunt(currentUserId ?: "", huntID)
    }
    else {
      profileRepository.addLikedHunt(currentUserId ?: "", huntID)
    }

     */

    val updatedHuntUiState = currentHuntUiState.copy(isLiked = !currentlyLiked)
    _uiState.value = updatedHuntUiState
  }

  fun initialize(userId: String, hunt: Hunt) {
    // This is legal
    _uiState.value = HuntCardUiState(hunt = hunt, currentUserId = userId)
  }
  /**
   * Filters the hunts to show only those that have been achieved by the user and adds it to the
   * profile AchievedList. Will be modify later
   */
  fun onDoneClick() {
    val currentHuntUiState = _uiState.value
    val currentUserId = _uiState.value.currentUserId
    // This will be added to the AchivedList in the profile
    val hunt = currentHuntUiState.hunt
    if (hunt == null) {
      setErrorMsg("Hunt data is not loaded.")
    } else {
      viewModelScope.launch {
        try {
          // Call the suspend function inside a coroutine
          profileRepository.addDoneHunt(currentUserId ?: "", hunt)

          // Update UI state
          _uiState.value = currentHuntUiState.copy(isAchieved = true)
        } catch (e: Exception) {
          setErrorMsg("Failed to mark hunt as done: ${e.message}")
        }
      }
    }
  }
}
