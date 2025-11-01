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
import com.swentseekr.seekr.ui.theme.Green
import kotlinx.coroutines.launch

object SettingsScreenTestTags {
  const val LOGOUT_BUTTON = "logoutButton"
  const val APP_VERSION_TEXT = "appVersionText"
  const val BACK_BUTTON = "backButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onSignedOut: () -> Unit = {},
    onGoBack: () -> Unit = {}
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  val credentialManager = remember { CredentialManager.create(context) }
  val scope = rememberCoroutineScope()

  LaunchedEffect(uiState.signedOut) { if (uiState.signedOut) onSignedOut() }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Settings") },
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
                    containerColor = Green,
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
      modifier = modifier.padding(24.dp).fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()) {
              SettingsItem(
                  title = "App Version",
                  value = appVersion ?: "Unknown",
                  modifier = Modifier.testTag(SettingsScreenTestTags.APP_VERSION_TEXT))
            }

        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = 32.dp)
                    .testTag(SettingsScreenTestTags.LOGOUT_BUTTON)) {
              Text("Log out", color = MaterialTheme.colorScheme.onError)
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
