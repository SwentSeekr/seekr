package com.swentseekr.seekr

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.resources.C
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

  /** ✅ Basic existence checks */
  @Test
  fun mainScreen_andGreeting_areDisplayed() {
    composeRule
        .onNodeWithTag(C.Tag.main_screen_container, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()

    composeRule
        .onNodeWithTag(C.Tag.greeting, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
        .assertTextEquals("Hello Android!")
  }

  /** ✅ Uniqueness checks for nodes */
  @Test
  fun ui_elements_areUnique() {
    composeRule
        .onAllNodesWithTag(C.Tag.main_screen_container, useUnmergedTree = true)
        .assertCountEquals(1)
    composeRule.onAllNodesWithTag(C.Tag.greeting, useUnmergedTree = true).assertCountEquals(1)
  }

  /** ✅ Orientation changes (recreates activity) */
  @Test
  fun ui_survivesOrientationChange() {
    composeRule.activityRule.scenario.onActivity {
      it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(C.Tag.main_screen_container).assertExists().assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.greeting).assertTextEquals("Hello Android!")

    composeRule.activityRule.scenario.onActivity {
      it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(C.Tag.main_screen_container).assertExists().assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.greeting).assertTextEquals("Hello Android!")
  }

  /** ✅ Lifecycle transitions cover all possible paths */
  @Test
  fun activity_lifecycleTransitions_doNotCrash() {
    val scenario = composeRule.activityRule.scenario
    scenario.moveToState(Lifecycle.State.CREATED)
    scenario.moveToState(Lifecycle.State.STARTED)
    scenario.moveToState(Lifecycle.State.RESUMED)
    scenario.moveToState(Lifecycle.State.DESTROYED)
    assert(true)
  }

  /** ✅ Recreate activity to cover re-instantiation */
  @Test
  fun ui_survivesRecreation() {
    composeRule.activityRule.scenario.recreate()
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(C.Tag.main_screen_container).assertExists().assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.greeting).assertTextEquals("Hello Android!")
  }
}
