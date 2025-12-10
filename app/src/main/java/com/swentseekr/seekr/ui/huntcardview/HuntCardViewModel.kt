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
import com.swentseekr.seekr.model.hunt.IReviewImageRepository
import com.swentseekr.seekr.model.hunt.ReviewImageRepositoryProvider
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.ui.profile.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Hunt Card screen.
 *
 * @param hunt The currently loaded hunt (if any).
 * @param reviewList List of reviews for the hunt.
 * @param isLiked Whether the current user has liked this hunt.
 * @param isAchieved Whether the current user has completed this hunt.
 * @param errorMsg Optional error message to show in the UI.
 * @param currentUserId The ID of the logged-in user.
 * @param authorProfile The profile of the hunt's author (single hunt view).
 * @param authorProfiles Map of multiple author profiles (for overview screen).
 */
data class HuntCardUiState(
    val hunt: Hunt? = null,
    val reviewList: List<HuntReview> = emptyList(),
    val isLiked: Boolean = false,
    val isAchieved: Boolean = false,
    val errorMsg: String? = null,
    val currentUserId: String? = null,
    val authorProfile: Profile? = null,
    val authorProfiles: Map<String, Profile?> = emptyMap()
)

/**
 * ViewModel responsible for managing the state and business logic of the Hunt Card screen.
 *
 * Handles:
 * - Loading hunts and their reviews
 * - Loading author profiles
 * - Tracking likes and achievements
 * - Deleting hunts and reviews
 * - User interaction state (likes, achievements)
 *
 * @property huntRepository Repository to load, edit, and delete hunts.
 * @property reviewRepository Repository to load and manage hunt reviews.
 * @property profileRepository Repository to load and manage user profiles.
 * @property imageRepository Repository to manage review images.
 */
