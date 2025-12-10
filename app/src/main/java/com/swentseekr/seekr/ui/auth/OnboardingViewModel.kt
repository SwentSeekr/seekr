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

  fun nextStep() {
    _state.update { it.copy(step = it.step + 1) }
  }

  fun previousStep() {
    _state.update { it.copy(step = it.step - 1) }
  }

  fun validateTerms() {
    _state.update { it.copy(step = 4) }
  }

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

  fun clearPseudonymError() {
    _state.value = _state.value.copy(pseudonymError = null)
  }

  override fun completeOnboarding(userId: String, pseudonym: String, bio: String) {
    // call your real handler
  }
}
