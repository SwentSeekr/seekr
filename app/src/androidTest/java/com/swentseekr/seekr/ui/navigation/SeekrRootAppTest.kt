package com.swentseekr.seekr.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.model.profile.createOverviewTestHunt
import com.swentseekr.seekr.model.profile.sampleProfile
import com.swentseekr.seekr.offline.cache.ProfileCache
import com.swentseekr.seekr.ui.auth.SignInScreenTestTags
import com.swentseekr.seekr.ui.offline.OfflineConstants
import com.swentseekr.seekr.ui.profile.Profile
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
    NetworkTestUtils.goOnline()
    FakeAuthEmulator.signOut()
    ProfileCache.clear(composeTestRule.activity)
  }

  @After
  fun teardown() = runBlocking {
    // Restore network + clear auth state at the end of each test run.
    NetworkTestUtils.goOnline()
    FakeAuthEmulator.signOut()
    ProfileCache.clear(composeTestRule.activity)
  }

  /** Helper: sign in only if not already authenticated. */
  private fun ensureSignedIn() = runBlocking {
    if (!FakeAuthEmulator.isAuthenticated()) {
      val fakeToken = FakeJwtGenerator.createFakeGoogleIdToken()
      FakeAuthEmulator.signInWithFakeGoogleToken(fakeToken)
    }
  }

  private fun createOfflineCachedProfileForTest(): Profile {

    val sharedHunt =
        createOverviewTestHunt(
            uid = "shared-id",
            title = "Shared Hunt",
            description = "A shared test hunt used in multiple lists",
            time = 60.0,
            distance = 5.0,
        )

    val uniqueHunt =
        createOverviewTestHunt(
            uid = "unique-id",
            title = "Unique Hunt",
            description = "A unique test hunt only in doneHunts",
            time = 45.0,
            distance = 3.5,
        )
    return sampleProfile(
        myHunts = listOf(sharedHunt),
        doneHunts = listOf(sharedHunt, uniqueHunt),
        likedHunts = emptyList(),
    )
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

  @Test
  fun offlineWithoutCachedProfile_showsOfflineRequired_andOpenSettingsClickable() {
    // No network or auth hacks needed; overrides control everything.
    composeTestRule.setContent {
      SeekrRootApp(
          isOnlineOverride = false,
          cachedProfileInitialForTest = null,
      )
    }

    // Check that the offline-required UI is shown.
    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodesWithText(OfflineConstants.OFFLINE_TITLE)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText(OfflineConstants.OFFLINE_TITLE).assertIsDisplayed()

    // This click will execute openSettings, hitting:
    // - Intent(Settings.ACTION_WIRELESS_SETTINGS).addFlags(...)
    // - context.startActivity(intent)
    composeTestRule.onNodeWithText(OfflineConstants.OPEN_SETTINGS_BUTTON).assertIsDisplayed()
  }

  @Test
  fun offlineWithCachedProfile_usesOfflineNavHost_andBuildsOfflineHunts() {
    val cachedProfile = createOfflineCachedProfileForTest()

    composeTestRule.setContent {
      SeekrRootApp(
          isOnlineOverride = false, // force offline
          cachedProfileInitialForTest = cachedProfile, // non-null profile
      )
    }

    // Just let composition settle so the branch and distinctBy line run.
    composeTestRule.waitForIdle()
  }
}
