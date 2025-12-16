package com.swentseekr.seekr.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.model.profile.ProfileUtils
import com.swentseekr.seekr.ui.auth.OnboardingFlowStrings
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.MAX_BIO_LENGTH
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.MAX_PSEUDONYM_LENGTH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for editing a profile.
 *
 * @property pseudonym The user's pseudonym.
 * @property bio The user's biography.
 * @property profilePicture Resource ID for the profile picture.
 * @property isSaving Whether the profile is currently being saved.
 * @property hasChanges Whether there are unsaved changes.
 * @property canSave Whether the current state can be saved.
 * @property errorMsg Optional error message to display in the UI.
 * @property success Whether the last save operation was successful.
 * @property profilePictureUri URI of a selected profile picture from gallery or camera.
 * @property profilePictureUrl URL of the profile picture stored in the backend (e.g., Firestore).
 * @property isLoading Whether the profile is currently being loaded.
 */
data class EditProfileUIState(
    val pseudonym: String = EditProfileStrings.EMPTY_STRING,
    val bio: String = EditProfileStrings.EMPTY_STRING,
    val profilePicture: Int = EditProfileNumberConstants.PROFILE_PIC_DEFAULT,
    val isSaving: Boolean = false,
    val hasChanges: Boolean = false,
    val canSave: Boolean = false,
    val errorMsg: String? = null,
    val success: Boolean = false,
    val profilePictureUri: Uri? = null, // picked from gallery/camera
    val profilePictureUrl: String = EditProfileStrings.EMPTY_STRING, // from Firestore
    val pseudonymError: String? = null,
    val isCheckingPseudonym: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * ViewModel for managing the UI state of editing the current user's profile. Handles loading,
 * updating, and saving profile data using [ProfileRepository] and manages UI state via
 * [EditProfileUIState] with [StateFlow].
 *
 * @property repository The repository to read/write profiles.
 * @property auth FirebaseAuth instance to obtain the current user's UID.
 */
class EditProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

  private val _uiState = MutableStateFlow(EditProfileUIState(isLoading = true))
  val uiState: StateFlow<EditProfileUIState> = _uiState.asStateFlow()

  /** Keep track of last saved profile for undo/cancel */
  private var lastSavedProfile: EditProfileUIState? = null
  private var lastSavedFullProfile: Profile? = null

  private val _cameraError = MutableStateFlow<String?>(null)
  val cameraError = _cameraError.asStateFlow()

  /** Load the profile for editing */
  fun loadProfile() {
    val uid = auth.currentUser?.uid
    if (uid == null) {
      _uiState.update { it.copy(errorMsg = EditProfileStrings.LOG_IN_ERROR) }
      return
    }
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(errorMsg = null)
      try {
        val profile = repository.getProfile(uid)
        if (profile == null) {
          _uiState.update { it.copy(isLoading = false, errorMsg = EditProfileStrings.NO_PROFILE) }
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
            EditProfileUIState(
                errorMsg = e.message ?: EditProfileStrings.LOAD_PROFILE_ERROR, isLoading = false)
        lastSavedProfile = null
        lastSavedFullProfile = null
      }
    }
  }

  /**
   * Updates the pseudonym in the UI state and recomputes change-related flags.
   *
   * Triggers validation and determines whether the profile can be saved.
   *
   * @param pseudonym New pseudonym entered by the user.
   */
  fun updatePseudonym(pseudonym: String) {
    val newState = _uiState.value.copy(pseudonym = pseudonym)
    updateChangesFlags(newState)
  }

  /**
   * Validates the user’s chosen pseudonym locally and triggers availability check if valid.
   *
   * @param pseudonym The pseudonym entered by the user.
   * - If invalid (format or empty), sets an error message.
   * - If valid and non-empty, triggers async availability check via `checkPseudonymAvailability`.
   */
  fun validatePseudonym(pseudonym: String) {
    _uiState.value =
        _uiState.value.copy(
            pseudonymError =
                when {
                  ProfileUtils().isValidPseudonym(pseudonym) -> null
                  else -> OnboardingFlowStrings.ERROR_PSEUDONYM_INVALID
                })

    if (_uiState.value.pseudonymError == null && pseudonym.isNotBlank()) {
      checkPseudonymAvailability(pseudonym)
    }
    updatePseudonym(pseudonym)
  }

  /**
   * Asynchronously checks if the given pseudonym is available (not already taken).
   *
   * Updates state to show loading indicator, then:
   * - Sets error if pseudonym is taken.
   * - Clears error if available.
   * - Handles network/error cases gracefully.
   *
   * @param pseudonym The pseudonym to check for availability.
   */
  private fun checkPseudonymAvailability(pseudonym: String) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isCheckingPseudonym = true)

      try {
        val isAvailable =
            pseudonym == lastSavedFullProfile?.author?.pseudonym ||
                pseudonym !in repository.getAllPseudonyms()

        _uiState.value =
            _uiState.value.copy(
                pseudonymError =
                    if (!isAvailable) {
                      OnboardingFlowStrings.ERROR_PSEUDONYM_TAKEN
                    } else {
                      null
                    },
                isCheckingPseudonym = false)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(pseudonymError = null, isCheckingPseudonym = false)
      }
    }
  }

  /**
   * Updates the profile picture URI selected from gallery or camera.
   *
   * Used when the user selects a local image that has not yet been uploaded.
   *
   * @param uri URI of the selected image, or null if cleared.
   */
  fun updateBio(bio: String) {
    val newState = _uiState.value.copy(bio = bio)
    updateChangesFlags(newState)
  }

  /**
   * Updates the profile picture URI selected from gallery or camera.
   *
   * Used when the user selects a local image that has not yet been uploaded.
   *
   * @param uri URI of the selected image, or null if cleared.
   */
  fun updateProfilePictureUri(uri: Uri?) {
    val newState = _uiState.value.copy(profilePictureUri = uri)
    updateChangesFlags(newState)
  }

  /**
   * Updates the profile picture resource identifier and recomputes change flags.
   *
   * Used when selecting a predefined avatar or bundled drawable.
   *
   * @param profilePicture Resource ID of the selected profile picture.
   */
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
              _uiState.value = _uiState.value.copy(errorMsg = EditProfileStrings.LOAD_USER_ERROR)
              return
            }

    if (!_uiState.value.canSave) return

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isSaving = true, errorMsg = null)
      try {
        val currentProfile =
            repository.getProfile(uid)
                ?: lastSavedFullProfile
                ?: throw Exception(EditProfileStrings.NO_PROFILE)

        val profilePictureUri = _uiState.value.profilePictureUri
        val profilePictureUrlState = _uiState.value.profilePictureUrl

        val finalProfilePictureUrl =
            when {
              profilePictureUri != null -> {
                val oldUrl = currentProfile.author.profilePictureUrl
                if (oldUrl.isNotEmpty()) {
                  repository.deleteCurrentProfilePicture(uid, oldUrl)
                }
                repository.uploadProfilePicture(uid, profilePictureUri)
              }
              profilePictureUrlState.isEmpty() &&
                  _uiState.value.profilePicture ==
                      EditProfileNumberConstants.PROFILE_PIC_DEFAULT -> {
                EditProfileStrings.EMPTY_STRING
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
            _uiState.value.copy(
                isSaving = false, errorMsg = e.message ?: EditProfileStrings.SAVE_PROFILE_ERROR)
      }
    }
  }

  /**
   * Recomputes change-tracking and validation flags for the edit-profile screen.
   *
   * Determines:
   * - Whether the current UI state differs from the last saved profile
   * - Whether the profile can be persisted based on validation rules
   *
   * Validation rules:
   * - Pseudonym must be non-blank and within MAX_PSEUDONYM_LENGTH
   * - Bio length must not exceed MAX_BIO_LENGTH
   *
   * @param newState Candidate UI state after a mutation.
   */
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
  /**
   * Removes the current profile picture, resetting it to the default and updates the repository and
   * UI state accordingly
   */
  fun removeProfilePicture() {
    viewModelScope.launch {
      val uid = auth.currentUser?.uid ?: return@launch
      val currentUrl = _uiState.value.profilePictureUrl

      if (currentUrl.isNotEmpty()) {
        repository.deleteCurrentProfilePicture(uid, currentUrl)
      }

      val updatedProfile =
          lastSavedFullProfile?.copy(
              author =
                  lastSavedFullProfile!!
                      .author
                      .copy(
                          profilePicture = EditProfileNumberConstants.PROFILE_PIC_DEFAULT,
                          profilePictureUrl = EditProfileStrings.EMPTY_STRING))

      if (updatedProfile != null) {
        repository.updateProfile(updatedProfile)
        lastSavedFullProfile = updatedProfile
      }
      val newState =
          _uiState.value.copy(
              profilePicture = EditProfileNumberConstants.PROFILE_PIC_DEFAULT,
              profilePictureUri = null,
              profilePictureUrl = EditProfileStrings.EMPTY_STRING)
      updateChangesFlags(newState)
    }
  }
}
