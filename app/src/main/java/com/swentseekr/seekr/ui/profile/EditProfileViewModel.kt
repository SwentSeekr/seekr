package com.swentseekr.seekr.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MAX_PSEUDONYM_LENGTH = 30
const val MAX_BIO_LENGTH = 200
const val PROFILE_PIC_DEFAULT = 0
/** UI state for editing a profile */
data class EditProfileUIState(
    val pseudonym: String = "",
    val bio: String = "",
    val profilePicture: Int = PROFILE_PIC_DEFAULT,
    val isSaving: Boolean = false,
    val hasChanges: Boolean = false,
    val canSave: Boolean = false,
    val errorMsg: String? = null,
    val success: Boolean = false,
    val profilePictureUri: Uri? = null, // picked from gallery/camera
    val profilePictureUrl: String = "", // from Firestore
    val isLoading: Boolean = false
)

/** ViewModel for editing the current user's profile */
class EditProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

  private val _uiState = MutableStateFlow(EditProfileUIState(isLoading = true))
  val uiState: StateFlow<EditProfileUIState> = _uiState.asStateFlow()

  /** Keep track of last saved profile for undo/cancel */
  private var lastSavedProfile: EditProfileUIState? = null
  private var lastSavedFullProfile: Profile? = null

  /** Load the profile for editing */
  fun loadProfile() {
    val uid = auth.currentUser?.uid
    if (uid == null) {
      _uiState.update { it.copy(errorMsg = "User not logged in") }
      return
    }
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(errorMsg = null)
      try {
        val profile = repository.getProfile(uid)
        if (profile == null) {
          _uiState.update { it.copy(isLoading = false, errorMsg = "Profile not found") }
          return@launch
        }
        lastSavedFullProfile = profile
        val state =
            EditProfileUIState(
                pseudonym = profile.author.pseudonym,
                bio = profile.author.bio,
                profilePicture = profile.author.profilePicture,
                profilePictureUrl = profile.author.profilePictureUrl,
                hasChanges = false,
                canSave = false,
                isLoading = false)
        lastSavedProfile = state
        _uiState.value = state
      } catch (e: Exception) {
        _uiState.value =
            EditProfileUIState(errorMsg = e.message ?: "Failed to load profile", isLoading = false)
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

  fun updateProfilePictureUri(uri: Uri?) {
    val newState = _uiState.value.copy(profilePictureUri = uri)
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
    val uid =
        auth.currentUser?.uid
            ?: run {
              _uiState.value = _uiState.value.copy(errorMsg = "User not loaded")
              return
            }

    if (!_uiState.value.canSave) return

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isSaving = true, errorMsg = null)
      try {
        val currentProfile =
            repository.getProfile(uid)
                ?: lastSavedFullProfile
                ?: throw Exception("Profile not found")

        val finalProfilePictureUrl =
            when {
              _uiState.value.profilePictureUri != null -> {
                repository.uploadProfilePicture(uid, _uiState.value.profilePictureUri!!)
              }
              _uiState.value.profilePictureUrl.isEmpty() &&
                  _uiState.value.profilePicture == PROFILE_PIC_DEFAULT &&
                  _uiState.value.profilePictureUri == null -> {
                ""
              }
              else -> currentProfile.author.profilePictureUrl
            }
        val updatedProfile =
            currentProfile.copy(
                author =
                    currentProfile.author.copy(
                        pseudonym = _uiState.value.pseudonym,
                        bio = _uiState.value.bio,
                        profilePictureUrl = finalProfilePictureUrl))
        repository.updateProfile(updatedProfile)

        val successState =
            _uiState.value.copy(
                isSaving = false,
                success = true,
                hasChanges = false,
                canSave = false,
                profilePictureUri = null,
                profilePictureUrl = finalProfilePictureUrl)
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
              it.profilePicture != newState.profilePicture ||
              it.profilePictureUrl != newState.profilePictureUrl ||
              it.profilePictureUri != newState.profilePictureUri
        } ?: true

    val canSave =
        hasChanges &&
            newState.pseudonym.isNotBlank() &&
            newState.pseudonym.length <= MAX_PSEUDONYM_LENGTH &&
            newState.bio.length <= MAX_BIO_LENGTH

    _uiState.value =
        newState.copy(hasChanges = hasChanges, canSave = canSave, success = false, errorMsg = null)
  }

  fun removeProfilePicture() {
    val newState =
        _uiState.value.copy(profilePicture = 0, profilePictureUri = null, profilePictureUrl = "")
    updateChangesFlags(newState)
  }
}
