package com.swentseekr.seekr.ui.offline

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OfflineRequiredScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun offlineRequiredScreen_displaysAllTexts_andInvokesCallbackOnClick() {
    var callbackInvoked = false

    composeRule.setContent {
      MaterialTheme { OfflineRequiredScreen(onOpenSettings = { callbackInvoked = true }) }
    }

    composeRule.onNodeWithText(OfflineConstants.OFFLINE_TITLE).assertExists()
    composeRule.onNodeWithText(OfflineConstants.OFFLINE_MESSAGE).assertExists()
    composeRule.onNodeWithText(OfflineConstants.OPEN_SETTINGS_BUTTON).assertExists()

    composeRule.onNodeWithText(OfflineConstants.OPEN_SETTINGS_BUTTON).performClick()

    assertTrue(callbackInvoked)
  }
}