open class HuntCardViewModel(
    private val huntRepository: HuntsRepository = HuntRepositoryProvider.repository,
    private val reviewRepository: HuntReviewRepository = HuntReviewRepositoryProvider.repository,
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val imageRepository: IReviewImageRepository = ReviewImageRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(HuntCardUiState())
  open val uiState: StateFlow<HuntCardUiState> = _uiState.asStateFlow()

  val _likedHuntsCache = MutableStateFlow<Set<String>>(emptySet())

  open val likedHuntsCache: StateFlow<Set<String>> = _likedHuntsCache.asStateFlow()

  fun isHuntLiked(huntId: String): Boolean {
    return _likedHuntsCache.value.contains(huntId)
  }

  /** Clears any existing error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }
  /**
   * Sets an error message in the UI state.
   *
   * @param error The message to display.
   */
  fun setErrorMsg(error: String) {
    _uiState.value = _uiState.value.copy(errorMsg = error)
  }

  /**
   * Loads the profile of the author of a hunt and updates [authorProfile].
   *
   * @param userID The ID of the author whose profile is being fetched.
   */
  open fun loadAuthorProfile(userID: String) {
    viewModelScope.launch {
      try {
        val profile = profileRepository.getProfile(userID)
        _uiState.value = _uiState.value.copy(authorProfile = profile)
      } catch (e: Exception) {
        Log.e(
            HuntCardViewModelConstants.HuntCardTag,
            "${HuntCardViewModelConstants.ErrorLoadingProfil} $userID",
            e)
        setErrorMsg(HuntCardViewModelConstants.ErrorLoadingProfileSetMsg)
      }
    }
  }

  /**
   * Loads the profile of a specific author and adds it to the authorProfiles map in the UI state.
   *
   * @param userId The ID of the author whose profile should be loaded.
   */
  fun loadMultipleAuthorProfiles(userId: String) {
    viewModelScope.launch {
      try {
        val profile = profileRepository.getProfile(userId)

        _uiState.value =
            _uiState.value.copy(
                authorProfiles =
                    _uiState.value.authorProfiles.toMutableMap().apply { put(userId, profile) })
      } catch (e: Exception) {
        Log.e("HuntCardViewModel", "Error loading author $userId", e)
        setErrorMsg("Error loading author profile")
      }
    }
  }

  /**
   * Loads current user ID in the UI state.
   *
   * Also loads the user's liked hunts into cache.
   */
  open fun loadCurrentUserID() {
    viewModelScope.launch {
      try {
        val userID =
            FirebaseAuth.getInstance().currentUser?.uid ?: HuntCardViewModelConstants.UnknownUser
        _uiState.value = _uiState.value.copy(currentUserId = userID)
        loadLikedHuntsCache(userID)
      } catch (e: Exception) {
        Log.e(
            HuntCardViewModelConstants.HuntCardTag,
            HuntCardViewModelConstants.ErrorLoadingCurrentUser,
            e)
        setErrorMsg(HuntCardViewModelConstants.ErrorLodingCurrentUserSetMsg)
      }
    }
  }

  private fun loadLikedHuntsCache(userId: String) {
    viewModelScope.launch {
      try {
        val likedHunts = profileRepository.getLikedHunts(userId)
        _likedHuntsCache.value = likedHunts.map { it.uid }.toSet()
      } catch (e: Exception) {
        Log.e("HuntCardViewModel", "Error loading liked hunts cache", e)
      }
    }
  }

  /**
   * Loads all reviews belonging to a hunt and updates the UI state.
   *
   * @param huntID The ID of the hunt whose reviews are being fetched.
   */
  open fun loadOtherReview(huntID: String) {
    viewModelScope.launch {
      try {
        val reviews = reviewRepository.getHuntReviews(huntID)
        _uiState.value = _uiState.value.copy(reviewList = reviews)
      } catch (e: Exception) {
        Log.e(
            HuntCardViewModelConstants.ReviewHuntTag,
            "${HuntCardViewModelConstants.ErrorLoadingOtherReviews} $huntID",
            e)
        setErrorMsg(HuntCardViewModelConstants.ErrorLoadingOtherReviewsSetMsg)
      }
    }
  }

  /**
   * Loads a Hunt by its ID and updates the UI state.
   *
   * @param huntID The ID of the Hunt to be loaded.
   */
  open fun loadHunt(huntID: String) {
    viewModelScope.launch {
      try {
        val hunt = huntRepository.getHunt(huntID)
        val reviews = reviewRepository.getHuntReviews(huntID)
        val userId = _uiState.value.currentUserId
        val currentUserLikes =
            if (userId != null) {
              profileRepository.getLikedHunts(userId)
            } else emptyList()

        val isLiked = currentUserLikes.any { it.uid == huntID }

        _uiState.value = _uiState.value.copy(hunt = hunt, isLiked = isLiked, reviewList = reviews)

        val currentUserAchieved =
            if (userId != null) {
              profileRepository.getDoneHunts(userId)
            } else emptyList()

        val isAchieved = currentUserAchieved.any { it.uid == huntID }

        _uiState.value =
            _uiState.value.copy(
                hunt = hunt, isLiked = isLiked, isAchieved = isAchieved, reviewList = reviews)
      } catch (e: Exception) {
        Log.e(
            HuntCardViewModelConstants.HuntCardTag,
            "${HuntCardViewModelConstants.ErrorLoadingHunt} $huntID",
            e)
        setErrorMsg(HuntCardViewModelConstants.ErrorLoadingHuntSetMsg)
      }
    }
  }

  /**
   * Loads the Author of a Hunt by its ID.
   *
   * @param huntID The ID of the hunt whose author is requested.
   */
  fun loadHuntAuthor(huntID: String) {
    viewModelScope.launch {
      try {
        val hunt = huntRepository.getHunt(huntID)
        val authorId = hunt.authorId
        // repositoryAuthor.getPseudo(authorId)
      } catch (e: Exception) {
        Log.e(
            HuntCardViewModelConstants.HuntCardTag,
            "${HuntCardViewModelConstants.ErrorLoadingHuntAuthor} $huntID",
            e)
        setErrorMsg(HuntCardViewModelConstants.ErrorLOadingHuntAuthorSetMsg)
      }
    }
  }
  /**
   * Deletes a Hunt by its ID.
   *
   * @param huntID the id of the hunt to delete.
   */
  fun deleteHunt(huntID: String) {
    viewModelScope.launch {
      try {
        huntRepository.deleteHunt(huntID)
      } catch (e: Exception) {
        Log.e(
            HuntCardViewModelConstants.HuntCardTag,
            "${HuntCardViewModelConstants.ErrorDeleteHunt} $huntID",
            e)
        setErrorMsg(HuntCardViewModelConstants.ErrorDeleteHuntSetMsg)
      }
    }
  }

  /**
   * Deletes a review if the user is the author.
   *
   * @param huntID The ID of the hunt the review belongs to.
   * @param reviewID The ID of the review to delete.
   * @param userID The ID of the user attempting the deletion.
   * @param currentUserId Optional: The ID of the currently logged-in user; defaults to
   *   Firebase.currentUser.
   */
  fun deleteReview(
      huntID: String,
      reviewID: String,
      userID: String,
      currentUserId: String? = Firebase.auth.currentUser?.uid
  ) {
    viewModelScope.launch {
      try {
        val currentUid = currentUserId ?: HuntCardViewModelConstants.NoUser
        if (userID == currentUid) {
          val review = reviewRepository.getReviewHunt(reviewID)
          val photosToDelete = review.photos
          for (photoUrl in photosToDelete) {
            try {
              imageRepository.deleteReviewPhoto(photoUrl)
            } catch (e: Exception) {
              Log.e(
                  HuntCardViewModelConstants.ReviewHuntTag,
                  "${HuntCardViewModelConstants.ErrorDeletingPhoto} $photoUrl",
                  e)
              setErrorMsg("${HuntCardViewModelConstants.ErrorDeletingPhotoSetMsg} ${e.message}")
            }
          }
          reviewRepository.deleteReviewHunt(reviewId = reviewID)
          loadOtherReview(huntID)
        } else {
          setErrorMsg(HuntCardViewModelConstants.ErrorDeleteReviewSetMsg)
        }
      } catch (e: Exception) {
        Log.e(
            HuntCardViewModelConstants.ReviewHuntTag,
            HuntCardViewModelConstants.ErrorDeletingReview,
            e)
        setErrorMsg("${HuntCardViewModelConstants.ErrorDeletingReviewSetMsg} ${e.message}")
      }
    }
  }

  /**
   * Updates a hunt with new data.
   *
   * @param huntID The ID of the hunt to update.
   * @param newValue The updated Hunt object containing new values.
   */
  fun editHunt(huntID: String, newValue: Hunt) {
    viewModelScope.launch {
      try {
        huntRepository.editHunt(huntID, newValue)
      } catch (e: Exception) {
        Log.e(
            HuntCardViewModelConstants.HuntCardTag,
            "${HuntCardViewModelConstants.ErrorEditHunt} $huntID",
            e)
        setErrorMsg(HuntCardViewModelConstants.ErrorEditHuntSetMsg)
      }
    }
  }
  /**
   * Toggles the like state of a hunt.
   *
   * Updates:
   * - Local cache (`likedHuntsCache`)
   * - Firebase profile repository
   *
   * @param huntID The ID of the hunt to like or unlike.
   */
  open fun onLikeClick(huntID: String) {
    val currentUserId = _uiState.value.currentUserId ?: return
    val currentlyLiked = _likedHuntsCache.value.contains(huntID)

    _likedHuntsCache.value =
        _likedHuntsCache.value.toMutableSet().apply {
          if (currentlyLiked) remove(huntID) else add(huntID)
        }

    _uiState.value = _uiState.value.copy(isLiked = !currentlyLiked)
    viewModelScope.launch {
      try {
        if (currentlyLiked) profileRepository.removeLikedHunt(currentUserId, huntID)
        else profileRepository.addLikedHunt(currentUserId, huntID)
      } catch (e: Exception) {
        // Revert on error
        _likedHuntsCache.value =
            _likedHuntsCache.value.toMutableSet().apply {
              if (currentlyLiked) add(huntID) else remove(huntID)
            }
        _uiState.value = _uiState.value.copy(isLiked = currentlyLiked)
        setErrorMsg("Failed to update liked hunt: ${e.message}")
      }
    }
  }

  /**
   * Initializes the UI state for the Hunt Card screen.
   *
   * Used when loading a hunt inside an existing user session.
   *
   * @param userId The ID of the current user.
   * @param hunt The Hunt to display.
   */
  fun initialize(userId: String, hunt: Hunt) {
    viewModelScope.launch {
      try {
        val likedHunts = profileRepository.getLikedHunts(userId)
        val isLiked = likedHunts.any { it.uid == hunt.uid }

        _uiState.value = HuntCardUiState(hunt = hunt, currentUserId = userId, isLiked = isLiked)
      } catch (e: Exception) {
        _uiState.value = HuntCardUiState(hunt = hunt, currentUserId = userId, isLiked = false)
      }
    }
  }

  /**
   * Marks the current hunt as "achieved" (completed) for the current user.
   *
   * Updates:
   * - The Firebase profile repository
   * - UI state
   */
  fun onDoneClick() {
    val currentHuntUiState = _uiState.value
    val currentUserId = _uiState.value.currentUserId
    // This will be added to the AchivedList in the profile
    val hunt = currentHuntUiState.hunt
    if (hunt == null) {
      setErrorMsg(HuntCardViewModelConstants.ErrorOnDoneLoading)
    } else {
      viewModelScope.launch {
        try {
          // Call the suspend function inside a coroutine
          profileRepository.addDoneHunt(currentUserId ?: HuntCardViewModelConstants.Empty, hunt)

          // Update UI state
          _uiState.value = currentHuntUiState.copy(isAchieved = true)
        } catch (e: Exception) {
          Log.e(
              HuntCardViewModelConstants.HuntCardTag, HuntCardViewModelConstants.ErrorOnDonClick, e)
          setErrorMsg("${HuntCardViewModelConstants.ErrorOnDoneClickSetMsg} ${e.message}")
        }
      }
    }
  }
}
