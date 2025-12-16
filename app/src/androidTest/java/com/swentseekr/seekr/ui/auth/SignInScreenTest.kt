package com.swentseekr.seekr.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.ui.auth.SignInScreenTestTags.APP_LOGO
import com.swentseekr.seekr.ui.auth.SignInScreenTestTags.LOGIN_BUTTON
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the SignInScreen composable.
 *
 * This test suite verifies that the sign-in screen components are displayed correctly and that user
 * interactions, such as clicking the login button, function as expected.
 */
class SignInScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Clean up Firebase state before each test
    FirebaseAuth.getInstance().signOut()
  }

  @Test
  fun signInScreenComponentsAreDisplayed() {
    composeTestRule.setContent { SignInScreen() }

    composeTestRule.onNodeWithTag(APP_LOGO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(LOGIN_BUTTON).assertIsDisplayed()
  }

  @Test
  fun clickingLoginButtonIsAvailable() {
    composeTestRule.setContent { SignInScreen() }

    composeTestRule.onNodeWithTag(LOGIN_BUTTON).assertIsDisplayed().performClick()
  }
}
