package com.swentseekr.seekr.screen.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.auth.SignInScreenTestTags
import com.swentseekr.seekr.ui.navigation.AuthNavHost
import com.swentseekr.seekr.utils.FakeAuthEmulator
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Bootcamp-style UI test for AuthNavHost. This uses FakeAuthEmulator to simulate authentication,
 * not CredentialManager.
 */
@RunWith(AndroidJUnit4::class)
class AuthNavHostTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() = runBlocking {
    FirebaseTestEnvironment.setup()
    FakeAuthEmulator.signOut()
  }

  @Test
  fun showsSignInScreenInitially() {
    composeTestRule.setContent {
      AuthNavHost(
          credentialManager =
              androidx.credentials.CredentialManager.create(composeTestRule.activity),
          onSignedIn = {})
    }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.APP_LOGO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
  }

  //  @Test
  //  fun clickingSignIn_withFakeAuthEmulator_triggersSignedInState() = runBlocking {
  //    val fakeToken = FakeJwtGenerator.createFakeGoogleIdToken()
  //    FakeAuthEmulator.signInWithFakeGoogleToken(fakeToken)
  //
  //    var onSignedInCalled = false
  //
  //    composeTestRule.setContent {
  //      AuthNavHost(
  //          credentialManager =
  //              androidx.credentials.CredentialManager.create(composeTestRule.activity),
  //          onSignedIn = { onSignedInCalled = true })
  //    }
  //
  //    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).performClick()
  //    composeTestRule.waitForIdle()
  //
  //    assert(FakeAuthEmulator.isAuthenticated())
  //    assert(onSignedInCalled)
  //  }
}
