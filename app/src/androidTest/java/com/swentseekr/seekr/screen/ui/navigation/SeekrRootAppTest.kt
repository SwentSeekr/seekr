package com.swentseekr.seekr.screen.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.auth.SignInScreenTestTags
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrRootApp
import com.swentseekr.seekr.utils.FakeAuthEmulator
import com.swentseekr.seekr.utils.FakeJwtGenerator
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Integration test verifying SeekrRootApp navigates correctly after login. */
@RunWith(AndroidJUnit4::class)
class SeekrRootAppTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() = runBlocking {
    FirebaseTestEnvironment.setup()
    FakeAuthEmulator.signOut()
  }

  @Test
  fun showsSignInScreenWhenLoggedOut() {
    composeTestRule.setContent { SeekrRootApp() }
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
  }

  @Test
  fun loggedInUser_opensAppAndGoesDirectlyToMain() {
    runBlocking {
      val fakeToken = FakeJwtGenerator.createFakeGoogleIdToken()

      FakeAuthEmulator.signInWithFakeGoogleToken(fakeToken)
      composeTestRule.setContent { SeekrRootApp() }

      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule
            .onAllNodesWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    }
  }
}
