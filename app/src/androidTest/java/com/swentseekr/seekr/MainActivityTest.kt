// File: 'src/androidTest/java/com/swentseekr/seekr/MainActivityTest.kt'
package com.swentseekr.seekr

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.resources.C
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun mainSurface_isDisplayed() {
    composeRule
        .onNodeWithTag(C.Tag.main_screen_container, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun mainSurface_isUnique() {
    composeRule
        .onAllNodesWithTag(C.Tag.main_screen_container, useUnmergedTree = true)
        .assertCountEquals(1)
  }

  @Test
  fun mainSurface_survivesOrientationChange() {
    composeRule.activityRule.scenario.onActivity {
      it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(C.Tag.main_screen_container, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()

    composeRule.activityRule.scenario.onActivity {
      it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(C.Tag.main_screen_container, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun mainSurface_survivesActivityRecreate() {
    composeRule.activityRule.scenario.recreate()
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(C.Tag.main_screen_container, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }
}
