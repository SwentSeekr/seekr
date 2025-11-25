package com.swentseekr.seekr.ui.auth

import androidx.lifecycle.ViewModel
import com.swentseekr.seekr.model.authentication.OnboardingHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class OnboardingState(val step: Int = 1)

class OnboardingViewModel : ViewModel(), OnboardingHandler {

  private val _state = MutableStateFlow(OnboardingState())
  val state: StateFlow<OnboardingState> = _state

  fun nextStep() {
    _state.update { it.copy(step = it.step + 1) }
  }

  override fun completeOnboarding(userId: String, pseudonym: String, bio: String) {
    // call your real handler
  }
}
