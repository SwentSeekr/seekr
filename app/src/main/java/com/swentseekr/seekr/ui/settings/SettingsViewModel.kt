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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Settings screen.
 *
 * Represents the complete, immutable state consumed by the Settings UI. Updated by the
 * SettingsViewModel in response to user actions and permission changes.
 *
 * @property signedOut Indicates whether the user has been signed out.
 * @property errorMsg Optional error message displayed to the user.
 * @property appVersion Application version string displayed in settings.
 * @property notificationsEnabled Whether in-app notifications are enabled.
 * @property picturesEnabled Whether picture-related features are enabled.
 * @property localisationEnabled Whether location-based features are enabled.
 * @property notificationPermissionGranted Whether notification permission is granted.
 * @property galleryPermissionGranted Whether gallery/storage permission is granted.
 * @property locationPermissionGranted Whether location permission is granted.
 */
data class SettingsUIState(
    val signedOut: Boolean = false,
    val errorMsg: String? = null,
    val appVersion: String? = null,
    val notificationsEnabled: Boolean = false,
    val picturesEnabled: Boolean = false,
    val localisationEnabled: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val galleryPermissionGranted: Boolean = false,
    val locationPermissionGranted: Boolean = false,
)

sealed interface PermissionEvent {
  object RequestNotification : PermissionEvent

  object RequestGallery : PermissionEvent

  object RequestLocation : PermissionEvent
}

class SettingsViewModel(private val authRepository: AuthRepository = AuthRepositoryFirebase()) :
    ViewModel() {

  private val _permissionEvents = MutableSharedFlow<PermissionEvent>()
  val permissionEvents: SharedFlow<PermissionEvent> = _permissionEvents

  private val _uiState = MutableStateFlow(SettingsUIState())
  val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

  init {
    setAppVersion(BuildConfig.VERSION_NAME)
  }

  /**
   * Refreshes the current permission and feature states.
   *
   * Checks system-level and runtime permissions for:
   * - Notifications
   * - Gallery access
   * - Location access
   *
   * Updates the UI state accordingly to reflect both permission status and whether related features
   * should be considered enabled.
   *
   * @param context Application context used to check permissions.
   */
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

  /**
   * Handles user interaction with the notifications toggle.
   *
   * Behavior:
   * - If enabling notifications and permission is missing, emits a permission request event
   * - Otherwise, redirects the user to the system app settings
   *
   * @param enabled Whether the user is attempting to enable notifications.
   * @param context Application context used to open system settings.
   */
  fun onNotificationsToggleRequested(enabled: Boolean, context: Context) {
    val state = uiState.value
    if (enabled) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
          !state.notificationPermissionGranted) {
        viewModelScope.launch { _permissionEvents.emit(PermissionEvent.RequestNotification) }
      } else {
        openAppSettings(context)
      }
    } else {
      openAppSettings(context)
    }
  }

  /**
   * Processes the result of the notification permission request.
   *
   * Updates permission and feature state depending on:
   * - Android version
   * - Whether the permission was granted
   *
   * Automatically enables notifications if permission is effectively granted.
   *
   * @param granted True if the permission was granted by the user.
   */
  fun onNotificationPermissionResult(granted: Boolean) {
    val effectiveGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || granted

    _uiState.update {
      it.copy(
          notificationPermissionGranted = effectiveGranted,
          notificationsEnabled = effectiveGranted || it.notificationsEnabled)
    }

    if (effectiveGranted) {
      updateNotifications(true)
    }
  }

  /**
   * Updates the notifications enabled state.
   *
   * Internal helper used to synchronize the UI state after permission changes.
   *
   * @param enabled Whether notifications should be considered enabled.
   */
  private fun updateNotifications(enabled: Boolean) {
    _uiState.update { it.copy(notificationsEnabled = enabled) }
  }

  /**
   * Handles user interaction with the pictures/gallery toggle.
   *
   * Behavior:
   * - If enabling without permission, emits a gallery permission request
   * - Otherwise, redirects the user to the system app settings
   *
   * @param enabled Whether the user is attempting to enable picture access.
   * @param context Application context used to open system settings.
   */
  fun onPicturesToggleRequested(enabled: Boolean, context: Context) {
    val state = uiState.value
    if (enabled && !state.galleryPermissionGranted) {
      viewModelScope.launch { _permissionEvents.emit(PermissionEvent.RequestGallery) }
    } else {
      openAppSettings(context)
    }
  }

  /**
   * Processes the result of the gallery permission request.
   *
   * Updates both permission and feature state. Automatically enables picture access if permission
   * is granted.
   *
   * @param granted True if the gallery permission was granted.
   */
  fun onGalleryPermissionResult(granted: Boolean) {
    _uiState.update {
      it.copy(galleryPermissionGranted = granted, picturesEnabled = it.picturesEnabled || granted)
    }
    if (granted) {
      updatePictures(true)
    }
  }

  /**
   * Updates the pictures enabled state.
   *
   * Internal helper used to synchronize the UI state after permission changes.
   *
   * @param enabled Whether picture access should be considered enabled.
   */
  private fun updatePictures(enabled: Boolean) {
    _uiState.update { it.copy(picturesEnabled = enabled) }
  }

  /**
   * Handles user interaction with the localisation/location toggle.
   *
   * Behavior:
   * - If enabling without permission, emits a location permission request
   * - Otherwise, redirects the user to the system app settings
   *
   * @param enabled Whether the user is attempting to enable localisation.
   * @param context Application context used to open system settings.
   */
  fun onLocalisationToggleRequested(enabled: Boolean, context: Context) {
    val state = uiState.value
    if (enabled && !state.locationPermissionGranted) {
      viewModelScope.launch { _permissionEvents.emit(PermissionEvent.RequestLocation) }
    } else {
      openAppSettings(context)
    }
  }

  /**
   * Processes the result of the location permission request.
   *
   * Updates both permission and feature state. Automatically enables localisation if permission is
   * granted.
   *
   * @param granted True if the location permission was granted.
   */
  fun onLocationPermissionResult(granted: Boolean) {
    _uiState.update {
      it.copy(
          locationPermissionGranted = granted,
          localisationEnabled = it.localisationEnabled || granted)
    }
    if (granted) {
      updateLocalisation(true)
    }
  }

  /**
   * Updates the localisation enabled state.
   *
   * Internal helper used to synchronize the UI state after permission changes.
   *
   * @param enabled Whether localisation should be considered enabled.
   */
  private fun updateLocalisation(enabled: Boolean) {
    _uiState.update { it.copy(localisationEnabled = enabled) }
  }

  /**
   * Signs the user out of the application.
   *
   * Behavior:
   * - Calls the authentication repository sign-out logic
   * - Clears stored credentials using the Credential Manager
   * - Updates UI state to reflect success or error
   *
   * @param credentialManager Credential manager used to clear stored credentials.
   */
  fun signOut(credentialManager: CredentialManager) {
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

  /**
   * Clears the current error message from the UI state.
   *
   * Typically called after the error has been displayed to the user.
   */
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  /**
   * Sets the application version displayed in the settings screen.
   *
   * @param version Version name of the application.
   */
  fun setAppVersion(version: String) {
    _uiState.update { it.copy(appVersion = version) }
  }

  /**
   * Opens the system settings screen for the current application.
   *
   * Allows the user to manually manage permissions and system-level settings.
   *
   * @param context Application context used to start the settings activity.
   */
  private fun openAppSettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.fromParts(SettingsScreenStrings.PACKAGE, context.packageName, null)
        }
    context.startActivity(intent)
  }
}
