package com.swentseekr.seekr.ui.profile

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.model.profile.sampleProfileWithPseudonym
import com.swentseekr.seekr.ui.components.HuntCardScreen
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import com.swentseekr.seekr.ui.huntCardScreen.FakeHuntCardViewModel
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrMainNavHost
import com.swentseekr.seekr.ui.navigation.SeekrNavigationTest.Companion.MED
import com.swentseekr.seekr.utils.FakeRepoSuccess
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for opening a public profile from various screens.
 *
 * This test suite verifies that navigating to a public profile works correctly from different entry
 * points, such as the HuntCardScreen. It checks that the profile screen opens and that navigation
 * actions function as expected.
 */
@RunWith(AndroidJUnit4::class)
class OpenPublicProfileTests {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private inline fun <T> withFakeRepo(repo: FakeRepoSuccess, crossinline block: () -> T): T {
    val prev = HuntRepositoryProvider.repository
    HuntRepositoryProvider.repository = repo
    return try {
      block()
    } finally {
      HuntRepositoryProvider.repository = prev
    }
  }

  @Before
  fun setupFirebase() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
  }

  private fun waitUntilTrue(timeout: Long = MED, block: () -> Boolean) {
    composeRule.waitUntil(timeoutMillis = timeout) { runCatching { block() }.getOrNull() == true }
  }

  private fun setUp() {
    composeRule.runOnUiThread { composeRule.activity.setContent { SeekrMainNavHost() } }
    waitUntilTrue(MED) {
      composeRule
          .onAllNodes(hasTestTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  private fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun opensProfileChecksButton() {
    tearDown()

    val myProfile =
        sampleProfileWithPseudonym(
            uid = ProfileTestsConstants.EXAMPLE_MY_USER_ID,
            pseudonym = ProfileTestsConstants.EXAMPLE_MY_PSEUDONYM)

    val authorProfile =
        sampleProfileWithPseudonym(
            uid = ProfileTestsConstants.EXAMPLE_OTHER_USER_ID,
            pseudonym = ProfileTestsConstants.EXAMPLE_OTHER_PSEUDONYM)

    val hunt =
        createHunt(
                uid = ProfileTestsConstants.EXAMPLE_HUNT_ID,
                title = ProfileTestsConstants.EXAMPLE_HUNT_TITLE)
            .copy(authorId = authorProfile.uid)

    withFakeRepo(FakeRepoSuccess(listOf(hunt), listOf(authorProfile, myProfile))) {
      var isBack = false
      composeRule.setContent {
        ProfileScreen(
            userId = authorProfile.uid,
            onGoBack = { isBack = true },
            testMode = true,
            testPublic = true)
      }
      composeRule.onNodeWithTag(ProfileTestTags.GO_BACK).performClick()
      assertTrue(isBack)
    }
  }

  @Test
  fun opensHuntCardChecksbutton() {

    val myProfile =
        sampleProfileWithPseudonym(
            uid = ProfileTestsConstants.EXAMPLE_MY_USER_ID,
            pseudonym = ProfileTestsConstants.EXAMPLE_MY_PSEUDONYM)

    val authorProfile =
        sampleProfileWithPseudonym(
            uid = ProfileTestsConstants.EXAMPLE_OTHER_USER_ID,
            pseudonym = ProfileTestsConstants.EXAMPLE_OTHER_PSEUDONYM)

    val hunt =
        createHunt(
                uid = ProfileTestsConstants.EXAMPLE_HUNT_ID,
                title = ProfileTestsConstants.EXAMPLE_HUNT_TITLE)
            .copy(authorId = authorProfile.uid)

    withFakeRepo(FakeRepoSuccess(listOf(hunt), listOf(authorProfile, myProfile))) {
      var isBack = false
      composeRule.setContent {
        HuntCardScreen(
            huntId = hunt.uid,
            huntCardViewModel = FakeHuntCardViewModel(hunt),
            goProfile = { isBack = true },
            navController = rememberNavController())
      }
      composeRule.onNodeWithTag(HuntCardScreenTestTags.AUTHOR_TEXT).performClick()
      assertTrue(isBack)
    }
  }
}
