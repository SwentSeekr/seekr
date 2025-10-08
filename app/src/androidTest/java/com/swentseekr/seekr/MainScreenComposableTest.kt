package com.swentseekr.seekr

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.swentseekr.seekr.resources.C
import org.junit.Rule
import org.junit.Test

class MainScreenComposableTest {

  @get:Rule val composeRule = createComposeRule()

  /** ✅ 1. Ensure Greeting displays correct text and tag */
  @Test
  fun greeting_displaysCorrectText() {
    composeRule.setContent { Greeting("Android") }

    composeRule
        .onNodeWithTag(C.Tag.greeting, useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
        .assertTextEquals("Hello Android!")
  }

  /** ✅ 2. Handle dynamic recomposition — state change path */
  @Test
  fun greeting_recomposesWhenNameChanges() {
    var name by mutableStateOf("Android")
    composeRule.setContent { Greeting(name) }

    composeRule.onNodeWithTag(C.Tag.greeting).assertTextEquals("Hello Android!")

    composeRule.runOnUiThread { name = "Seekr" }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(C.Tag.greeting).assertTextEquals("Hello Seekr!")
  }

  /** ✅ 3. Verify empty string edge case */
  @Test
  fun greeting_handlesEmptyString() {
    composeRule.setContent { Greeting("") }
    composeRule.onNodeWithTag(C.Tag.greeting).assertTextEquals("Hello !")
  }

  /** ✅ 4. Cover MaterialTheme colorScheme branches (dark/light) */
  @Test
  fun mainScreen_rendersInLightThemes() {
    composeRule.setContent { MaterialTheme(colorScheme = lightColorScheme()) { MainScreen() } }
    composeRule.onNodeWithTag(C.Tag.main_screen_container).assertIsDisplayed()
  }

  @Test
  fun mainScreen_rendersInDarkThemes() {
    composeRule.setContent { MaterialTheme(colorScheme = darkColorScheme()) { MainScreen() } }
    composeRule.onNodeWithTag(C.Tag.main_screen_container).assertIsDisplayed()
  }

  /** ✅ 5. Ensure semantics tag propagation works */
  @Test
  fun greeting_appliesModifierSemanticsTag() {
    composeRule.setContent {
      Greeting(name = "TagTest", modifier = Modifier.semantics { testTag = "custom_greeting" })
    }

    composeRule
        .onNodeWithTag("custom_greeting", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Hello TagTest!")
  }
}
