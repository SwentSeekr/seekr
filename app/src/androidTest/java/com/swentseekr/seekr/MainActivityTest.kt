package com.swentseekr.seekr

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
    // Verifies the Surface with the expected test tag exists and is visible
    composeRule
        .onNodeWithTag(C.Tag.main_screen_container, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }
}
