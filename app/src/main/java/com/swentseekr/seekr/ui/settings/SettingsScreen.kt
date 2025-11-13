package com.swentseekr.seekr.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.profile.EditProfileConstants.SCREEN_PADDING
import kotlinx.coroutines.launch
import com.swentseekr.seekr.ui.settings.SettingsScreenDefaults
import com.swentseekr.seekr.ui.settings.SettingsScreenStrings.EDIT_PROFILE_TEXT
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
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.typography.headlineSmall.color,
                    navigationIconContentColor = MaterialTheme.typography.headlineSmall.color))
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
) {  Column(
      modifier = modifier.padding(SettingsScreenDefaults.ScreenPadding).fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally) {
    Column(
        modifier = modifier.padding(SCREEN_PADDING).fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Column(
            verticalArrangement = Arrangement.spacedBy(SettingsScreenDefaults.ItemSpacing),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()) {
              SettingsItem(
                  title = SettingsScreenStrings.VersionLabel,
                  value = appVersion ?: SettingsScreenStrings.UnknownVersion,
                  modifier = Modifier.testTag(SettingsScreenTestTags.APP_VERSION_TEXT))
            Button(
                onClick = onEditProfileClick,
                modifier = Modifier.fillMaxWidth().testTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON)
            ) {
                Text(EDIT_PROFILE_TEXT)
            }

        Button(
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
        verticalAlignment = Alignment.CenterVertically)
    {
        Text(text = title, fontWeight = FontWeight.Medium)
        Text(text = value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}