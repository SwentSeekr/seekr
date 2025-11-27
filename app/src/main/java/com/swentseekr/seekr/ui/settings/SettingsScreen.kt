package com.swentseekr.seekr.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.settings.SettingsScreenDefaults.COLUMN_WEIGHT
import com.swentseekr.seekr.ui.theme.Green
import com.swentseekr.seekr.ui.theme.LightError
import com.swentseekr.seekr.ui.theme.LightOnError
import com.swentseekr.seekr.ui.theme.White
import kotlinx.coroutines.launch

/**
 * The main settings screen displaying app information and configurable settings.
 *
 * @param viewModel The [SettingsViewModel] to manage state and actions.
 * @param onSignedOut Callback invoked when the user signs out.
 * @param onGoBack Callback invoked when the back navigation is pressed.
 * @param onEditProfile Callback invoked when "Edit Profile" is clicked.
 * @param credentialManager [CredentialManager] used for sign-out functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onSignedOut: () -> Unit = {},
    onGoBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current)
) {
  val uiState by viewModel.uiState.collectAsState()
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  LaunchedEffect(Unit) { viewModel.refreshPermissions(context) }

  LaunchedEffect(uiState.signedOut) { if (uiState.signedOut) onSignedOut() }

  val notificationPermissionLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          isGranted ->
        viewModel.onNotificationPermissionResult(isGranted)
      }

  val galleryPermission =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
      } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
      }

  val galleryPermissionLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          isGranted ->
        viewModel.onGalleryPermissionResult(isGranted)
      }

  val locationPermissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted =
                result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            viewModel.onLocationPermissionResult(granted)
          }

  LaunchedEffect(uiState.requestNotificationPermission) {
    if (uiState.requestNotificationPermission) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      } else {
        viewModel.onNotificationPermissionResult(true)
      }
      viewModel.consumeNotificationPermissionRequest()
    }
  }

  LaunchedEffect(uiState.requestGalleryPermission) {
    if (uiState.requestGalleryPermission) {
      galleryPermissionLauncher.launch(galleryPermission)
      viewModel.consumeGalleryPermissionRequest()
    }
  }

  LaunchedEffect(uiState.requestLocationPermission) {
    if (uiState.requestLocationPermission) {
      locationPermissionLauncher.launch(
          arrayOf(
              Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
      viewModel.consumeLocationPermissionRequest()
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(SettingsScreenStrings.TOP_BAR_TITLE) },
            navigationIcon = {
              IconButton(
                  onClick = onGoBack,
                  modifier = Modifier.testTag(SettingsScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = SettingsScreenStrings.BACK_CONTENT_DESCRIPTION)
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Green,
                    titleContentColor = White,
                    navigationIconContentColor = White))
      }) { padding ->
        SettingsContent(
            modifier = Modifier.padding(padding).fillMaxSize(),
            onEditProfileClick = onEditProfile,
            onLogoutClick = { scope.launch { viewModel.signOut(credentialManager) } },
            uiState = uiState,
            onNotificationsChange = { viewModel.onNotificationsToggleRequested(it, context) },
            onPicturesChange = { viewModel.onPicturesToggleRequested(it, context) },
            onLocalisationChange = { viewModel.onLocalisationToggleRequested(it, context) })
      }
}

/**
 * Composable that lays out all the settings content.
 *
 * @param modifier Optional [Modifier] for styling and layout adjustments.
 * @param onEditProfileClick Callback triggered when the "Edit Profile" button is clicked.
 * @param onLogoutClick Callback triggered when the "Logout" button is clicked.
 * @param uiState Current state of settings UI.
 * @param onNotificationsChange Callback when notifications toggle changes.
 * @param onPicturesChange Callback when pictures toggle changes.
 * @param onLocalisationChange Callback when localisation toggle changes.
 */
@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    uiState: SettingsUIState,
    onNotificationsChange: (Boolean) -> Unit = {},
    onPicturesChange: (Boolean) -> Unit = {},
    onLocalisationChange: (Boolean) -> Unit = {},
) {
  Column(modifier = modifier.fillMaxSize().padding(SettingsScreenDefaults.SCREEN_PADDING)) {
    LazyColumn(modifier = Modifier.weight(COLUMN_WEIGHT)) {
      item {
        SettingsItem(
            title = SettingsScreenStrings.VERSION_LABEL,
            value = uiState.appVersion ?: SettingsScreenStrings.UNKNOWN_VERSION,
            modifier = Modifier.testTag(SettingsScreenTestTags.APP_VERSION_TEXT))
      }

      item {
        SettingsToggleItem(
            title = SettingsScreenStrings.NOTIFICATION_LABEL,
            checked = uiState.notificationsEnabled,
            onToggle = onNotificationsChange,
            modifier = Modifier.testTag(SettingsScreenTestTags.NOTIFICATIONS_TOGGLE))
      }

      item {
        SettingsToggleItem(
            title = SettingsScreenStrings.PICTURES_LABEL,
            checked = uiState.picturesEnabled,
            onToggle = onPicturesChange,
            modifier = Modifier.testTag(SettingsScreenTestTags.PICTURES_TOGGLE))
      }

      item {
        SettingsToggleItem(
            title = SettingsScreenStrings.LOCALISATION_LABEL,
            checked = uiState.localisationEnabled,
            onToggle = onLocalisationChange,
            modifier = Modifier.testTag(SettingsScreenTestTags.LOCALISATION_TOGGLE))
      }

      item {
        SettingsArrowItem(
            title = SettingsScreenStrings.EDIT_PROFILE_LABEL,
            onClick = onEditProfileClick,
            modifier = Modifier.testTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON))
      }
      item {
        SettingsArrowItem(
            title = SettingsScreenStrings.APP_CONDITION_LABEL,
            modifier = Modifier.testTag(SettingsScreenTestTags.APP_CONDITION_BUTTON))
      }
    }

    Button(
        onClick = onLogoutClick,
        colors = ButtonDefaults.buttonColors(containerColor = LightError),
        modifier =
            Modifier.fillMaxWidth()
                .padding(top = SettingsScreenDefaults.LOGOUT_TOP_PADDING)
                .testTag(SettingsScreenTestTags.LOGOUT_BUTTON)) {
          Text(text = SettingsScreenStrings.LOGOUT_LABEL, color = LightOnError)
        }
  }
}
