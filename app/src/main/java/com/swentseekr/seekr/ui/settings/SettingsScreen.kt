package com.swentseekr.seekr.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.model.notifications.NotificationHelper
import com.swentseekr.seekr.ui.settings.SettingsScreenDefaults.COLUMN_WEIGHT
import kotlinx.coroutines.launch

val UI_SET = SettingsScreenDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onSignedOut: () -> Unit = {},
    onGoBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onViewTerms: () -> Unit = {},
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current)
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  LaunchedEffect(uiState.signedOut) { if (uiState.signedOut) onSignedOut() }

  HandlePermissions(viewModel = viewModel)

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  SettingsScreenStrings.TOP_BAR_TITLE,
                  style = MaterialTheme.typography.headlineSmall)
            },
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface))
      }) { padding ->
        SettingsContent(
            modifier = Modifier.padding(padding).fillMaxSize(),
            onEditProfileClick = onEditProfile,
            onLogoutClick = { scope.launch { viewModel.signOut(credentialManager) } },
            uiState = uiState,
            onNotificationsChange = { viewModel.onNotificationsToggleRequested(it, context) },
            onPicturesChange = { viewModel.onPicturesToggleRequested(it, context) },
            onLocalisationChange = { viewModel.onLocalisationToggleRequested(it, context) },
            onViewTerms = onViewTerms)
      }
}

@Composable
private fun HandlePermissions(viewModel: SettingsViewModel) {
  val context = LocalContext.current

  val notificationPermissionLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          isGranted ->
        viewModel.onNotificationPermissionResult(isGranted)

        if (isGranted) {
          NotificationHelper.sendNotification(
              context,
              SettingsScreenStrings.NOTIFICATION_FIELD_2,
              SettingsScreenStrings.NOTIFICATION_ACCEPT_MESSAGE)
        }
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

  LaunchedEffect(Unit) { viewModel.refreshPermissions(context) }

  LaunchedEffect(Unit) {
    viewModel.permissionEvents.collect { event ->
      when (event) {
        PermissionEvent.RequestNotification -> {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          } else {
            viewModel.onNotificationPermissionResult(true)
            NotificationHelper.sendNotification(
                context,
                SettingsScreenStrings.NOTIFICATION_FIELD_2,
                SettingsScreenStrings.NOTIFICATION_ACCEPT_MESSAGE)
          }
        }
        PermissionEvent.RequestGallery -> {
          galleryPermissionLauncher.launch(galleryPermission)
        }
        PermissionEvent.RequestLocation -> {
          locationPermissionLauncher.launch(
              arrayOf(
                  Manifest.permission.ACCESS_FINE_LOCATION,
                  Manifest.permission.ACCESS_COARSE_LOCATION))
        }
      }
    }
  }
}

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    uiState: SettingsUIState,
    onNotificationsChange: (Boolean) -> Unit = {},
    onPicturesChange: (Boolean) -> Unit = {},
    onLocalisationChange: (Boolean) -> Unit = {},
    onViewTerms: () -> Unit = {}
) {
  Column(
      modifier =
          modifier
              .fillMaxSize()
              .background(
                  Brush.verticalGradient(
                      colors =
                          listOf(
                              MaterialTheme.colorScheme.background,
                              MaterialTheme.colorScheme.surfaceVariant.copy(
                                  alpha = UI_SET.ALPHA_CHANGE))))
              .padding(UI_SET.PADDING_MID)) {
        LazyColumn(modifier = Modifier.weight(COLUMN_WEIGHT)) {
          item {
            Text(
                "App Info",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier.padding(
                        vertical = UI_SET.PADDING_TINY, horizontal = UI_SET.PADDING_MINI))
          }

          item {
            SettingsItem(
                title = SettingsScreenStrings.VERSION_LABEL,
                value = uiState.appVersion ?: SettingsScreenStrings.UNKNOWN_VERSION,
                modifier = Modifier.testTag(SettingsScreenTestTags.APP_VERSION_TEXT))
          }

          item { Spacer(modifier = Modifier.height(UI_SET.SPACER_HEIGHT)) }

          item {
            Text(
                "Permissions",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier.padding(
                        vertical = UI_SET.PADDING_TINY, horizontal = UI_SET.PADDING_MINI))
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

          item { Spacer(modifier = Modifier.height(UI_SET.SPACER_HEIGHT)) }

          item {
            Text(
                "Account",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier.padding(
                        vertical = UI_SET.PADDING_TINY, horizontal = UI_SET.PADDING_MINI))
          }

          item {
            SettingsArrowItem(
                title = SettingsScreenStrings.EDIT_PROFILE_LABEL,
                onClick = onEditProfileClick,
                modifier = Modifier.testTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON))
          }

          item { Spacer(modifier = Modifier.height(UI_SET.SPACER_HEIGHT_SMALL)) }

          item {
            Text(
                "Legal",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier.padding(
                        vertical = UI_SET.PADDING_TINY, horizontal = UI_SET.PADDING_MINI))
          }

          item {
            SettingsArrowItem(
                title = SettingsScreenStrings.APP_CONDITION_LABEL,
                onClick = onViewTerms,
                modifier = Modifier.testTag(SettingsScreenTestTags.APP_CONDITION_BUTTON))
          }
        }

        Spacer(modifier = Modifier.height(UI_SET.SPACER_HEIGHT_SMALL))

        Button(
            onClick = onLogoutClick,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer),
            shape = RoundedCornerShape(UI_SET.ROUND_CORNER),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = UI_SET.ELEVATION),
            modifier =
                Modifier.fillMaxWidth()
                    .height(UI_SET.BUTTON_HEIGHT)
                    .testTag(SettingsScreenTestTags.LOGOUT_BUTTON)) {
              Icon(
                  imageVector = Icons.Filled.AccountCircle,
                  contentDescription = null,
                  modifier = Modifier.padding(end = UI_SET.PADDING_TINY))
              Text(
                  text = SettingsScreenStrings.LOGOUT_LABEL,
                  style = MaterialTheme.typography.titleMedium)
            }
      }
}
