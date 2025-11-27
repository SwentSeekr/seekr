package com.swentseekr.seekr.ui.profile

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.model.profile.sampleProfileWithPseudonym
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrMainNavHost
import com.swentseekr.seekr.ui.navigation.SeekrNavigationTest.Companion.MED
import com.swentseekr.seekr.ui.navigation.SeekrNavigationTest.Companion.XLONG
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.utils.FakeRepoSuccess
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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

  private fun node(tag: String) = composeRule.onNodeWithTag(tag, useUnmergedTree = true)

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
  fun opensProfile_checks_button () {
    tearDown()

    val myProfile = sampleProfileWithPseudonym(
      uid = "current-user",
      pseudonym = "Me"
    )

    val authorProfile = sampleProfileWithPseudonym(
      uid = "author-123",
      pseudonym = "John The Hunter"
    )

    val hunt = createHunt(
      uid = "hunt-001",
      title = "Treasure in Paris"
    ).copy(authorId = authorProfile.uid)

    withFakeRepo(FakeRepoSuccess(listOf(hunt),listOf(authorProfile, myProfile))) {
      var isBack = false
      composeRule.setContent {
        ProfileScreen(
          userId = authorProfile.uid,
          onGoBack = {isBack = true},
          testMode = true,
          testPublic = true
        )
      }
      composeRule.onNodeWithTag(ProfileTestTags.GO_BACK).performClick()
      assertTrue(isBack)
    }
  }

  @Test
  fun overview_click_navigates_to_huntcard_nagigates_to_profile() {

    setUp()
    // Use createHunt() to seed repository

    val myProfile = sampleProfileWithPseudonym(uid = "current-user", pseudonym = "Me")

    val authorProfile =
        sampleProfileWithPseudonym(uid = "author-123", pseudonym = "John The Hunter")

    val hunt =
        createHunt(uid = "hunt-001", title = "Treasure in Paris").copy(authorId = authorProfile.uid)

    withFakeRepo(FakeRepoSuccess(listOf(hunt), listOf(authorProfile, myProfile))) {
      // Compose the real NavHost (no Firebase involved).
      composeRule.runOnUiThread {
        composeRule.activity.setContent { SeekrMainNavHost(testMode = true) }
      }

      // Wait for Overview to draw with list content.
      waitUntilTrue(MED) {
        composeRule
            .onNodeWithTag(OverviewScreenTestTags.HUNT_LIST, useUnmergedTree = true)
            .assertExists()
        true
      }

      // Click the last card.
      composeRule
          .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
          .onFirst()
          .assertExists()
          .performClick()

      composeRule.waitUntil(timeoutMillis = XLONG) {
        composeRule
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      composeRule.onNodeWithTag(HuntCardScreenTestTags.AUTHOR_TEXT).performClick()

      composeRule.onNodeWithTag(ProfileTestTags.PROFILE_SCREEN).assertExists().assertIsDisplayed()
    }
  }



}
