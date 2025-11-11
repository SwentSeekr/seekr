package com.swentseekr.seekr.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

object SettingsScreenTestTags {
  const val LOGOUT_BUTTON = "logoutButton"
  const val APP_VERSION_TEXT = "appVersionText"
  const val BACK_BUTTON = "backButton"
}

// text constants
const val TOP_BAR_TEXT = "Settings"
const val VERSION_TEXT = "App Version"
const val UNKNOWN_VERSION_TEXT = "Unknown"
const val LOGOUT_TEXT = "Log out"

// layout constants
private val SCREEN_PADDING = 24.dp
private val ITEM_SPACING = 16.dp
private val LOGOUT_TOP_PADDING = 32.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onSignedOut: () -> Unit = {},
    onGoBack: () -> Unit = {},
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current)
) {
  val uiState by viewModel.uiState.collectAsState()
  val scope = rememberCoroutineScope()

  LaunchedEffect(uiState.signedOut) { if (uiState.signedOut) onSignedOut() }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(TOP_BAR_TEXT) },
            navigationIcon = {
              IconButton(
                  onClick = onGoBack,
                  modifier = Modifier.testTag(SettingsScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back")
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
            onLogoutClick = { scope.launch { viewModel.signOut(credentialManager) } })
      }
}

@Composable
fun SettingsContent(modifier: Modifier = Modifier, appVersion: String?, onLogoutClick: () -> Unit) {
  Column(
      modifier = modifier.padding(SCREEN_PADDING).fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ITEM_SPACING),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()) {
              SettingsItem(
                  title = VERSION_TEXT,
                  value = appVersion ?: UNKNOWN_VERSION_TEXT,
                  modifier = Modifier.testTag(SettingsScreenTestTags.APP_VERSION_TEXT))
            }

        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = LOGOUT_TOP_PADDING)
                    .testTag(SettingsScreenTestTags.LOGOUT_BUTTON)) {
              Text(LOGOUT_TEXT, color = MaterialTheme.colorScheme.onError)
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
