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
  fun opensProfile_checks_button() {
    tearDown()

    val myProfile = sampleProfileWithPseudonym(uid = "current-user", pseudonym = "Me")

    val authorProfile =
        sampleProfileWithPseudonym(uid = "author-123", pseudonym = "John The Hunter")

    val hunt =
        createHunt(uid = "hunt-001", title = "Treasure in Paris").copy(authorId = authorProfile.uid)

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
  fun opensHuntCard_checks_button() {

    val myProfile = sampleProfileWithPseudonym(uid = "current-user", pseudonym = "Me")

    val authorProfile =
        sampleProfileWithPseudonym(uid = "author-123", pseudonym = "John The Hunter")

    val hunt =
        createHunt(uid = "hunt-001", title = "Treasure in Paris").copy(authorId = authorProfile.uid)

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
