package com.swentseekr.seekr.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for editing a profile */
data class EditProfileUIState(
    val pseudonym: String = "",
    val bio: String = "",
    val profilePicture: Int = 0,
    val isSaving: Boolean = false,
    val hasChanges: Boolean = false,
    val canSave: Boolean = false,
    val errorMsg: String? = null,
    val success: Boolean = false
)

/** ViewModel for editing the current user's profile */
class EditProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(EditProfileUIState())
  val uiState: StateFlow<EditProfileUIState> = _uiState.asStateFlow()

  /** Keep track of last saved profile for undo/cancel */
  private var lastSavedProfile: EditProfileUIState? = null
  private var lastSavedFullProfile: Profile? = null

  private var currentUserId: String? = null

  /** Load the profile for editing */
  fun loadProfile(userId: String) {
    currentUserId = userId
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(errorMsg = null)
      try {
        val profile = repository.getProfile(userId)
        if (profile != null) {
          val state =
              EditProfileUIState(
                  pseudonym = profile.author.pseudonym,
                  bio = profile.author.bio,
                  profilePicture = profile.author.profilePicture,
                  hasChanges = false,
                  canSave = false)
          lastSavedProfile = state
          lastSavedFullProfile = profile
          _uiState.value = state
        } else {
          _uiState.value = EditProfileUIState(errorMsg = "Profile not found")
          lastSavedProfile = null
          lastSavedFullProfile = null
        }
      } catch (e: Exception) {
        _uiState.value = EditProfileUIState(errorMsg = e.message ?: "Failed to load profile")
        lastSavedProfile = null
        lastSavedFullProfile = null
      }
    }
  }

  /** Update pseudonym and recompute flags */
  fun updatePseudonym(pseudonym: String) {
    val newState = _uiState.value.copy(pseudonym = pseudonym)
    updateChangesFlags(newState)
  }

  /** Update bio and recompute flags */
  fun updateBio(bio: String) {
    val newState = _uiState.value.copy(bio = bio)
    updateChangesFlags(newState)
  }

  /** Update profile picture and recompute flags */
  fun updateProfilePicture(profilePicture: Int) {
    val newState = _uiState.value.copy(profilePicture = profilePicture)
    updateChangesFlags(newState)
  }

  /** Cancel changes and revert to last saved state */
  fun cancelChanges() {
    lastSavedProfile?.let {
      _uiState.value =
          it.copy(hasChanges = false, canSave = false, errorMsg = null, success = false)
    }
  }

  /** Save changes to Firestore */
  fun saveProfile() {
    val userId =
        currentUserId
            ?: run {
              _uiState.value = _uiState.value.copy(errorMsg = "User not loaded")
              return
            }

    if (!_uiState.value.canSave) return

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isSaving = true, errorMsg = null)
      try {
        val currentProfile =
            repository.getProfile(userId)
                ?: lastSavedFullProfile
                ?: throw Exception("Profile not found")
        val updatedProfile =
            currentProfile.copy(
                author =
                    currentProfile.author.copy(
                        pseudonym = _uiState.value.pseudonym,
                        bio = _uiState.value.bio,
                        profilePicture = _uiState.value.profilePicture))
        repository.updateProfile(updatedProfile)
        val successState =
            _uiState.value.copy(
                isSaving = false, success = true, hasChanges = false, canSave = false)
        lastSavedProfile = successState
        lastSavedFullProfile = updatedProfile

        _uiState.value = successState
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(isSaving = false, errorMsg = e.message ?: "Failed to save profile")
      }
    }
  }

  /** Helper to compute hasChanges and canSave */
  private fun updateChangesFlags(newState: EditProfileUIState) {
    val hasChanges =
        lastSavedProfile?.let {
          it.pseudonym != newState.pseudonym ||
              it.bio != newState.bio ||
              it.profilePicture != newState.profilePicture
        } ?: true

    val canSave =
        hasChanges &&
            newState.pseudonym.isNotBlank() &&
            newState.pseudonym.length <= 30 &&
            newState.bio.length <= 200

    _uiState.value =
        newState.copy(hasChanges = hasChanges, canSave = canSave, success = false, errorMsg = null)
  }

  fun removeProfilePicture() {
    val newState = _uiState.value.copy(profilePicture = 0)
    updateChangesFlags(newState)
  }
}
