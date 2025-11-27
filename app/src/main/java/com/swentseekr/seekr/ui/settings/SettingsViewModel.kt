package com.swentseekr.seekr.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
    val appVersion: String? = null,
    val notificationsEnabled: Boolean = false,
    val picturesEnabled: Boolean = false,
    val localisationEnabled: Boolean = false,

    // Permissions
    val notificationPermissionGranted: Boolean = false,
    val galleryPermissionGranted: Boolean = false,
    val locationPermissionGranted: Boolean = false,

    // Permission requests
    val requestNotificationPermission: Boolean = false,
    val requestGalleryPermission: Boolean = false,
    val requestLocationPermission: Boolean = false
)

class SettingsViewModel(private val authRepository: AuthRepository = AuthRepositoryFirebase()) :
    ViewModel() {

  private val _uiState = MutableStateFlow(SettingsUIState())
  val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

  init {
    setAppVersion(BuildConfig.VERSION_NAME)
  }

  fun refreshPermissions(context: Context) {

    val notificationsAllowedBySystem =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    val postNotificationsGranted =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
              PackageManager.PERMISSION_GRANTED
        } else {
          true
        }

    val notificationGranted = notificationsAllowedBySystem && postNotificationsGranted

    val galleryPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          Manifest.permission.READ_MEDIA_IMAGES
        } else {
          Manifest.permission.READ_EXTERNAL_STORAGE
        }

    val galleryGranted =
        ContextCompat.checkSelfPermission(context, galleryPermission) ==
            PackageManager.PERMISSION_GRANTED

    val fineGranted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    val coarseGranted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    val locationGranted = fineGranted || coarseGranted

    _uiState.update {
      it.copy(
          notificationPermissionGranted = notificationGranted,
          galleryPermissionGranted = galleryGranted,
          locationPermissionGranted = locationGranted,
          notificationsEnabled = notificationGranted,
          picturesEnabled = galleryGranted,
          localisationEnabled = locationGranted)
    }
  }

  fun onNotificationsToggleRequested(enabled: Boolean, context: Context) {
    val state = uiState.value
    if (enabled) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
          !state.notificationPermissionGranted) {
        _uiState.update { it.copy(requestNotificationPermission = true) }
      } else {
        openAppSettings(context)
      }
    } else {
      openAppSettings(context)
    }
  }

  fun consumeNotificationPermissionRequest() {
    _uiState.update { it.copy(requestNotificationPermission = false) }
  }

  fun onNotificationPermissionResult(granted: Boolean) {
    val effectiveGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || granted

    _uiState.update {
      it.copy(
          notificationPermissionGranted = effectiveGranted,
          requestNotificationPermission = false,
          notificationsEnabled = effectiveGranted || it.notificationsEnabled)
    }

    if (effectiveGranted) {
      updateNotifications(true)
    }
  }

  private fun updateNotifications(enabled: Boolean) {
    _uiState.update { it.copy(notificationsEnabled = enabled) }
  }

  fun onPicturesToggleRequested(enabled: Boolean, context: Context) {
    val state = uiState.value
    if (enabled && !state.galleryPermissionGranted) {
      _uiState.update { it.copy(requestGalleryPermission = true) }
    } else {
      openAppSettings(context)
    }
  }

  fun consumeGalleryPermissionRequest() {
    _uiState.update { it.copy(requestGalleryPermission = false) }
  }

  fun onGalleryPermissionResult(granted: Boolean) {
    _uiState.update {
      it.copy(
          galleryPermissionGranted = granted,
          requestGalleryPermission = false,
          picturesEnabled = it.picturesEnabled || granted)
    }
    if (granted) {
      updatePictures(true)
    }
  }

  private fun updatePictures(enabled: Boolean) {
    _uiState.update { it.copy(picturesEnabled = enabled) }
  }

  fun onLocalisationToggleRequested(enabled: Boolean, context: Context) {
    val state = uiState.value
    if (enabled && !state.locationPermissionGranted) {
      _uiState.update { it.copy(requestLocationPermission = true) }
    } else {
      openAppSettings(context)
    }
  }

  fun consumeLocationPermissionRequest() {
    _uiState.update { it.copy(requestLocationPermission = false) }
  }

  fun onLocationPermissionResult(granted: Boolean) {
    _uiState.update {
      it.copy(
          locationPermissionGranted = granted,
          requestLocationPermission = false,
          localisationEnabled = it.localisationEnabled || granted)
    }
    if (granted) {
      updateLocalisation(true)
    }
  }

  private fun updateLocalisation(enabled: Boolean) {
    _uiState.update { it.copy(localisationEnabled = enabled) }
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

  fun setAppVersion(version: String) {
    _uiState.update { it.copy(appVersion = version) }
  }

  private fun openAppSettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.fromParts("package", context.packageName, null)
        }
    context.startActivity(intent)
  }
}
