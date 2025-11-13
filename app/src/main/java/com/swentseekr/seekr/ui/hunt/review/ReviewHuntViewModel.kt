package com.swentseekr.seekr.ui.hunt.review

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewHuntUIState(
    val hunt: Hunt? = null,
    val huntId: String = "",
    val userId: String = "",
    val reviewText: String = "",
    val rating: Double = 0.0,
    val isSubmitted: Boolean = false,
    val photos: List<String> = emptyList(),
    val errorMsg: String? = null,
    val saveSuccessful: Boolean = false,
    val invalidReviewText: String? = null,
    val invalidRating: String? = null
) {
  val isValid: Boolean
    get() = reviewText.isNotBlank() && rating >= 0.0 && rating <= 5.0
}

class ReviewHuntViewModel(
    private val repositoryHunt: HuntsRepository = HuntRepositoryProvider.repository,
    private val repositoryReview: HuntReviewRepository = HuntReviewRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(ReviewHuntUIState())
  val uiState: StateFlow<ReviewHuntUIState> = _uiState.asStateFlow()
  /** Clears any existing error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }
  /** Sets an error message in the UI state. */
  fun setErrorMsg(error: String) {
    _uiState.value = _uiState.value.copy(errorMsg = error)
  }

  /** Loads a hunt by its ID and updates the UI state. */
  fun loadHunt(huntID: String) {
    viewModelScope.launch {
      try {
        val hunt = repositoryHunt.getHunt(huntID)
        _uiState.value = ReviewHuntUIState(hunt = hunt)
      } catch (e: Exception) {
        Log.e("ReviewHuntViewModel", "Error loading Hunt by ID: $huntID", e)
      }
    }
  }

  /** Submits the review to the repository. */
  private fun reviewHuntToRepository(id: String, hunt: Hunt) {
    viewModelScope.launch {
      try {
        val createdReview =
            HuntReview(
                reviewId = repositoryReview.getNewUid(),
                authorId = id,
                huntId = hunt.uid,
                rating = _uiState.value.rating,
                comment = _uiState.value.reviewText,
                photos = _uiState.value.photos)
        repositoryReview.addReviewHunt(createdReview)
        _uiState.value =
            _uiState.value.copy(saveSuccessful = true, errorMsg = null, isSubmitted = true)
      } catch (e: Exception) {
        Log.e("ReviewHuntViewModel", "Error review Hunt", e)
        setErrorMsg("Failed to submit review: ${e.message}")
        _uiState.value = _uiState.value.copy(saveSuccessful = false)
      }
    }
  }
  /** Deletes a review if the user is the author. */
  fun deleteReview(
      reviewID: String,
      userID: String,
      currentUserId: String? = Firebase.auth.currentUser?.uid
  ) {
    viewModelScope.launch {
      try {
        val currentUid = currentUserId ?: "None (B2)"
        if (userID == currentUid) {
          repositoryReview.deleteReviewHunt(reviewId = reviewID)
        } else {
          setErrorMsg("You can only delete your own review.")
        }
      } catch (e: Exception) {
        Log.e("ReviewHuntViewModel", "Error deleting Review for hunt", e)
        setErrorMsg("Failed to delete Hunt: ${e.message}")
      }
    }
  }

  /** Sets the review text and validates it. */
  fun setReviewText(text: String) {
    _uiState.value =
        _uiState.value.copy(
            reviewText = text,
            invalidReviewText = if (text.isBlank()) "The review cannot be empty" else null)
  }

  /** Sets the rating for the review and validates it. */
  fun setRating(rating: Double) {
    _uiState.value =
        _uiState.value.copy(
            rating = rating,
            invalidRating =
                if (rating <= 0.0 || rating > 5.0) "Rating must be between 1 and 5" else null)
  }

  /**
   * Handles the submit button click event. Validates the input and submits the review to the
   * repository.
   */
  fun submitReviewHunt(userId: String, hunt: Hunt) {
    val state = _uiState.value
    if (!state.isValid) {
      setErrorMsg("At least one field is not valid")
      return
    }
    _uiState.value = _uiState.value.copy(isSubmitted = true)
    reviewHuntToRepository(userId, hunt)
  }

  /** Adds a photo to the current list of photos in the UI state. */
  fun addPhoto(myPhoto: String) {
    val currentPhotos = _uiState.value.photos.toMutableList()
    currentPhotos.add(myPhoto)
    _uiState.value = _uiState.value.copy(photos = currentPhotos)
  }

  /** Removes a photo from the current list of photos in the UI state. */
  fun removePhoto(myPhoto: String) {
    val currentPhotos = _uiState.value.photos.toMutableList()
    currentPhotos.remove(myPhoto)
    _uiState.value = _uiState.value.copy(photos = currentPhotos)
  }

  fun updateRating(newRating: Double) {
    _uiState.value = _uiState.value.copy(rating = newRating)
  }

  /** Clears the review form if the review was submitted successfully. */
  fun clearForm() {
    if (_uiState.value.saveSuccessful) {
      clearFormCancel()
    } else {
      setErrorMsg("Cannot clear form, review not submitted successfully.")
    }
  }

  fun clearFormCancel() {
    _uiState.value =
        _uiState.value.copy(
            reviewText = "",
            rating = 0.0,
            photos = emptyList(),
            isSubmitted = false,
            saveSuccessful = false,
            invalidReviewText = null,
            invalidRating = null,
            errorMsg = null)
  }

  fun submitCurrentUserReview(hunt: Hunt) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "0"
    submitReviewHunt(userId, hunt)
  }
}
