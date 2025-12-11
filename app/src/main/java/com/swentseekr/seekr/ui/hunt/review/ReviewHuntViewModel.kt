package com.swentseekr.seekr.ui.hunt.review

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
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
import com.swentseekr.seekr.model.notifications.NotificationHelper
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.ui.profile.Profile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for reviewing a hunt.
 *
 * @property hunt Currently loaded [Hunt] being reviewed, or `null` if none is loaded.
 * @property huntId Identifier of the hunt being reviewed.
 * @property userId Identifier of the user creating or editing the review.
 * @property reviewText Text content of the review.
 * @property rating Star rating associated with the review.
 * @property isSubmitted `true` if the review submit action has been triggered.
 * @property photos List of photo URLs attached to the review.
 * @property errorMsg Optional error message to display to the user.
 * @property saveSuccessful `true` if the review was successfully saved.
 * @property invalidReviewText Optional validation message for the review text.
 * @property invalidRating Optional validation message for the rating value.
 * @property authorProfiles Map from author user ID to their [Profile], or `null` while loading.
 */
data class ReviewHuntUIState(
    val hunt: Hunt? = null,
    val huntId: String = AddReviewScreenStrings.Empty,
    val reviewId: String = AddReviewScreenStrings.Empty,
    val userId: String = AddReviewScreenStrings.Empty,
    val reviewText: String = AddReviewScreenStrings.Empty,
    val rating: Double = AddReviewScreenDefaults.Rating,
    val isSubmitted: Boolean = false,
    val photos: List<String> = emptyList(),
    val errorMsg: String? = null,
    val saveSuccessful: Boolean = false,
    val invalidReviewText: String? = null,
    val invalidRating: String? = null,
    val authorProfiles: Map<String, Profile?> = emptyMap()
) {
  /**
   * Indicates whether the current review data is valid.
   *
   * @return `true` if the review text is not blank, `false` otherwise.
   */
  val isValid: Boolean
    get() = reviewText.isNotBlank()
}

/**
 * ViewModel responsible for loading hunts, managing review data, and interacting with repositories
 * for reviews, profiles, and review images.
 *
 * @property repositoryHunt Repository used to load [Hunt] data.
 * @property repositoryReview Repository used to create, read, and delete [HuntReview] data.
 * @property profileRepository Repository used to load author profile information.
 * @property imageRepository Repository used to upload and delete review images.
 * @property dispatcher Coroutine dispatcher used for image-related operations.
 */
