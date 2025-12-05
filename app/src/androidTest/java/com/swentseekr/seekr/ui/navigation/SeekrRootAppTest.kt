package com.swentseekr.seekr.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.auth.SignInScreenTestTags
import com.swentseekr.seekr.ui.offline.OfflineConstants
import com.swentseekr.seekr.utils.FakeAuthEmulator
import com.swentseekr.seekr.utils.FakeJwtGenerator
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import com.swentseekr.seekr.utils.NetworkTestUtils
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Integration tests verifying SeekrRootApp high-level navigation logic. */
@RunWith(AndroidJUnit4::class)
class SeekrRootAppTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() = runBlocking {
    FirebaseTestEnvironment.setup()
    // Always start signed out for a clean state.
    FakeAuthEmulator.signOut()
    // Make sure network is on by default (for online tests).
    NetworkTestUtils.goOnline()
  }

  @After
  fun teardown() = runBlocking {
    // Restore network for whatever runs after these tests.
    NetworkTestUtils.goOnline()
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

      // Wait until the bottom navigation appears (main app shell).
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule
            .onAllNodesWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    }
  }

  @Test
  fun offlineWithoutCachedProfile_showsOfflineRequiredScreen() {
    // We explicitly simulate offline *before* rendering the composable.
    NetworkTestUtils.goOffline()

    composeTestRule.setContent { SeekrRootApp() }

    // Wait until the offline-required UI shows up.
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithText(OfflineConstants.OFFLINE_TITLE)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Assert the "You're offline" screen is visible.
    composeTestRule.onNodeWithText(OfflineConstants.OFFLINE_TITLE).assertIsDisplayed()
  }
}
