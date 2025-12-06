package com.swentseekr.seekr.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.auth.SignInScreenTestTags
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
  fun setup() {
    // Make sure Firebase is wired up (emulators if available).
    FirebaseTestEnvironment.setup()
    // Start from "online" as a default; failures are safely ignored.
    NetworkTestUtils.goOnline()
  }

  @After
  fun teardown() = runBlocking {
    // Restore network + clear auth state at the end of each test run.
    NetworkTestUtils.goOnline()
    FakeAuthEmulator.signOut()
  }

  /** Helper: sign in only if not already authenticated. */
  private fun ensureSignedIn() = runBlocking {
    if (!FakeAuthEmulator.isAuthenticated()) {
      val fakeToken = FakeJwtGenerator.createFakeGoogleIdToken()
      FakeAuthEmulator.signInWithFakeGoogleToken(fakeToken)
    }
  }

  @Test
  fun showsSignInScreenWhenLoggedOut() {
    // Explicitly ensure logged-out state for this test.
    runBlocking { FakeAuthEmulator.signOut() }
    NetworkTestUtils.goOnline()

    composeTestRule.setContent { SeekrRootApp() }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
  }

  @Test
  fun loggedInUser_opensAppAndGoesDirectlyToMain() {
    // Ensure we have an authenticated user (no-op if already signed in).
    ensureSignedIn()
    NetworkTestUtils.goOnline()

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
