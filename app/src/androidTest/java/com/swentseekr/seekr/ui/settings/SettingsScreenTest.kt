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
import com.swentseekr.seekr.model.settings.UserSettings
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
  fun clicking_back_button_triggers_callback() {
    var backPressed = false

    composeRule.setContent { MaterialTheme { SettingsScreen(onGoBack = { backPressed = true }) } }

    composeRule.onNodeWithTag(SettingsScreenTestTags.BACK_BUTTON).performClick()
    assertTrue(backPressed)
  }

  @Test
  fun clicking_callbacks_work() {
    var backPressed = false
    var editProfileClicked = false
    var logoutClicked = false
    var notificationsToggled = false
    var picturesToggled = false
    var localisationToggled = false

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            appVersion = SettingsScreenStrings.APP_VERSION_1,
            onEditProfileClick = { editProfileClicked = true },
            onLogoutClick = { logoutClicked = true },
            uiState =
                UserSettings(
                    notificationsEnabled = false,
                    picturesEnabled = false,
                    localisationEnabled = false),
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
  }

  @Test
  fun clicking_edit_profile_button_triggers_callback() {
    var editProfileTriggered = false

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            appVersion = SettingsScreenStrings.APP_VERSION_1,
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
            appVersion = SettingsScreenStrings.APP_VERSION_1,
            onEditProfileClick = {},
            onLogoutClick = { logoutTriggered = true })
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.LOGOUT_BUTTON).performClick()
    assertTrue(logoutTriggered)
  }

  @Test
  fun app_version_is_displayed_correctly() {
    val expectedVersion = SettingsScreenStrings.APP_VERSION_2
    composeRule.setContent {
      MaterialTheme {
        SettingsContent(appVersion = expectedVersion, onEditProfileClick = {}, onLogoutClick = {})
      }
    }
    composeRule.onNodeWithText(SettingsScreenStrings.VERSION_LABEL).assertExists()
    composeRule.onNodeWithText(expectedVersion).assertExists()
  }

  @Test
  fun app_version_is_unknown_when_null() {
    composeRule.setContent {
      MaterialTheme {
        SettingsContent(appVersion = null, onEditProfileClick = {}, onLogoutClick = {})
      }
    }

    composeRule.onNodeWithText(SettingsScreenStrings.VERSION_LABEL).assertExists()
    composeRule.onNodeWithText(SettingsScreenStrings.UNKNOW_VERSION).assertExists()
  }

  @Test
  fun unknown_app_version_displayed() {
    composeRule.setContent {
      MaterialTheme {
        SettingsContent(appVersion = null, onEditProfileClick = {}, onLogoutClick = {})
      }
    }

    composeRule.onNodeWithText(SettingsScreenStrings.VERSION_LABEL).assertExists()
    composeRule.onNodeWithText(SettingsScreenStrings.UNKNOW_VERSION).assertExists()
  }

  @Test
  fun toggles_reflect_initial_ui_state() {
    composeRule.setContent {
      MaterialTheme {
        SettingsContent(
            appVersion = SettingsScreenStrings.APP_VERSION_1,
            onEditProfileClick = {},
            onLogoutClick = {},
            uiState =
                UserSettings(
                    notificationsEnabled = true,
                    picturesEnabled = true,
                    localisationEnabled = false))
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.NOTIFICATIONS_TOGGLE).assertIsOn()
    composeRule.onNodeWithTag(SettingsScreenTestTags.PICTURES_TOGGLE).assertIsOn()
    composeRule.onNodeWithTag(SettingsScreenTestTags.LOCALISATION_TOGGLE).assertIsOff()
  }

  @Test
  fun top_bar_title_is_displayed() {
    composeRule.setContent { MaterialTheme { SettingsScreen() } }

    composeRule.onNodeWithText(SettingsScreenStrings.TOP_BAR_TITLE).assertExists()
  }
}
