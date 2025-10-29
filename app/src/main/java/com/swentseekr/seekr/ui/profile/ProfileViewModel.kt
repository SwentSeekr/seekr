package com.swentseekr.seekr.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import kotlin.String
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Represents the UI state for the Profile screen. */
data class ProfileUIState(
    val profile: Profile? = null,
    val isMyProfile: Boolean = false,
    val errorMsg: String? = null,
)

/**
 * ViewModel for the Profile screen. Responsible for managing the UI state, by fetching and
 * providing Profile data via the .
 */
class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
  private val _uiState = MutableStateFlow(ProfileUIState())
  val uiState: StateFlow<ProfileUIState> = _uiState.asStateFlow()

  val currentUid: String?
    get() = auth.currentUser?.uid

  fun loadProfile(userId: String? = null) {
    val uidToLoad = userId ?: currentUid
    if (uidToLoad == null) {
      _uiState.value = ProfileUIState(errorMsg = "User not logged in")
      return
    }
    viewModelScope.launch {
      try {
        val profile = repository.getProfile(uidToLoad)
        if (profile != null) {
          val myHunts = repository.getMyHunts(uidToLoad)
          val doneHunts = repository.getDoneHunts(uidToLoad)
          val likedHunts = repository.getLikedHunts(uidToLoad)

          _uiState.value =
              ProfileUIState(
                  profile =
                      profile.copy(
                          myHunts = myHunts.toMutableList(),
                          doneHunts = doneHunts.toMutableList(),
                          likedHunts = likedHunts.toMutableList()),
                  isMyProfile = uidToLoad == currentUid)
        } else {
          _uiState.value = ProfileUIState(errorMsg = "Profile not found")
        }
      } catch (e: Exception) {
        _uiState.value = ProfileUIState(errorMsg = e.message ?: "Failed to load profile")
      }
    }
  }

  fun refreshUIState() {
    {
      val uid = _uiState.value.profile?.uid ?: currentUid
      if (uid != null) {
        loadProfile(uid)
      }
    }

    fun updateProfile(profile: Profile) {
      viewModelScope.launch {
        val uid = currentUid
        if (uid == null) {
          _uiState.value = _uiState.value.copy(errorMsg = "User not logged in")
          return@launch
        }

        try {
          repository.updateProfile(profile.copy(uid = uid))
          loadProfile(uid)
        } catch (e: Exception) {
          _uiState.value = _uiState.value.copy(errorMsg = "Failed to update profile")
        }
      }
    }

    fun loadHunts(userId: String) {
      viewModelScope.launch {
        try {
          val myHunts = repository.getMyHunts(userId)
          val doneHunts = repository.getDoneHunts(userId)
          val likedHunts = repository.getLikedHunts(userId)
          val currentProfile = _uiState.value.profile
          if (currentProfile != null) {
            _uiState.value =
                _uiState.value.copy(
                    profile =
                        currentProfile.copy(
                            myHunts = myHunts as MutableList<Hunt>,
                            doneHunts = doneHunts as MutableList<Hunt>,
                            likedHunts = likedHunts as MutableList<Hunt>))
          }
        } catch (e: Exception) {
          _uiState.value = _uiState.value.copy(errorMsg = "Failed to load hunts")
        }
      }
    }
  }
}
