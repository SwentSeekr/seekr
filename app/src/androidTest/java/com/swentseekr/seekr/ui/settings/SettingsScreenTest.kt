package com.swentseekr.seekr.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

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
    setUiState(
        viewModel,
        SettingsUIState(signedOut = false, appVersion = SettingsScreenStrings.APP_VERSION_1))

    var signedOutCalls = 0

    composeRule.setContent {
      MaterialTheme { SettingsScreen(viewModel = viewModel, onSignedOut = { signedOutCalls++ }) }
    }

    composeRule.waitForIdle()
    assertEquals(0, signedOutCalls)

    composeRule.runOnIdle {
      setUiState(
          viewModel,
          viewModel.uiState.value.copy(signedOut = true),
      )
    }

    composeRule.waitForIdle()
    assertEquals(1, signedOutCalls)
  }

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

  private fun setUiState(viewModel: SettingsViewModel, newState: SettingsUIState) {
    val field = SettingsViewModel::class.java.getDeclaredField("_uiState")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val mutableStateFlow = field.get(viewModel) as MutableStateFlow<SettingsUIState>
    mutableStateFlow.value = newState
  }
}
