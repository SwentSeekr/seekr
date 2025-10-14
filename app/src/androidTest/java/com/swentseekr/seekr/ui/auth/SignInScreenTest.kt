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

class SignInScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Clean up Firebase state before each test
    FirebaseAuth.getInstance().signOut()
  }

  @Test
  fun signInScreen_componentsAreDisplayed() {
    composeTestRule.setContent { SignInScreen() }

    composeTestRule.onNodeWithTag(APP_LOGO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(LOGIN_BUTTON).assertIsDisplayed()
  }

  @Test
  fun clickingLoginButton_isAvailable() {
    composeTestRule.setContent { SignInScreen() }

    composeTestRule.onNodeWithTag(LOGIN_BUTTON).assertIsDisplayed().performClick()
  }
}
