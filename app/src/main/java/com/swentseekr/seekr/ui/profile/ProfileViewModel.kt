package com.swentseekr.seekr.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.ui.overview.HuntUiState
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
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(ProfileUIState())
  val uiState: StateFlow<ProfileUIState> = _uiState.asStateFlow()
  private var myHunts: MutableList<HuntUiState> = mutableListOf()
  private var doneHunts: MutableList<Hunt> = mutableListOf()
  private var likedHunts: MutableList<Hunt> = mutableListOf()

  // val currentUid: String? = Firebase.auth.currentUser?.uid
  val currentUid: String? =
      "testUserId" // Will be remove as soon as Firestore Emulator will be working

  fun loadProfile(userId: String) {
    viewModelScope.launch {
      try {
        val profile = repository.getProfile(userId)
        _uiState.value = ProfileUIState(profile = profile)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Profile not found")
      }
    }
  }

  fun refreshUIState() {
    val userId = _uiState.value.profile?.uid ?: return
    loadProfile(userId)
  }

  fun updateProfile(profile: Profile) {
    viewModelScope.launch {
      try {
        repository.updateProfile(profile)
        _uiState.value = _uiState.value.copy(profile = profile)
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
