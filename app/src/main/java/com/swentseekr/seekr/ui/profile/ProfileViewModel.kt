package com.swentseekr.seekr.ui.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryProvider
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.offline.cache.ProfileCache
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

  private fun updateUiState(transform: (ProfileUIState) -> ProfileUIState) {
    _uiState.value = transform(_uiState.value)
  }

  fun loadTotalReviewsForProfile(profile: Profile) {
    viewModelScope.launch {
      var total = 0
      profile.myHunts.forEach { hunt ->
        try {
          val reviews = HuntReviewRepositoryProvider.repository.getHuntReviews(hunt.uid)
          total += reviews.size
        } catch (e: Exception) {
          Log.e("ProfileViewModel", "Failed to load reviews for hunt ${hunt.uid}", e)
        }
      }
      _totalReviews.value = total
    }
  }

  fun loadProfile(userId: String? = null,context: Context? = null) {
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
          updateUiState { it.copy(errorMsg = "Profile not found", isLoading = false) }
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
            if (e.message?.contains("not found", ignoreCase = true) == true) "Profile not found"
            else e.message ?: "Failed to load profile"

        updateUiState { it.copy(errorMsg = msg, isLoading = false) }
      }
    }
  }

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

  private fun calculateAverageReview(myHunts: List<Hunt>): Double {
    if (myHunts.isEmpty()) return 0.0
    return myHunts.map { it.reviewRate }.average().coerceIn(0.0, 5.0)
  }

  private fun calculateAverageSport(doneHunts: List<Hunt>): Double {
    if (doneHunts.isEmpty()) return 0.0
    val points =
        doneHunts.map {
          when (it.difficulty) {
            Difficulty.EASY -> 1.0
            Difficulty.INTERMEDIATE -> 3.0
            Difficulty.DIFFICULT -> 5.0
          }
        }
    return points.average().coerceIn(0.0, 5.0)
  }

  fun refreshUIState(context: Context? = null) {
    {
      val uid = _uiState.value.profile?.uid ?: currentUid
      if (uid != null) {
        loadProfile( uid,context)
      }
    }
  }

  fun updateProfile(profile: Profile, context: Context? = null) {
    viewModelScope.launch {
      val uid = currentUid
      if (uid == null) {
        updateUiState { it.copy(errorMsg = LOGIN_USER_ERROR) }
        return@launch
      }

      try {
        repository.updateProfile(profile.copy(uid = uid))
        loadProfile(uid,context)
      } catch (e: Exception) {
        updateUiState { it.copy(errorMsg = LOGIN_USER_ERROR) }
      }
    }
  }

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
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to load hunts")
      }
    }
  }
  // For testing or preview purposes: builds a profile with computed averages
  fun buildComputedProfile(profile: Profile): Profile {
    return buildProfileWithComputedRatings(
        profile = profile,
        myHunts = profile.myHunts,
        doneHunts = profile.doneHunts,
        likedHunts = profile.likedHunts)
  }
}