open class ReviewHuntViewModel(
    private val repositoryHunt: HuntsRepository = HuntRepositoryProvider.repository,
    private val repositoryReview: HuntReviewRepository = HuntReviewRepositoryProvider.repository,
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val imageRepository: IReviewImageRepository = ReviewImageRepositoryProvider.repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

  private val _uiState = MutableStateFlow(ReviewHuntUIState())

  /** Public immutable [StateFlow] exposing the current [ReviewHuntUIState]. */
  open val uiState: StateFlow<ReviewHuntUIState> = _uiState.asStateFlow()

  private var lastSavedReview: ReviewHuntUIState? = null

  /**
   * Clears any existing error message in the UI state.
   *
   * @return Unit
   */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /**
   * Sets an error message in the UI state.
   *
   * @param error Error message to display to the user.
   * @return Unit
   */
  fun setErrorMsg(error: String) {
    _uiState.value = _uiState.value.copy(errorMsg = error)
  }

  /**
   * Sets the list of photos in the UI state. Intended for use in tests.
   *
   * @param list List of photo URLs to set.
   * @return Unit
   */
  fun setPhotosForTest(list: List<String>) {
    _uiState.value = _uiState.value.copy(photos = list)
  }

  /**
   * Loads a hunt by its ID and updates the UI state.
   *
   * @param huntId Unique identifier of the [Hunt] to load.
   * @return Unit
   */
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

  fun loadReview(reviewId: String) {
    viewModelScope.launch {
      try {
        val review = repositoryReview.getReviewHunt(reviewId)
        // _uiState.value = ReviewHuntUIState(hunt = hunt)

        // Update only review-specific fields, preserve hunt data
        _uiState.update { currentState ->
          currentState.copy(
              reviewId = reviewId,
              userId = review.authorId,
              reviewText = review.comment,
              photos = review.photos,
              rating = review.rating)
        }
      } catch (e: Exception) {
        Log.e(
            AddReviewScreenStrings.ReviewViewModel,
            "${AddReviewScreenStrings.ErrorLoadingHunt} $reviewId",
            e)
      }
    }
  }

  /**
   * Loads the profile of the author of a hunt or review and stores it in [ReviewHuntUIState].
   *
   * @param userId Unique identifier of the user whose [Profile] should be loaded.
   * @return Unit
   */
  open fun loadAuthorProfile(userId: String) {
    viewModelScope.launch {
      try {
        val profile = profileRepository.getProfile(userId)
        _uiState.update { state ->
          state.copy(authorProfiles = state.authorProfiles + (userId to profile))
        }
      } catch (e: Exception) {
        Log.e(
            AddReviewScreenStrings.HuntCardViewModel,
            "${AddReviewScreenStrings.ErrorLoadingProfil} $userId",
            e)
      }
    }
  }

  /**
   * Submits the review to the repository and updates the UI state.
   *
   * This method constructs a [HuntReview] from the current UI state, adds it to [repositoryReview],
   * optionally sends a notification, and updates flags such as [ReviewHuntUIState.saveSuccessful]
   * and [ReviewHuntUIState.isSubmitted].
   *
   * @param id Identifier of the user submitting the review.
   * @param hunt [Hunt] being reviewed.
   * @param context Optional [Context] used to display a notification. If `null`, no notification is
   *   sent.
   * @return Unit
   */
  private fun reviewHuntToRepository(id: String, hunt: Hunt, context: Context?) {
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
        if (context != null) {
          NotificationHelper.sendNotification(
              context,
              AddReviewScreenStrings.NEW_REVIEW_TITLE,
              AddReviewScreenStrings.NEW_REVIEW_MESSAGE)
        }
        _uiState.value =
            _uiState.value.copy(saveSuccessful = true, errorMsg = null, isSubmitted = true)
      } catch (e: Exception) {
        Log.e(AddReviewScreenStrings.ReviewViewModel, AddReviewScreenStrings.ErrorReviewHunt, e)
        setErrorMsg("${AddReviewScreenStrings.FailSubmitReview} ${e.message}")
        _uiState.value = _uiState.value.copy(saveSuccessful = false)
      }
    }
  }

  /** Updates an existing review in the repository. */
  private fun updateReviewInRepository(reviewId: String, hunt: Hunt, context: Context?) {
    viewModelScope.launch {
      try {
        val updatedReview =
            HuntReview(
                reviewId = reviewId, // Use existing review ID
                authorId =
                    FirebaseAuth.getInstance().currentUser?.uid ?: AddReviewScreenStrings.User0,
                huntId = hunt.uid,
                rating = _uiState.value.rating,
                comment = _uiState.value.reviewText,
                photos = _uiState.value.photos)

        // Update the review instead of adding a new one
        repositoryReview.updateReviewHunt(reviewId, updatedReview)

        if (context != null) {
          NotificationHelper.sendNotification(
              context, "Review Updated", "Your review has been updated successfully")
        }

        _uiState.value =
            _uiState.value.copy(saveSuccessful = true, errorMsg = null, isSubmitted = true)
      } catch (e: Exception) {
        Log.e(AddReviewScreenStrings.ReviewViewModel, "Error updating review", e)
        setErrorMsg("Failed to update review: ${e.message}")
        _uiState.value = _uiState.value.copy(saveSuccessful = false)
      }
    }
  }
  /**
   * Deletes a review (and its associated photos) if the given user is the author.
   *
   * @param reviewID Identifier of the review to delete.
   * @param userID Identifier of the user requesting deletion (the review author).
   * @param currentUserId Optional current user ID. If `null`, the ID is fetched from Firebase. This
   *   is mainly useful for testing.
   * @return Unit
   */
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
            try {
              imageRepository.deleteReviewPhoto(photoUrl)
            } catch (e: Exception) {
              Log.e(
                  AddReviewScreenStrings.ReviewViewModel,
                  "${AddReviewScreenStrings.ErrorDeletingPhoto} $photoUrl",
                  e)
            }
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

  /**
   * Sets the review text and updates validation state accordingly.
   *
   * @param text New review text entered by the user.
   * @return Unit
   */
  fun setReviewText(text: String) {
    _uiState.value =
        _uiState.value.copy(
            reviewText = text,
            invalidReviewText = if (text.isBlank()) AddReviewScreenStrings.ReviewNotEmpty else null)
  }

  /**
   * Sets the rating for the review and updates validation errors if necessary.
   *
   * @param rating New rating value, expected to be in the range (0.0, 5.0].
   * @return Unit
   */
  fun setRating(rating: Double) {
    _uiState.value =
        _uiState.value.copy(
            rating = rating,
            invalidRating =
                if (rating <= 0.0 || rating > 5.0) AddReviewScreenStrings.InvalidRating else null)
  }

  /**
   * Handles the submit button click event.
   *
   * Validates the current UI state and, if valid, triggers the submission of the review to the
   * repository.
   *
   * @param userId Identifier of the user submitting the review.
   * @param hunt [Hunt] being reviewed.
   * @param context Optional [Context] used for notifications.
   * @return Unit
   */
  fun submitReviewHunt(userId: String, hunt: Hunt, context: Context?) {
    val state = _uiState.value
    if (!state.isValid) {
      setErrorMsg(AddReviewScreenStrings.ErrorSubmisson)
      return
    }
    if (state.reviewId.isNotEmpty()) {
      updateReviewInRepository(state.reviewId, hunt, context)
    } else {
      _uiState.value = _uiState.value.copy(isSubmitted = true)
      reviewHuntToRepository(userId, hunt, context)
    }
  }

  /**
   * Adds a photo to the review by uploading it and updating the UI state's photo list.
   *
   * @param myPhoto Local URI string of the photo to upload.
   * @param userId Optional user ID to associate with the uploaded photo. If `null`, the ID is taken
   *   from the currently authenticated Firebase user.
   * @return Unit
   */
  fun addPhoto(myPhoto: String, userId: String? = null) {
    val uid = userId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
    val uri = myPhoto.toUri()

    viewModelScope.launch(dispatcher) {
      try {
        val downloadUrl = imageRepository.uploadReviewPhoto(uid, uri)
        val updated = _uiState.value.photos + downloadUrl
        _uiState.value = _uiState.value.copy(photos = updated)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                errorMsg = "${AddReviewScreenStrings.ErrorAddingPhoto} ${e.message}")
      }
    }
  }

  /**
   * Removes a photo from the review by deleting it remotely and updating the UI state's photo list.
   *
   * @param myPhoto URL of the photo to remove.
   * @return Unit
   */
  fun removePhoto(myPhoto: String) {
    viewModelScope.launch(dispatcher) {
      try {
        imageRepository.deleteReviewPhoto(myPhoto)
        val updated = _uiState.value.photos - myPhoto
        _uiState.value = _uiState.value.copy(photos = updated)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                errorMsg = "${AddReviewScreenStrings.ErrorDeletingImages} ${e.message}")
      }
    }
  }

  /**
   * Updates the rating in the UI state.
   *
   * This does not perform validation. Use [setRating] if you also want validation.
   *
   * @param newRating New rating value to set.
   * @return Unit
   */
  fun updateRating(newRating: Double) {
    _uiState.value = _uiState.value.copy(rating = newRating)
  }

  /**
   * Loads the given list of review image URLs into the UI state's photo list.
   *
   * @param photoUrls List of existing review photo URLs.
   * @return Unit
   */
  fun loadReviewImages(photoUrls: List<String>) {
    _uiState.value = _uiState.value.copy(photos = photoUrls)
  }

  /**
   * Clears the review form if the review was submitted successfully.
   *
   * If the save was not successful, sets an error message instead.
   *
   * @return Unit
   */
  fun clearForm() {
    if (_uiState.value.saveSuccessful) {
      clearFormCancel()
    } else {
      setErrorMsg(AddReviewScreenStrings.ErrorClearSubmitReview)
    }
  }

  /**
   * Clears the review form and deletes any selected photos without submitting the review.
   *
   * This is typically called when the user cancels review creation.
   *
   * @return Unit
   */
  fun clearFormNoSubmission() {
    for (photo in _uiState.value.photos) {
      viewModelScope.launch(dispatcher) {
        try {
          imageRepository.deleteReviewPhoto(photo)
        } catch (e: Exception) {
          _uiState.value =
              _uiState.value.copy(
                  errorMsg = "${AddReviewScreenStrings.ErrorCancleImage} ${e.message}")
        }
      }
    }
    clearFormCancel()
  }

  /**
   * Resets the review form to its initial state.
   *
   * This is used by both [clearForm] and [clearFormNoSubmission].
   *
   * @return Unit
   */
  fun clearFormCancel() {
    _uiState.value =
        _uiState.value.copy(
            reviewText = AddReviewScreenStrings.Empty,
            rating = AddReviewScreenDefaults.Rating,
            photos = emptyList(),
            isSubmitted = false,
            saveSuccessful = false,
            invalidReviewText = null,
            invalidRating = null,
            errorMsg = null)
  }

  /**
   * Submits the current user's review for the given hunt.
   *
   * The current user ID is obtained from [FirebaseAuth]. If no user is logged in, a default ID is
   * used as fallback.
   *
   * @param hunt [Hunt] being reviewed.
   * @param context Optional [Context] used for notifications.
   * @return Unit
   */
  fun submitCurrentUserReview(hunt: Hunt, context: Context?) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: AddReviewScreenStrings.User0
    submitReviewHunt(userId, hunt, context)
  }

  /**
   * Loads a review by its ID and updates the UI state's list of photo URLs.
   *
   * @param reviewId Identifier of the review to load.
   * @return A [kotlinx.coroutines.Job] representing the launched coroutine.
   */
  /*
  fun loadReview(reviewId: String) =
      viewModelScope.launch {
        try {
          val review = repositoryReview.getReviewHunt(reviewId)

          _uiState.update { it.copy(photos = review.photos) }
        } catch (e: Exception) {
          Log.e("ReviewHuntViewModel", "Failed to load review $reviewId", e)
        }
      }

   */
}
