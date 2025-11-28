package com.swentseekr.seekr.ui.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swentseekr.seekr.BuildConfig
import com.swentseekr.seekr.model.authentication.AuthRepository
import com.swentseekr.seekr.model.authentication.AuthRepositoryFirebase
import com.swentseekr.seekr.model.notifications.NotificationHelper
import com.swentseekr.seekr.model.settings.SettingsRepositoryFirestore
import com.swentseekr.seekr.model.settings.SettingsRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUIState(
    val signedOut: Boolean = false,
    val errorMsg: String? = null,
    val appVersion: String? = null,
    val notificationsEnabled: Boolean = false,
    val picturesEnabled: Boolean = false,
    val localisationEnabled: Boolean = false
)

class SettingsViewModel(
    private val repository: SettingsRepositoryFirestore = SettingsRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryFirebase()
) : ViewModel() {

  private val _uiState = MutableStateFlow(SettingsUIState())
  val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()
  val settingsFlow = repository.settingsFlow

  init {
    setAppVersion(BuildConfig.VERSION_NAME)
    setAppVersion(BuildConfig.VERSION_NAME)
    viewModelScope.launch {
      try {
        repository.loadSettings()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
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
  fun setAppVersion(version: String) {
    _uiState.update { it.copy(appVersion = version) }
  }

  fun updateNotifications(enabled: Boolean, context: Context?) =
      viewModelScope.launch {
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          val activity = context as? Activity
          activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                SettingsScreenDefaults.NOTIFICATION_REQUEST_CODE)
          }
        }
        repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, enabled)

        // Send a test notification when enabled
        if (enabled && context != null) {
          NotificationHelper.sendNotification(
              context,
              SettingsScreenStrings.NOTIFICATION_FIELD_2,
              SettingsScreenStrings.NOTIFICATION_ACCEPT_MESSAGE)
        }
      }

  fun updatePictures(enabled: Boolean) =
      viewModelScope.launch {
        repository.updateField(SettingsScreenStrings.PICTURES_FIELD, enabled)
      }

  fun updateLocalisation(enabled: Boolean) =
      viewModelScope.launch {
        repository.updateField(SettingsScreenStrings.LOCALISATION_FIELD, enabled)
      }
}
