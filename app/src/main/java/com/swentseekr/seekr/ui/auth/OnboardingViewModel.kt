package com.swentseekr.seekr.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swentseekr.seekr.model.authentication.OnboardingHandler
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.model.profile.ProfileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val STEP = 1
const val TOTAL_STEPS = 4

data class OnboardingState(
    val step: Int = 1,
    val showingFullTerms: Boolean = false,
    val pseudonymError: String? = null,
    val isCheckingPseudonym: Boolean = false
)

class OnboardingViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel(), OnboardingHandler {

  private val _state = MutableStateFlow(OnboardingState())
  val state: StateFlow<OnboardingState> = _state

  /**
   * Advances the onboarding flow to the next step.
   *
   * Increments the current step index by 1.
   */
  fun nextStep() {
    _state.update { it.copy(step = it.step + STEP) }
  }

  /**
   * Moves the onboarding flow back to the previous step.
   *
   * Decrements the current step index by 1.
   */
  fun previousStep() {
    _state.update { it.copy(step = it.step - STEP) }
  }

  /**
   * Skips to the final onboarding step (step 4) after terms acceptance.
   *
   * Called when user accepts terms and conditions — bypasses intermediate steps.
   */
  fun validateTerms() {
    _state.update { it.copy(step = TOTAL_STEPS) }
  }

  /**
   * Validates the user’s chosen pseudonym locally and triggers availability check if valid.
   *
   * @param pseudonym The pseudonym entered by the user.
   * - If invalid (format or empty), sets an error message.
   * - If valid and non-empty, triggers async availability check via `checkPseudonymAvailability`.
   */
  fun validatePseudonym(pseudonym: String) {
    _state.value =
        _state.value.copy(
            pseudonymError =
                when {
                  ProfileUtils().isValidPseudonym(pseudonym) -> null
                  else -> OnboardingFlowStrings.ERROR_PSEUDONYM_INVALID
                })

    if (_state.value.pseudonymError == null && pseudonym.isNotBlank()) {
      checkPseudonymAvailability(pseudonym)
    }
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
      _state.value = _state.value.copy(isCheckingPseudonym = true)

      try {
        val isAvailable = pseudonym !in profileRepository.getAllPseudonyms()

        _state.value =
            _state.value.copy(
                pseudonymError =
                    if (!isAvailable) {
                      OnboardingFlowStrings.ERROR_PSEUDONYM_TAKEN
                    } else {
                      null
                    },
                isCheckingPseudonym = false)
      } catch (e: Exception) {
        _state.value = _state.value.copy(pseudonymError = null, isCheckingPseudonym = false)
      }
    }
  }

  /** Clears any existing pseudonym error message. */
  fun clearPseudonymError() {
    _state.value = _state.value.copy(pseudonymError = null)
  }

  /**
   * Finalizes the onboarding process with user data.
   *
   * @param userId Unique identifier of the user.
   * @param pseudonym Final validated pseudonym.
   * @param bio User’s bio text.
   */
  override fun completeOnboarding(userId: String, pseudonym: String, bio: String) {
    // call your real handler
  }
}
