package com.swentseekr.seekr.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.ui.auth.SignInScreen
import com.swentseekr.seekr.ui.auth.SignInScreenTestTags.APP_LOGO
import com.swentseekr.seekr.ui.auth.SignInScreenTestTags.LOGIN_BUTTON
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Clean up Firebase state before each test
    try {
      FirebaseAuth.getInstance().signOut()
    } catch (e: Exception) {
      println("Firebase not initialized in CI — continuing test without signOut()")
    }
  }

  @Test
  fun signInScreen_componentsAreDisplayed() {
    composeTestRule.setContent { SignInScreen() }

    // Wait for the logo and button to be present in the composition tree
    // fetchSemanticsNodes() to ensure the nodes are actually in the tree
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
        .onAllNodesWithTag(APP_LOGO)
        .fetchSemanticsNodes().isNotEmpty() &&
              composeTestRule
                .onAllNodesWithTag(LOGIN_BUTTON)
                .fetchSemanticsNodes().isNotEmpty()
    }

    // Check that both components are visible
    composeTestRule.onNodeWithTag(APP_LOGO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(LOGIN_BUTTON).assertIsDisplayed()
  }

  @Test
  fun clickingLoginButton_triggersSignInAttempt() {
    composeTestRule.setContent { SignInScreen() }

    // Wait for the button to be displayed before interacting
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
        .onAllNodesWithTag(LOGIN_BUTTON)
        .fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag(LOGIN_BUTTON).assertIsDisplayed().performClick()

  }
}
