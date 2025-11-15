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
  }

  @Test
  fun clicking_back_button_triggers_callback() {
    var backPressed = false

    composeRule.setContent { MaterialTheme { SettingsScreen(onGoBack = { backPressed = true }) } }

    composeRule.onNodeWithTag(SettingsScreenTestTags.BACK_BUTTON).performClick()
    assertTrue(backPressed)
  }

  @Test
  fun clicking_edit_profile_button_triggers_callback() {
    var editProfileTriggered = false

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            appVersion = "1.0.0",
            onEditProfileClick = { editProfileTriggered = true },
            onLogoutClick = {})
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON).performClick()
    assertTrue(editProfileTriggered)
  }

  @Test
  fun clicking_logout_button_triggers_callback() {
    var logoutTriggered = false

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            appVersion = "1.0.0",
            onEditProfileClick = {},
            onLogoutClick = { logoutTriggered = true })
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.LOGOUT_BUTTON).performClick()
    assertTrue(logoutTriggered)
  }

  @Test
  fun app_version_is_displayed_correctly() {
    val expectedVersion = "1.2.3"
    composeRule.setContent {
      MaterialTheme {
        SettingsContent(appVersion = expectedVersion, onEditProfileClick = {}, onLogoutClick = {})
      }
    }
    composeRule.onNodeWithText(SettingsScreenStrings.VersionLabel).assertExists()
    composeRule.onNodeWithText(expectedVersion).assertExists()
  }

  @Test
  fun app_version_is_unknown_when_null() {
    composeRule.setContent {
      MaterialTheme {
        SettingsContent(appVersion = null, onEditProfileClick = {}, onLogoutClick = {})
      }
    }

    composeRule.onNodeWithText(SettingsScreenStrings.VersionLabel).assertExists()
    composeRule.onNodeWithText(SettingsScreenStrings.UnknownVersion).assertExists()
  }

  @Test
  fun toggling_notifications_calls_callback() {
    var notificationsEnabled = false

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            appVersion = "1.0.0",
            onEditProfileClick = {},
            onLogoutClick = {},
            uiState = SettingsUIState(notificationsEnabled = false),
            onNotificationsChange = { notificationsEnabled = it })
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.NOTIFICATIONS_TOGGLE).performClick()
    assertTrue(notificationsEnabled)
  }

  @Test
  fun toggling_pictures_calls_callback() {
    var picturesEnabled = false

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            appVersion = "1.0.0",
            onEditProfileClick = {},
            onLogoutClick = {},
            uiState = SettingsUIState(picturesEnabled = false),
            onPicturesChange = { picturesEnabled = it })
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.PICTURES_TOGGLE).performClick()
    assertTrue(picturesEnabled)
  }

  @Test
  fun toggling_localisation_calls_callback() {
    var localisationEnabled = false

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            appVersion = "1.0.0",
            onEditProfileClick = {},
            onLogoutClick = {},
            uiState = SettingsUIState(localisationEnabled = false),
            onLocalisationChange = { localisationEnabled = it })
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.LOCALISATION_TOGGLE).performClick()
    assertTrue(localisationEnabled)
  }

  @Test
  fun app_condition_button_exists_and_clickable() {
    var clicked = false
    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            appVersion = "1.0.0",
            onEditProfileClick = {},
            onLogoutClick = {},
            uiState = SettingsUIState(),
            onNotificationsChange = {},
            onPicturesChange = {},
            onLocalisationChange = {})
      }
    }

    composeRule
        .onNodeWithTag(SettingsScreenTestTags.APP_CONDITION_BUTTON)
        .assertExists()
        .performClick()
  }

  @Test
  fun toggles_reflect_initial_ui_state() {
    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            appVersion = "1.0.0",
            onEditProfileClick = {},
            onLogoutClick = {},
            uiState =
                SettingsUIState(
                    notificationsEnabled = true,
                    picturesEnabled = true,
                    localisationEnabled = false),
            onNotificationsChange = {},
            onPicturesChange = {},
            onLocalisationChange = {})
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.NOTIFICATIONS_TOGGLE).assertIsOn()
    composeRule.onNodeWithTag(SettingsScreenTestTags.PICTURES_TOGGLE).assertIsOn()
    composeRule.onNodeWithTag(SettingsScreenTestTags.LOCALISATION_TOGGLE).assertIsOff()
  }

  @Test
  fun top_bar_title_is_displayed() {
    composeRule.setContent { MaterialTheme { SettingsScreen() } }

    composeRule.onNodeWithText(SettingsScreenStrings.TopBarTitle).assertExists()
  }
}
