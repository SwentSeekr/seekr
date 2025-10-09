package com.swentseekr.seekr.ui.map

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class MapScreenTagsPresenceTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun mapScreen_allExpectedTestTags_exist() {
    composeTestRule.setContent { MapScreen() }
    composeTestRule
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertExists()
  }
}
