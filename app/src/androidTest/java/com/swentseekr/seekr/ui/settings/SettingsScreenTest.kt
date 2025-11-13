package com.swentseekr.seekr.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
  fun clicking_logout_button_triggers_callback() {
    var logoutTriggered = false

    composeRule.setContent {
      MaterialTheme {
        SettingsContent(appVersion = "1.0.0", onLogoutClick = { logoutTriggered = true })
      }
    }

    composeRule.onNodeWithTag(SettingsScreenTestTags.LOGOUT_BUTTON).performClick()
    assertTrue(logoutTriggered)
  }

  @Test
  fun app_version_is_displayed_correctly() {
    val expectedVersion = "1.2.3"
    composeRule.setContent {
      MaterialTheme { SettingsContent(appVersion = expectedVersion, onLogoutClick = {}) }
    }
    composeRule.onNodeWithText(SettingsScreenStrings.VersionLabel).assertExists()
    composeRule.onNodeWithText(expectedVersion).assertExists()
  }

  @Test
  fun app_version_is_unknown_when_null() {
    composeRule.setContent {
      MaterialTheme { SettingsContent(appVersion = null, onLogoutClick = {}) }
    }

    composeRule.onNodeWithText(SettingsScreenStrings.VersionLabel).assertExists()
    composeRule.onNodeWithText(SettingsScreenStrings.UnknownVersion).assertExists()
  }
}
