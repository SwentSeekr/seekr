package com.swentseekr.seekr.ui.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryProvider
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.offline.cache.ProfileCache
import com.swentseekr.seekr.ui.profile.ProfileScreenConstants.EMPTY_REVIEW_RATE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val LOGIN_USER_ERROR = "User not logged in"

/** Represents the UI state for the Profile screen. */
data class ProfileUIState(
    val profile: Profile? = null,
    val isMyProfile: Boolean = false,
    val errorMsg: String? = null,
    val isLoading: Boolean = false
)

/**
 * ViewModel for the Profile screen. Responsible for managing the UI state, by fetching and
 * providing Profile data via the .
 */
class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val auth: FirebaseAuth? = null,
    private val injectedCurrentUid: String? = null // forUnitTests
) : ViewModel() {
  private val firebaseAuth: FirebaseAuth? by lazy {
    try {
      auth ?: FirebaseAuth.getInstance()
    } catch (_: IllegalStateException) {
      null // Firebase not initialized (e.g., during unit tests)
    }
  }

  private val _uiState = MutableStateFlow(ProfileUIState())
  val uiState: StateFlow<ProfileUIState> = _uiState.asStateFlow()

  val currentUid: String?
    get() = injectedCurrentUid ?: firebaseAuth?.currentUser?.uid

  private val _totalReviews = MutableStateFlow(0)
  val totalReviews: StateFlow<Int> = _totalReviews.asStateFlow()

  private val _reviewsState = MutableStateFlow<List<HuntReview>>(emptyList())
  val reviewsState: StateFlow<List<HuntReview>> = _reviewsState.asStateFlow()

  private fun updateUiState(transform: (ProfileUIState) -> ProfileUIState) {
    _uiState.value = transform(_uiState.value)
  }

  /**
   * Loads the total number of reviews for a given profile.
   *
   * Iterates through all hunts created by the profile and counts the number of reviews associated
   * with each hunt.
   *
   * Errors during review loading are logged but do not stop the process.
   *
   * @param profile Profile whose hunts are used to compute total reviews.
   */
  fun loadTotalReviewsForProfile(profile: Profile) {
    viewModelScope.launch {
      var total = 0
      profile.myHunts.forEach { hunt ->
        try {
          val reviews = HuntReviewRepositoryProvider.repository.getHuntReviews(hunt.uid)
          total += reviews.size
        } catch (e: Exception) {
          Log.e(
              ProfileViewModelConstants.PROFILE_VIEW_MODEL_TEST_TAG,
              "${ProfileViewModelConstants.FAIL_LOAD_REVIEWS} ${hunt.uid}",
              e)
        }
      }
      _totalReviews.value = total
    }
  }

  /**
   * Loads profile data for a given user.
   *
   * Behavior:
   * - Fetches profile and related hunts (created, completed, liked)
   * - Computes average review and sport ratings
   * - Determines whether the loaded profile belongs to the current user
   * - Optionally caches the profile locally
   *
   * Updates loading and error states accordingly.
   *
   * @param userId Optional user ID to load. Defaults to current user.
   * @param context Optional context used for local caching.
   */
  fun loadProfile(userId: String? = null, context: Context? = null) {
    val uidToLoad = userId ?: currentUid
    if (uidToLoad == null) {
      updateUiState { it.copy(errorMsg = LOGIN_USER_ERROR) }
      return
    }
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMsg = null)
      try {
        val profile = repository.getProfile(uidToLoad)
        val myHunts = repository.getMyHunts(uidToLoad)
        val doneHunts = repository.getDoneHunts(uidToLoad)
        val likedHunts = repository.getLikedHunts(uidToLoad)

        if (profile == null) {
          updateUiState {
            it.copy(errorMsg = ProfileViewModelConstants.PROFILE_NOT_FOUND, isLoading = false)
          }
          return@launch
        }

        val updatedProfile =
            buildProfileWithComputedRatings(
                profile = profile,
                myHunts = myHunts,
                doneHunts = doneHunts,
                likedHunts = likedHunts)

        if (context != null) {
          ProfileCache.saveProfile(context, updatedProfile)
        }
        updateUiState {
          it.copy(
              profile = updatedProfile, isMyProfile = uidToLoad == currentUid, isLoading = false)
        }
      } catch (e: Exception) {
        val msg =
            if (e.message?.contains(ProfileViewModelConstants.NOT_FOUND, ignoreCase = true) == true)
                ProfileViewModelConstants.PROFILE_NOT_FOUND
            else e.message ?: ProfileViewModelConstants.FAIL_LOAD_PROFILE

        updateUiState { it.copy(errorMsg = msg, isLoading = false) }
      }
    }
  }

  /**
   * Builds a profile with computed rating values.
   *
   * Computes:
   * - Average review rating from created hunts
   * - Average sport rating from completed hunts
   *
   * Returns a new profile instance with updated author ratings and refreshed hunt lists.
   *
   * @param profile Base profile information.
   * @param myHunts Hunts created by the user.
   * @param doneHunts Hunts completed by the user.
   * @param likedHunts Hunts liked by the user.
   */
  private fun buildProfileWithComputedRatings(
      profile: Profile,
      myHunts: List<Hunt>,
      doneHunts: List<Hunt>,
      likedHunts: List<Hunt>
  ): Profile {
    val reviewRate = calculateAverageReview(myHunts)
    val sportRate = calculateAverageSport(doneHunts)

    return profile.copy(
        author = profile.author.copy(reviewRate = reviewRate, sportRate = sportRate),
        myHunts = myHunts.toMutableList(),
        doneHunts = doneHunts.toMutableList(),
        likedHunts = likedHunts.toMutableList())
  }

  /**
   * Calculates the average review rating for the user's hunts.
   *
   * If no hunts are available, returns the minimum review rate. The result is clamped between
   * allowed minimum and maximum values.
   *
   * @param myHunts List of hunts created by the user.
   * @return Average review rating.
   */
  private fun calculateAverageReview(myHunts: List<Hunt>): Double {
    if (myHunts.isEmpty()) return ProfileViewModelNumbers.MIN_REVIEW_RATE
    return myHunts
        .map { it.reviewRate }
        .average()
        .coerceIn(ProfileViewModelNumbers.MIN_REVIEW_RATE, ProfileViewModelNumbers.MAX_REVIEW_RATE)
  }

  /**
   * Calculates the average sport rating based on completed hunts.
   *
   * Each hunt contributes points depending on its difficulty level. The final value is clamped
   * between allowed minimum and maximum values.
   *
   * @param doneHunts List of hunts completed by the user.
   * @return Average sport rating.
   */
  private fun calculateAverageSport(doneHunts: List<Hunt>): Double {
    if (doneHunts.isEmpty()) return ProfileViewModelNumbers.MIN_SPORT_RATE
    val points =
        doneHunts.map {
          when (it.difficulty) {
            Difficulty.EASY -> ProfileViewModelNumbers.EASY_SPORT_RATE
            Difficulty.INTERMEDIATE -> ProfileViewModelNumbers.INTERMEDIATE_SPORT_RATE
            Difficulty.DIFFICULT -> ProfileViewModelNumbers.DIFFICULT_SPORT_RATE
          }
        }
    return points
        .average()
        .coerceIn(ProfileViewModelNumbers.MIN_SPORT_RATE, ProfileViewModelNumbers.MAX_SPORT_RATE)
  }

  /**
   * Refreshes the current UI state.
   *
   * Reloads the profile using the currently displayed profile ID or the logged-in user ID if none
   * is available.
   *
   * @param context Optional context used for local caching.
   */
  fun refreshUIState(context: Context? = null) {
    {
      val uid = _uiState.value.profile?.uid ?: currentUid
      if (uid != null) {
        loadProfile(uid, context)
      }
    }
  }

  /**
   * Updates the current user's profile.
   *
   * Behavior:
   * - Ensures the user is logged in
   * - Persists profile changes to the repository
   * - Reloads the profile to refresh computed values
   *
   * @param profile Updated profile data.
   * @param context Optional context used for local caching.
   */
  fun updateProfile(profile: Profile, context: Context? = null) {
    viewModelScope.launch {
      val uid = currentUid
      if (uid == null) {
        updateUiState { it.copy(errorMsg = LOGIN_USER_ERROR) }
        return@launch
      }

      try {
        repository.updateProfile(profile.copy(uid = uid))
        loadProfile(uid, context)
      } catch (e: Exception) {
        updateUiState { it.copy(errorMsg = LOGIN_USER_ERROR) }
      }
    }
  }

  /**
   * Loads all hunts related to a given user.
   *
   * Fetches:
   * - Created hunts
   * - Completed hunts
   * - Liked hunts
   *
   * Updates the current profile with refreshed hunt data and recomputed ratings.
   *
   * @param userId User ID whose hunts should be loaded.
   */
  fun loadHunts(userId: String) {
    viewModelScope.launch {
      try {
        val myHunts = repository.getMyHunts(userId)
        val doneHunts = repository.getDoneHunts(userId)
        val likedHunts = repository.getLikedHunts(userId)
        updateUiState { state ->
          val currentProfile = state.profile
          if (currentProfile != null) {
            val updatedProfile =
                buildProfileWithComputedRatings(
                    profile = currentProfile,
                    myHunts = myHunts,
                    doneHunts = doneHunts,
                    likedHunts = likedHunts)
            state.copy(profile = updatedProfile)
          } else state
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = ProfileViewModelConstants.FAIL_LOAD_HUNTS)
      }
    }
  }

  /**
   * Builds a profile with computed ratings.
   *
   * Intended for testing or preview purposes. Uses the hunts already present in the profile.
   *
   * @param profile Base profile data.
   * @return Profile with computed rating values.
   */
  fun buildComputedProfile(profile: Profile): Profile {
    return buildProfileWithComputedRatings(
        profile = profile,
        myHunts = profile.myHunts,
        doneHunts = profile.doneHunts,
        likedHunts = profile.likedHunts)
  }

  /**
   * Loads all reviews for a given profile.
   *
   * Collects reviews from all hunts created by the profile. Updates:
   * - Review list state
   * - Total review count
   * - Average review rating on the profile
   *
   * @param profile Profile whose reviews should be loaded.
   */
  fun loadAllReviewsForProfile(profile: Profile) {
    viewModelScope.launch {
      val allReviews =
          profile.myHunts.flatMap { hunt ->
            try {
              HuntReviewRepositoryProvider.repository.getHuntReviews(hunt.uid)
            } catch (_: Exception) {
              emptyList()
            }
          }
      _reviewsState.value = allReviews
      _totalReviews.value = allReviews.size

      val newReviewRate =
          if (allReviews.isEmpty()) EMPTY_REVIEW_RATE else allReviews.map { it.rating }.average()

      _uiState.value =
          _uiState.value.copy(
              profile =
                  _uiState.value.profile?.copy(
                      author = _uiState.value.profile!!.author.copy(reviewRate = newReviewRate)))
    }
  }

  /**
   * Toggles the liked status of a hunt for the current user.
   *
   * If the hunt is already liked, it will be removed from the liked list. If not, it will be added
   * to the liked list.
   *
   * Updates both local UI state and persists changes to the repository.
   *
   * @param hunt Hunt to like or unlike.
   * @param context Optional context used for local caching.
   */
  fun toggleLikedHunt(hunt: Hunt, context: Context? = null) {
    viewModelScope.launch {
      val currentProfile = _uiState.value.profile ?: return@launch
      val userId = currentProfile.uid

      val isCurrentlyLiked = currentProfile.likedHunts.any { it.uid == hunt.uid }

      val updatedLikedHunts =
          if (isCurrentlyLiked) {
            currentProfile.likedHunts.filter { it.uid != hunt.uid }
          } else {
            currentProfile.likedHunts + hunt
          }

      val updatedProfile = currentProfile.copy(likedHunts = updatedLikedHunts.toMutableList())

      _uiState.value = _uiState.value.copy(profile = updatedProfile)

      try {
        if (isCurrentlyLiked) {
          repository.removeLikedHunt(userId, hunt.uid)
        } else {
          repository.addLikedHunt(userId, hunt.uid)
        }

        context?.let { ProfileCache.saveProfile(it, updatedProfile) }
      } catch (e: Exception) {
        Log.e(ProfileViewModelConstants.PROFILE_TEST_TAG, ProfileViewModelConstants.FAIL_LIKE, e)
      }
    }
  }
}
