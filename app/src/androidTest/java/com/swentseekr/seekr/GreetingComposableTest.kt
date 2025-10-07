package com.swentseekr.seekr

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.swentseekr.seekr.resources.C
import org.junit.Rule
import org.junit.Test

class GreetingComposableTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun greeting_rendersExpectedTextAndTag() {
    composeRule.setContent { Greeting(name = "Android") }

    composeRule
        .onNodeWithTag(C.Tag.greeting, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
        .assertTextEquals("Hello Android!")
  }
}
