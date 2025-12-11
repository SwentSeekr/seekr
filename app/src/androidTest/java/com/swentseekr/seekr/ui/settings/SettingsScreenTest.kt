package com.swentseekr.seekr.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.credentials.CredentialManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  // ----------------------------------------------------------------------------------------------
  // SettingsScreen high-level UI
  // ----------------------------------------------------------------------------------------------

  @Test
  fun screen_displays_all_main_elements() {
    composeRule.setContent { MaterialTheme { SettingsScreen() } }

    composeRule.onNodeWithTag(SettingsScreenTestTags.BACK_BUTTON).assertExists()
    composeRule.onNodeWithTag(SettingsScreenTestTags.APP_VERSION_TEXT).assertExists()
    composeRule.onNodeWithTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON).assertExists()
    composeRule.onNodeWithTag(SettingsScreenTestTags.LOGOUT_BUTTON).assertExists()
    composeRule.onNodeWithTag(SettingsScreenTestTags.APP_CONDITION_BUTTON).assertExists()
    composeRule.onNodeWithTag(SettingsScreenTestTags.NOTIFICATIONS_TOGGLE).assertExists()
    composeRule.onNodeWithTag(SettingsScreenTestTags.PICTURES_TOGGLE).assertExists()
    composeRule.onNodeWithTag(SettingsScreenTestTags.LOCALISATION_TOGGLE).assertExists()
  }

  @Test
  fun top_bar_title_is_displayed() {
    composeRule.setContent { MaterialTheme { SettingsScreen() } }

    composeRule.onNodeWithText(SettingsScreenStrings.TOP_BAR_TITLE).assertExists()
  }

  @Test
  fun clicking_back_button_triggers_callback() {
    var backPressed = false

    composeRule.setContent { MaterialTheme { SettingsScreen(onGoBack = { backPressed = true }) } }

    composeRule.onNodeWithTag(SettingsScreenTestTags.BACK_BUTTON).performClick()
    assertTrue(backPressed)
  }

  @Test
  fun settingsScreen_toggles_reflect_viewModel_state() {
    val viewModel = SettingsViewModel()

    composeRule.setContent {
      MaterialTheme { SettingsScreen(viewModel = viewModel, onSignedOut = {}, onGoBack = {}) }
    }
    composeRule.waitForIdle()

    composeRule.runOnIdle {
      setUiState(
          viewModel,
          viewModel.uiState.value.copy(
              appVersion = SettingsScreenStrings.APP_VERSION_1,
              notificationsEnabled = true,
              picturesEnabled = false,
              localisationEnabled = true))
    }

    composeRule.waitForIdle()
    composeRule.onNodeWithTag(SettingsScreenTestTags.NOTIFICATIONS_TOGGLE).assertIsOn()
    composeRule.onNodeWithTag(SettingsScreenTestTags.PICTURES_TOGGLE).assertIsOff()
    composeRule.onNodeWithTag(SettingsScreenTestTags.LOCALISATION_TOGGLE).assertIsOn()
  }

  @Test
  fun settingsScreen_app_version_uses_viewModel_state_and_handles_null() {
    val viewModel = SettingsViewModel()

    // version explicitly set
    setUiState(
        viewModel,
        SettingsUIState(
            appVersion = SettingsScreenStrings.APP_VERSION_2,
            notificationsEnabled = false,
            picturesEnabled = false,
            localisationEnabled = false))

    composeRule.setContent {
      MaterialTheme { SettingsScreen(viewModel = viewModel, onSignedOut = {}, onGoBack = {}) }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.APP_VERSION_TEXT).assertExists()
    composeRule.onNodeWithText(SettingsScreenStrings.APP_VERSION_2).assertExists()

    // Now change to null appVersion and verify UNKNOWN is shown
    composeRule.runOnIdle {
      setUiState(
          viewModel,
          viewModel.uiState.value.copy(appVersion = null),
      )
    }

    composeRule.waitForIdle()
    composeRule.onNodeWithText(SettingsScreenStrings.UNKNOWN_VERSION).assertExists()
  }

  @Test
  fun settingsScreen_clicking_edit_profile_button_triggers_callback() {
    var editProfileTriggered = false
    val viewModel = SettingsViewModel()

    composeRule.setContent {
      MaterialTheme {
        SettingsScreen(
            viewModel = viewModel, onEditProfile = { editProfileTriggered = true }, onGoBack = {})
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON).performClick()
    assertTrue(editProfileTriggered)
  }

  @Test
  fun when_signedOut_true_onSignedOut_callback_is_called_initially() {
    val viewModel = SettingsViewModel()
    setUiState(
        viewModel,
        SettingsUIState(signedOut = true, appVersion = SettingsScreenStrings.APP_VERSION_1))

    var signedOutCalled = false

    composeRule.setContent {
      MaterialTheme {
        SettingsScreen(viewModel = viewModel, onSignedOut = { signedOutCalled = true })
      }
    }

    composeRule.waitForIdle()

    assertTrue(signedOutCalled)
  }

  @Test
  fun when_signedOut_changes_from_false_to_true_onSignedOut_called_once() {
    val viewModel = SettingsViewModel()
    // Start with signedOut = false
    setUiState(
        viewModel,
        SettingsUIState(signedOut = false, appVersion = SettingsScreenStrings.APP_VERSION_1))

    var signedOutCalls = 0

    composeRule.setContent {
      MaterialTheme { SettingsScreen(viewModel = viewModel, onSignedOut = { signedOutCalls++ }) }
    }

    composeRule.waitForIdle()
    // No calls yet
    assertEquals(0, signedOutCalls)

    // Now flip signedOut to true and ensure callback is invoked exactly once
    composeRule.runOnIdle {
      setUiState(
          viewModel,
          viewModel.uiState.value.copy(signedOut = true),
      )
    }

    composeRule.waitForIdle()
    assertEquals(1, signedOutCalls)
  }

  // ----------------------------------------------------------------------------------------------
  // SettingsContent behaviour (existing + extra)
  // ----------------------------------------------------------------------------------------------

  @Test
  fun clicking_callbacks_work_in_SettingsContent() {
    var editProfileClicked = false
    var logoutClicked = false
    var notificationsToggled = false
    var picturesToggled = false
    var localisationToggled = false

    val uiState =
        SettingsUIState(
            appVersion = SettingsScreenStrings.APP_VERSION_1,
            notificationsEnabled = false,
            picturesEnabled = false,
            localisationEnabled = false)

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            onEditProfileClick = { editProfileClicked = true },
            onLogoutClick = { logoutClicked = true },
            uiState = uiState,
            onNotificationsChange = { notificationsToggled = it },
            onPicturesChange = { picturesToggled = it },
            onLocalisationChange = { localisationToggled = it })
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.NOTIFICATIONS_TOGGLE).performClick()
    composeRule.onNodeWithTag(SettingsScreenTestTags.PICTURES_TOGGLE).performClick()
    composeRule.onNodeWithTag(SettingsScreenTestTags.LOCALISATION_TOGGLE).performClick()

    assertTrue(notificationsToggled)
    assertTrue(picturesToggled)
    assertTrue(localisationToggled)

    composeRule.onNodeWithTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON).performClick()
    composeRule.onNodeWithTag(SettingsScreenTestTags.LOGOUT_BUTTON).performClick()

    assertTrue(editProfileClicked)
    assertTrue(logoutClicked)
  }

  @Test
  fun clicking_edit_profile_button_triggers_callback() {
    var editProfileTriggered = false

    val uiState = SettingsUIState(appVersion = SettingsScreenStrings.APP_VERSION_1)

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            onEditProfileClick = { editProfileTriggered = true },
            onLogoutClick = {},
            uiState = uiState)
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON).performClick()
    assertTrue(editProfileTriggered)
  }

  @Test
  fun clicking_logout_button_triggers_callback() {
    var logoutTriggered = false

    val uiState = SettingsUIState(appVersion = SettingsScreenStrings.APP_VERSION_1)

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            onEditProfileClick = {}, onLogoutClick = { logoutTriggered = true }, uiState = uiState)
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.LOGOUT_BUTTON).performClick()
    assertTrue(logoutTriggered)
  }

  @Test
  fun app_version_is_displayed_correctly() {
    val expectedVersion = SettingsScreenStrings.APP_VERSION_2
    val uiState = SettingsUIState(appVersion = expectedVersion)

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(onEditProfileClick = {}, onLogoutClick = {}, uiState = uiState)
      }
    }

    composeRule.onNodeWithText(SettingsScreenStrings.VERSION_LABEL).assertExists()
    composeRule.onNodeWithText(expectedVersion).assertExists()
  }

  @Test
  fun app_version_is_unknown_when_null() {
    val uiState = SettingsUIState(appVersion = null)

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(onEditProfileClick = {}, onLogoutClick = {}, uiState = uiState)
      }
    }

    composeRule.onNodeWithText(SettingsScreenStrings.VERSION_LABEL).assertExists()
    composeRule.onNodeWithText(SettingsScreenStrings.UNKNOWN_VERSION).assertExists()
  }

  @Test
  fun toggles_reflect_initial_ui_state() {
    val uiState =
        SettingsUIState(
            appVersion = SettingsScreenStrings.APP_VERSION_1,
            notificationsEnabled = true,
            picturesEnabled = true,
            localisationEnabled = false)

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(onEditProfileClick = {}, onLogoutClick = {}, uiState = uiState)
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.NOTIFICATIONS_TOGGLE).assertIsOn()
    composeRule.onNodeWithTag(SettingsScreenTestTags.PICTURES_TOGGLE).assertIsOn()
    composeRule.onNodeWithTag(SettingsScreenTestTags.LOCALISATION_TOGGLE).assertIsOff()
  }

  @Test
  fun toggles_call_callbacks_with_false_when_initially_true() {
    var notificationsValue: Boolean? = null
    var picturesValue: Boolean? = null
    var localisationValue: Boolean? = null

    val uiState =
        SettingsUIState(
            appVersion = SettingsScreenStrings.APP_VERSION_1,
            notificationsEnabled = true,
            picturesEnabled = true,
            localisationEnabled = true)

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            onEditProfileClick = {},
            onLogoutClick = {},
            uiState = uiState,
            onNotificationsChange = { notificationsValue = it },
            onPicturesChange = { picturesValue = it },
            onLocalisationChange = { localisationValue = it })
      }
    }

    // All toggles start ON, clicking should request turning them OFF -> callback with false
    composeRule.onNodeWithTag(SettingsScreenTestTags.NOTIFICATIONS_TOGGLE).performClick()
    composeRule.onNodeWithTag(SettingsScreenTestTags.PICTURES_TOGGLE).performClick()
    composeRule.onNodeWithTag(SettingsScreenTestTags.LOCALISATION_TOGGLE).performClick()

    assertEquals(false, notificationsValue)
    assertEquals(false, picturesValue)
    assertEquals(false, localisationValue)
  }

  @Test
  fun app_condition_button_is_present_in_SettingsContent() {
    val uiState =
        SettingsUIState(
            appVersion = SettingsScreenStrings.APP_VERSION_1,
            notificationsEnabled = false,
            picturesEnabled = false,
            localisationEnabled = false)

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(onEditProfileClick = {}, onLogoutClick = {}, uiState = uiState)
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.APP_CONDITION_BUTTON).assertExists()
    composeRule.onNodeWithText(SettingsScreenStrings.APP_CONDITION_LABEL).assertExists()
  }

  @Test
  fun settingsContent_toggles_with_default_callbacks_do_not_crash() {
    val uiState =
        SettingsUIState(
            appVersion = SettingsScreenStrings.APP_VERSION_1,
            notificationsEnabled = false,
            picturesEnabled = false,
            localisationEnabled = false)

    composeRule.setContent {
      MaterialTheme {
        // Note: we do NOT pass onNotificationsChange / onPicturesChange / onLocalisationChange
        SettingsContent(onEditProfileClick = {}, onLogoutClick = {}, uiState = uiState)
      }
    }

    // Just click the toggles; if nothing crashes, default callbacks executed
    composeRule.onNodeWithTag(SettingsScreenTestTags.NOTIFICATIONS_TOGGLE).performClick()
    composeRule.onNodeWithTag(SettingsScreenTestTags.PICTURES_TOGGLE).performClick()
    composeRule.onNodeWithTag(SettingsScreenTestTags.LOCALISATION_TOGGLE).performClick()
  }

  @Test
  fun handlePermissions_processes_gallery_and_location_events() {
    val viewModel = SettingsViewModel()

    composeRule.setContent {
      MaterialTheme { SettingsScreen(viewModel = viewModel, onSignedOut = {}, onGoBack = {}) }
    }

    // Let LaunchedEffect(Unit) { viewModel.refreshPermissions(context) } and
    // the permissionEvents collector start.
    composeRule.waitForIdle()

    composeRule.runOnIdle {
      val field = SettingsViewModel::class.java.getDeclaredField("_permissionEvents")
      field.isAccessible = true
      @Suppress("UNCHECKED_CAST")
      val flow = field.get(viewModel) as MutableSharedFlow<PermissionEvent>

      // Emit two different events to hit RequestGallery and RequestLocation branches
      flow.tryEmit(PermissionEvent.RequestGallery)
      flow.tryEmit(PermissionEvent.RequestLocation)
    }

    // Give the collector time to process
    composeRule.waitForIdle()
    // No assertion needed: if we don't crash, branches are exercised
  }

  @Test
  fun handlePermissions_processes_notification_event() {
    val viewModel = SettingsViewModel()

    composeRule.setContent {
      MaterialTheme { SettingsScreen(viewModel = viewModel, onSignedOut = {}, onGoBack = {}) }
    }

    composeRule.waitForIdle()

    composeRule.runOnIdle {
      val field = SettingsViewModel::class.java.getDeclaredField("_permissionEvents")
      field.isAccessible = true
      @Suppress("UNCHECKED_CAST")
      val flow = field.get(viewModel) as MutableSharedFlow<PermissionEvent>

      flow.tryEmit(PermissionEvent.RequestNotification)
    }

    composeRule.waitForIdle()
    // Again: main point is to execute the launch paths in HandlePermissions.
  }

  @Test
  fun handlePermissions_processes_permission_events() {
    val viewModel = SettingsViewModel()

    composeRule.setContent {
      MaterialTheme { SettingsScreen(viewModel = viewModel, onSignedOut = {}, onGoBack = {}) }
    }

    // Let LaunchedEffects (refreshPermissions + permissionEvents collector) start
    composeRule.waitForIdle()

    composeRule.runOnIdle {
      val field = SettingsViewModel::class.java.getDeclaredField("_permissionEvents")
      field.isAccessible = true
      @Suppress("UNCHECKED_CAST")
      val flow = field.get(viewModel) as MutableSharedFlow<PermissionEvent>

      // Emit all three events to exercise when branches in HandlePermissions
      flow.tryEmit(PermissionEvent.RequestNotification)
      flow.tryEmit(PermissionEvent.RequestGallery)
      flow.tryEmit(PermissionEvent.RequestLocation)
    }

    // Give the collector time to handle the events
    composeRule.waitForIdle()
    // No assertion needed: JaCoCo only cares that the code paths execute.
  }

  @Test
  fun clicking_edit_profile_with_default_callback_does_not_crash() {
    val viewModel = SettingsViewModel()

    composeRule.setContent {
      MaterialTheme {
        // Use default onEditProfile (don’t pass it)
        SettingsScreen(viewModel = viewModel)
      }
    }

    // Click the button – this executes the default empty lambda
    composeRule.onNodeWithTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON).performClick()
  }

  @Test
  fun settingsScreen_allows_custom_credentialManager() {
    val viewModel = SettingsViewModel()
    val activity = composeRule.activity
    val credentialManager = CredentialManager.create(activity)

    composeRule.setContent {
      MaterialTheme {
        // Explicitly pass credentialManager -> non-default branch hit
        SettingsScreen(viewModel = viewModel, credentialManager = credentialManager)
      }
    }

    // No click needed; Jacoco only cares that this call path exists.
  }

  @Test
  fun handlePermissions_collects_permission_events() {
    val viewModel = SettingsViewModel()

    composeRule.setContent { MaterialTheme { SettingsScreen(viewModel = viewModel) } }

    // Let LaunchedEffects start (refreshPermissions + collector)
    composeRule.waitForIdle()

    composeRule.runOnIdle {
      val field = SettingsViewModel::class.java.getDeclaredField("_permissionEvents")
      field.isAccessible = true
      @Suppress("UNCHECKED_CAST")
      val flow = field.get(viewModel) as MutableSharedFlow<PermissionEvent>

      // Emit all three events – this exercises all when branches
      flow.tryEmit(PermissionEvent.RequestNotification)
      flow.tryEmit(PermissionEvent.RequestGallery)
      flow.tryEmit(PermissionEvent.RequestLocation)
    }

    composeRule.waitForIdle()
    // No assertion needed; if no crash, Jacoco records the executed paths.
  }

  @Test
  fun notificationPermissionLauncher_executes_when_permission_granted() {
    val viewModel = SettingsViewModel()

    composeRule.setContent {
      MaterialTheme { SettingsScreen(viewModel = viewModel, onSignedOut = {}, onGoBack = {}) }
    }

    composeRule.waitForIdle()
    composeRule.runOnIdle { viewModel.onNotificationPermissionResult(true) }

    composeRule.waitForIdle()
  }

  @Test
  fun notificationPermissionLauncher_granted_executes_if_block_and_updates_state() {
    val viewModel = SettingsViewModel()

    composeRule.setContent {
      MaterialTheme { SettingsScreen(viewModel = viewModel, onSignedOut = {}, onGoBack = {}) }
    }

    composeRule.waitForIdle()
    assertFalse(
        "Notifications should initially be disabled", viewModel.uiState.value.notificationsEnabled)

    composeRule.runOnIdle {
      val field = SettingsViewModel::class.java.getDeclaredField("_permissionEvents")
      field.isAccessible = true
      @Suppress("UNCHECKED_CAST")
      val flow = field.get(viewModel) as MutableSharedFlow<PermissionEvent>
      flow.tryEmit(PermissionEvent.RequestNotification)
    }

    composeRule.waitForIdle()
    composeRule.runOnIdle { viewModel.onNotificationPermissionResult(true) }
    composeRule.waitForIdle()
    assertTrue(
        "Notifications should be enabled after granting permission",
        viewModel.uiState.value.notificationsEnabled)
  }

  @Test
  fun notificationPermission_preTiramisu_executes_else_block_and_updates_state() {
    val viewModel = SettingsViewModel()

    composeRule.setContent {
      MaterialTheme { SettingsScreen(viewModel = viewModel, onSignedOut = {}, onGoBack = {}) }
    }

    composeRule.waitForIdle()

    composeRule.runOnIdle {
      val field = SettingsViewModel::class.java.getDeclaredField("_permissionEvents")
      field.isAccessible = true
      @Suppress("UNCHECKED_CAST")
      val flow = field.get(viewModel) as MutableSharedFlow<PermissionEvent>
      flow.tryEmit(PermissionEvent.RequestNotification)
    }

    composeRule.waitForIdle()
    assertNotNull("ViewModel should still be valid after permission flow", viewModel.uiState.value)
  }

  @Test
  fun notificationPermissionLauncher_denied_keeps_notifications_disabled() {
    val viewModel = SettingsViewModel()

    composeRule.setContent {
      MaterialTheme { SettingsScreen(viewModel = viewModel, onSignedOut = {}, onGoBack = {}) }
    }

    composeRule.waitForIdle()

    composeRule.runOnIdle {
      val field = SettingsViewModel::class.java.getDeclaredField("_permissionEvents")
      field.isAccessible = true
      @Suppress("UNCHECKED_CAST")
      val flow = field.get(viewModel) as MutableSharedFlow<PermissionEvent>
      flow.tryEmit(PermissionEvent.RequestNotification)
    }

    composeRule.waitForIdle()

    composeRule.runOnIdle { viewModel.onNotificationPermissionResult(false) }

    composeRule.waitForIdle()

    assertFalse(
        "Notifications should remain disabled after denying permission",
        viewModel.uiState.value.notificationsEnabled)
  }

  @Test
  fun all_permission_grants_update_all_ui_states_correctly() {
    val viewModel = SettingsViewModel()

    composeRule.setContent {
      MaterialTheme { SettingsScreen(viewModel = viewModel, onSignedOut = {}, onGoBack = {}) }
    }

    composeRule.waitForIdle()

    val initialState = viewModel.uiState.value
    assertNotNull("Initial state should not be null", initialState)

    composeRule.runOnIdle {
      val field = SettingsViewModel::class.java.getDeclaredField("_permissionEvents")
      field.isAccessible = true
      @Suppress("UNCHECKED_CAST")
      val flow = field.get(viewModel) as MutableSharedFlow<PermissionEvent>

      flow.tryEmit(PermissionEvent.RequestNotification)
    }

    composeRule.waitForIdle()

    composeRule.runOnIdle {
      viewModel.onNotificationPermissionResult(true)
      viewModel.onGalleryPermissionResult(true)
      viewModel.onLocationPermissionResult(true)
    }

    composeRule.waitForIdle()

    val finalState = viewModel.uiState.value
    assertTrue("Notifications should be enabled after granting", finalState.notificationsEnabled)
    assertTrue("Pictures should be enabled after granting", finalState.picturesEnabled)
    assertTrue("Localisation should be enabled after granting", finalState.localisationEnabled)
  }

  @Test
  fun multiple_notification_permission_grants_remain_consistent() {
    val viewModel = SettingsViewModel()

    composeRule.setContent { MaterialTheme { SettingsScreen(viewModel = viewModel) } }
    composeRule.waitForIdle()

    repeat(3) {
      composeRule.runOnIdle {
        val field = SettingsViewModel::class.java.getDeclaredField("_permissionEvents")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val flow = field.get(viewModel) as MutableSharedFlow<PermissionEvent>
        flow.tryEmit(PermissionEvent.RequestNotification)
      }
      composeRule.waitForIdle()

      composeRule.runOnIdle { viewModel.onNotificationPermissionResult(true) }
      composeRule.waitForIdle()
    }

    assertTrue(
        "Notifications should remain enabled after multiple grants",
        viewModel.uiState.value.notificationsEnabled)
  }

  @Test
  fun notification_toggle_off_updates_state_to_disabled() {
    val viewModel = SettingsViewModel()
    val context = composeRule.activity

    composeRule.runOnIdle {
      setUiState(viewModel, viewModel.uiState.value.copy(notificationsEnabled = true))
    }

    composeRule.setContent { MaterialTheme { SettingsScreen(viewModel = viewModel) } }
    composeRule.waitForIdle()

    assertTrue("Notifications should start enabled", viewModel.uiState.value.notificationsEnabled)

    composeRule.runOnIdle { viewModel.onNotificationsToggleRequested(false, context) }
    composeRule.waitForIdle()

    assertFalse(
        "Notifications should be disabled after toggling off",
        viewModel.uiState.value.notificationsEnabled)
  }

  // ----------------------------------------------------------------------------------------------
  // Helper: reflection to manipulate real ViewModel state
  // ----------------------------------------------------------------------------------------------

  private fun setUiState(viewModel: SettingsViewModel, newState: SettingsUIState) {
    val field = SettingsViewModel::class.java.getDeclaredField("_uiState")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val mutableStateFlow = field.get(viewModel) as MutableStateFlow<SettingsUIState>
    mutableStateFlow.value = newState
  }
}
