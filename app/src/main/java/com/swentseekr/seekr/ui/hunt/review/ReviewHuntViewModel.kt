package com.swentseekr.seekr.ui.hunt.review

import android.net.Uri
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
import com.swentseekr.seekr.model.hunt.IReviewImageRepository
import com.swentseekr.seekr.model.hunt.ReviewImageRepositoryProvider
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.ui.profile.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val invalidRating: String? = null,
    val authorProfile: Profile? = null
) {
  val isValid: Boolean
    get() = reviewText.isNotBlank()
}

open class ReviewHuntViewModel(
    private val repositoryHunt: HuntsRepository = HuntRepositoryProvider.repository,
    private val repositoryReview: HuntReviewRepository = HuntReviewRepositoryProvider.repository,
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val imageRepository: IReviewImageRepository = ReviewImageRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(ReviewHuntUIState())
  open val uiState: StateFlow<ReviewHuntUIState> = _uiState.asStateFlow()
  /** Clears any existing error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }
  /** Sets an error message in the UI state. */
  fun setErrorMsg(error: String) {
    _uiState.value = _uiState.value.copy(errorMsg = error)
  }

  /** Loads a hunt by its ID and updates the UI state. */
  open fun loadHunt(huntId: String) {
    viewModelScope.launch {
      try {
        val hunt = repositoryHunt.getHunt(huntId)
        // _uiState.value = ReviewHuntUIState(hunt = hunt)
        _uiState.update { it.copy(hunt = hunt) }
      } catch (e: Exception) {
        Log.e(
            AddReviewScreenStrings.ReviewViewModel,
            "${AddReviewScreenStrings.ErrorLoadingHunt} $huntId",
            e)
      }
    }
  }

  /** Loads the profile of the Maker of the hunt */
  open fun loadAuthorProfile(userId: String) {
    viewModelScope.launch {
      try {
        val profile = profileRepository.getProfile(userId)
        _uiState.value = _uiState.value.copy(authorProfile = profile)
      } catch (e: Exception) {
        Log.e(
            AddReviewScreenStrings.HuntCardViewModel,
            "${AddReviewScreenStrings.ErrorLoadingProfil} $userId",
            e)
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
        Log.e(AddReviewScreenStrings.ReviewViewModel, AddReviewScreenStrings.ErrorReviewHunt, e)
        setErrorMsg("${AddReviewScreenStrings.FailSubmitReview} ${e.message}")
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
        val currentUid = currentUserId ?: AddReviewScreenStrings.NoCurrentUser
        if (userID == currentUid) {
          val review = repositoryReview.getReviewHunt(reviewID)
          val photosToDelete = review.photos
          for (photoUrl in photosToDelete) {
            imageRepository.deleteReviewPhoto(photoUrl)
          }
          repositoryReview.deleteReviewHunt(reviewId = reviewID)
        } else {
          setErrorMsg(AddReviewScreenStrings.ErrorDeleteReview)
        }
      } catch (e: Exception) {
        Log.e(AddReviewScreenStrings.ReviewViewModel, AddReviewScreenStrings.ErrorDeleteHunt, e)
        setErrorMsg("${AddReviewScreenStrings.FailDeleteHunt} ${e.message}")
      }
    }
  }

  /** Sets the review text and validates it. */
  fun setReviewText(text: String) {
    _uiState.value =
        _uiState.value.copy(
            reviewText = text,
            invalidReviewText = if (text.isBlank()) AddReviewScreenStrings.ReviewNotEmpty else null)
  }

  /** Sets the rating for the review and validates it. */
  fun setRating(rating: Double) {
    _uiState.value =
        _uiState.value.copy(
            rating = rating,
            invalidRating =
                if (rating <= 0.0 || rating > 5.0) AddReviewScreenStrings.InvalidRating else null)
  }

  /**
   * Handles the submit button click event. Validates the input and submits the review to the
   * repository.
   */
  fun submitReviewHunt(userId: String, hunt: Hunt) {
    val state = _uiState.value
    if (!state.isValid) {
      setErrorMsg(AddReviewScreenStrings.ErrorSubmisson)
      return
    }
    _uiState.value = _uiState.value.copy(isSubmitted = true)
    reviewHuntToRepository(userId, hunt)
  }

  /** Adds a photo to the current list of photos in the UI state. */
  fun addPhoto(myPhoto: String, userId: String? = null) {
    val uid = userId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
    val uri = Uri.parse(myPhoto)

    viewModelScope.launch {
      try {
        val downloadUrl = imageRepository.uploadReviewPhoto(uid, uri)
        val updated = _uiState.value.photos + downloadUrl
        _uiState.value = _uiState.value.copy(photos = updated)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to upload photo: ${e.message}")
      }
    }
  }

  /** Removes a photo from the current list of photos in the UI state. */
  fun removePhoto(myPhoto: String) {
    viewModelScope.launch {
      try {
        imageRepository.deleteReviewPhoto(myPhoto)
        val updated = _uiState.value.photos - myPhoto
        _uiState.value = _uiState.value.copy(photos = updated)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to delete image: ${e.message}")
      }
    }
  }

  /** Updates the rating in the UI state. */
  fun updateRating(newRating: Double) {
    _uiState.value = _uiState.value.copy(rating = newRating)
  }

  /** Clears the review form if the review was submitted successfully. */
  fun clearForm() {
    if (_uiState.value.saveSuccessful) {
      clearFormCancel()
    } else {
      setErrorMsg(AddReviewScreenStrings.ErrorClearSubmitReview)
    }
  }

  /** Clears the review form when click on cancel */
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

  /** Submits the current user's review for the given hunt. */
  fun submitCurrentUserReview(hunt: Hunt) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: AddReviewScreenStrings.User0
    submitReviewHunt(userId, hunt)
  }
}
