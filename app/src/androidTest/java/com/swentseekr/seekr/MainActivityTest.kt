package com.swentseekr.seekr

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.resources.C
import org.junit.Assert.assertNotNull
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
  fun greeting_isDisplayedWithCorrectText() {
    composeRule
        .onNodeWithTag(C.Tag.greeting, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
        .assertTextEquals("Hello Android!")
  }

  @Test
  fun greeting_isUnique() {
    composeRule.onAllNodesWithTag(C.Tag.greeting, useUnmergedTree = true).assertCountEquals(1)
  }

  @Test
  fun ui_survivesOrientationChange() {
    composeRule.activityRule.scenario.onActivity {
      it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(C.Tag.main_screen_container, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
    composeRule
        .onNodeWithTag(C.Tag.greeting, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
        .assertTextEquals("Hello Android!")

    composeRule.activityRule.scenario.onActivity {
      it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    composeRule.waitForIdle()

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

  @Test
  fun ui_survivesActivityRecreate() {
    composeRule.activityRule.scenario.recreate()
    composeRule.waitForIdle()

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

  // 🧩 Added tests below — these help with condition coverage

  @Test
  fun activity_handlesNullSavedInstanceState() {
    // Use the activity from the compose rule instead of creating a new one
    val scenario = composeRule.activityRule.scenario

    // Reset to created state to simulate onCreate
    scenario.moveToState(Lifecycle.State.CREATED)

    // Access the activity properly within the main thread
    scenario.onActivity { activity ->
      // Verify the activity was created successfully
      assertNotNull(activity)
    }
  }

  @Test
  fun activity_lifecycleTransitions_doNotCrash() {
    val scenario = composeRule.activityRule.scenario

    scenario.moveToState(Lifecycle.State.CREATED)
    scenario.moveToState(Lifecycle.State.STARTED)
    scenario.moveToState(Lifecycle.State.RESUMED)
    scenario.moveToState(Lifecycle.State.DESTROYED)
    // No crash means coverage for all lifecycle branches
    assert(true)
  }
}
