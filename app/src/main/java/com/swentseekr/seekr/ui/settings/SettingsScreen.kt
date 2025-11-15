package com.swentseekr.seekr.ui.settings

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
import com.swentseekr.seekr.ui.settings.SettingsScreenDefaults.ColumnWeight
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

  LaunchedEffect(uiState.signedOut) { if (uiState.signedOut) onSignedOut() }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(SettingsScreenStrings.TopBarTitle) },
            navigationIcon = {
              IconButton(
                  onClick = onGoBack,
                  modifier = Modifier.testTag(SettingsScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = SettingsScreenStrings.BackContentDescription)
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
            appVersion = uiState.appVersion,
            onEditProfileClick = onEditProfile,
            onLogoutClick = { scope.launch { viewModel.signOut(credentialManager) } },
            uiState = uiState,
            onNotificationsChange = { viewModel.updateNotifications(it) },
            onPicturesChange = { viewModel.updatePictures(it) },
            onLocalisationChange = { viewModel.updateLocalisation(it) })
      }
}

/**
 * Composable that lays out all the settings content.
 *
 * @param modifier Optional [Modifier] for styling and layout adjustments.
 * @param appVersion The current app version to display.
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
    appVersion: String?,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    uiState: SettingsUIState = SettingsUIState(),
    onNotificationsChange: (Boolean) -> Unit = {},
    onPicturesChange: (Boolean) -> Unit = {},
    onLocalisationChange: (Boolean) -> Unit = {},
) {
  Column(modifier = modifier.fillMaxSize().padding(SettingsScreenDefaults.ScreenPadding)) {
    LazyColumn(modifier = Modifier.weight(ColumnWeight)) {
      item {
        SettingsItem(
            title = SettingsScreenStrings.VersionLabel,
            value = appVersion ?: SettingsScreenStrings.UnknownVersion,
            modifier = Modifier.testTag(SettingsScreenTestTags.APP_VERSION_TEXT))
      }

      item {
        SettingsToggleItem(
            title = SettingsScreenStrings.NotificationsLabel,
            checked = uiState.notificationsEnabled,
            onToggle = onNotificationsChange,
            modifier = Modifier.testTag(SettingsScreenTestTags.NOTIFICATIONS_TOGGLE))
      }

      item {
        SettingsToggleItem(
            title = SettingsScreenStrings.PicturesLabel,
            checked = uiState.picturesEnabled,
            onToggle = onPicturesChange,
            modifier = Modifier.testTag(SettingsScreenTestTags.PICTURES_TOGGLE))
      }

      item {
        SettingsToggleItem(
            title = SettingsScreenStrings.LocalisationLabel,
            checked = uiState.localisationEnabled,
            onToggle = onLocalisationChange,
            modifier = Modifier.testTag(SettingsScreenTestTags.LOCALISATION_TOGGLE))
      }

      item {
        SettingsArrowItem(
            title = SettingsScreenStrings.EditProfileLabel,
            onClick = onEditProfileClick,
            modifier = Modifier.testTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON))
      }
      item {
        SettingsArrowItem(
            title = SettingsScreenStrings.AppConditionLabel,
            onClick = { /* TODO */},
            modifier = Modifier.testTag(SettingsScreenTestTags.APP_CONDITION_BUTTON))
      }
    }

    Button(
        onClick = onLogoutClick,
        colors = ButtonDefaults.buttonColors(containerColor = LightError),
        modifier =
            Modifier.fillMaxWidth()
                .padding(top = SettingsScreenDefaults.LogoutTopPadding)
                .testTag(SettingsScreenTestTags.LOGOUT_BUTTON)) {
          Text(text = SettingsScreenStrings.LogoutLabel, color = LightOnError)
        }
  }
}
