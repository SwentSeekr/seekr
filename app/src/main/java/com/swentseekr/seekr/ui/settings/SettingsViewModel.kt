package com.swentseekr.seekr.ui.settings

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swentseekr.seekr.BuildConfig
import com.swentseekr.seekr.model.authentication.AuthRepository
import com.swentseekr.seekr.model.authentication.AuthRepositoryFirebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUIState(
    val signedOut: Boolean = false,
    val errorMsg: String? = null,
    val appVersion: String? = null
)

class SettingsViewModel(private val authRepository: AuthRepository = AuthRepositoryFirebase()) :
    ViewModel() {

  private val _uiState = MutableStateFlow(SettingsUIState())
  val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

  init {
    displayAppVersion(BuildConfig.VERSION_NAME)
  }

  fun signOut(credentialManager: CredentialManager): Unit {
    viewModelScope.launch {
      authRepository
          .signOut()
          .fold(
              onSuccess = { _uiState.update { it.copy(signedOut = true) } },
              onFailure = { throwable ->
                _uiState.update { it.copy(errorMsg = throwable.localizedMessage) }
              })
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }

  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  // We could have used BuildConfig directly in the UI, but this way it's easier to test
  fun displayAppVersion(version: String) {
    _uiState.update { it.copy(appVersion = version) }
  }
}
