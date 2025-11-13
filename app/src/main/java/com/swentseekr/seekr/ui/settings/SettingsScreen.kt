package com.swentseekr.seekr.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.settings.SettingsScreenStrings.EDIT_PROFILE_TEXT
import kotlinx.coroutines.launch

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
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary))
      }) { padding ->
        SettingsContent(
            modifier = Modifier.padding(padding).fillMaxSize(),
            appVersion = uiState.appVersion,
            onEditProfileClick = onEditProfile,
            onLogoutClick = { scope.launch { viewModel.signOut(credentialManager) } })
      }
}

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    appVersion: String?,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
  Column(
      modifier = modifier.padding(SettingsScreenDefaults.ScreenPadding).fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Top section
        Column(
            verticalArrangement = Arrangement.spacedBy(SettingsScreenDefaults.ItemSpacing),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()) {
              SettingsItem(
                  title = SettingsScreenStrings.VersionLabel,
                  value = appVersion ?: SettingsScreenStrings.UnknownVersion,
                  modifier = Modifier.testTag(SettingsScreenTestTags.APP_VERSION_TEXT))

              androidx.compose.material3.Button(
                  onClick = onEditProfileClick,
                  modifier =
                      Modifier.fillMaxWidth().testTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON)) {
                    Text(EDIT_PROFILE_TEXT)
                  }
            }

        // Bottom section
        androidx.compose.material3.Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = SettingsScreenDefaults.LogoutTopPadding)
                    .testTag(SettingsScreenTestTags.LOGOUT_BUTTON)) {
              Text(SettingsScreenStrings.LogoutLabel, color = MaterialTheme.colorScheme.onError)
            }
      }
}

@Composable
fun SettingsItem(title: String, value: String, modifier: Modifier = Modifier) {
  Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, fontWeight = FontWeight.Medium)
        Text(text = value, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
}
