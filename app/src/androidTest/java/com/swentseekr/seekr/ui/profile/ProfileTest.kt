package com.swentseekr.seekr.ui.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.model.profile.createHuntWithRateAndDifficulty
import com.swentseekr.seekr.model.profile.emptyProfile
import com.swentseekr.seekr.model.profile.sampleProfile
import com.swentseekr.seekr.ui.components.MAX_RATING
import org.junit.Rule
import org.junit.Test

// Timeout for waiting for UI changes
const val UI_WAIT_TIMEOUT = 3_000L

// Semantics key for background color
fun hasBackgroundColor(expected: Color) = SemanticsMatcher.expectValue(BackgroundColorKey, expected)

/**
 * UI tests for the ProfileScreen composable.
 *
 * This test suite verifies the correct display and behavior of the ProfileScreen,
 * including profile information, hunt lists, tab switching, and empty states.
 */
class ProfileScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private fun setProfileScreen(profile: Profile) {
    composeTestRule.setContent {
      ProfileScreen(userId = "user123", testMode = true, testProfile = profile)
    }
  }

  private fun tagFor(hunt: Hunt, index: Int = 0): String =
      ProfileTestTags.getTestTagForHuntCard(hunt, index)

  private fun checkTabColors(my: Color, done: Color, liked: Color) {
    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_MY_HUNTS).assert(hasBackgroundColor(my))

    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_DONE_HUNTS).assert(hasBackgroundColor(done))

    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_LIKED_HUNTS).assert(hasBackgroundColor(liked))
  }

  private fun waitForTabColor(tabTag: String, expectedColor: Color) {
    composeTestRule.waitUntil(timeoutMillis = UI_WAIT_TIMEOUT) {
      try {
        val nodes = composeTestRule.onAllNodesWithTag(tabTag).fetchSemanticsNodes()
        nodes.any { it.config.getOrNull(BackgroundColorKey) == expectedColor }
      } catch (e: Exception) {
        false
      }
    }
  }

  private fun waitForHuntAndAssertVisible(visibleTag: String, notVisibleTags: List<String>) {
    composeTestRule.waitUntil(timeoutMillis = 3_000) {
      composeTestRule.onAllNodesWithTag(visibleTag).fetchSemanticsNodes().size == 1
    }

    composeTestRule.onNodeWithTag(visibleTag).assertIsDisplayed()

    notVisibleTags.forEach { tag -> composeTestRule.onAllNodesWithTag(tag).assertCountEquals(0) }
  }

  @Test
  fun profileScreen_displaysProfileInfo() {
    setProfileScreen(sampleProfile())

    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(ProfileTestTags.PROFILE_REVIEW_RATING, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_PSEUDONYM).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ProfileTestTags.PROFILE_SPORT_RATING, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_BIO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileTestTags.SETTINGS).assertIsDisplayed()
  }

  @Test
  fun profileScreen_profilePictureFallback() {
    val profile = sampleProfile()
    composeTestRule.setContent {
      ProfileScreen(
          userId = "user123",
          testMode = true,
          testProfile = profile.copy(author = profile.author.copy(profilePicture = 0)))
    }
    composeTestRule.onNodeWithTag(ProfileTestTags.EMPTY_PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profileScreen_addHuntButton_visibilityDependsOnProfile_notDisplayed() {
    composeTestRule.setContent { ProfileScreen(userId = "otherUser", testMode = false) }

    composeTestRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).assertIsNotDisplayed()
  }

  @Test
  fun profileScreen_displaysHuntsInLazyColumn() {
    val myHunts = List(3) { createHunt("hunt$it", "My Hunt $it") }
    setProfileScreen(sampleProfile(myHunts = myHunts))

    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_LIST).assertIsDisplayed()

    myHunts.forEachIndexed { index, hunt ->
      val tag = ProfileTestTags.getTestTagForHuntCard(hunt, index)

      composeTestRule
          .onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_LIST)
          .performScrollToNode(hasTestTag(tag))

      composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
    }
  }

  @Test
  fun profileScreen_tabSwitchingShowsCorrectHunts() {

    val myHunt = createHunt("hunt1", "My Hunt")
    val doneHunt = createHunt("hunt2", "Done Hunt")
    val likedHunt = createHunt("hunt3", "Liked Hunt")

    setProfileScreen(
        sampleProfile(
            myHunts = listOf(myHunt), doneHunts = listOf(doneHunt), likedHunts = listOf(likedHunt)))

    // ==== TAB : My Hunts (default) ====
    waitForHuntAndAssertVisible(
        visibleTag = tagFor(myHunt), notVisibleTags = listOf(tagFor(doneHunt), tagFor(likedHunt)))

    // ==== Switch to Done ====
    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_DONE_HUNTS).performClick()

    waitForHuntAndAssertVisible(
        visibleTag = tagFor(doneHunt), notVisibleTags = listOf(tagFor(myHunt), tagFor(likedHunt)))

    // ==== Switch to Liked ====
    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_LIKED_HUNTS).performClick()

    waitForHuntAndAssertVisible(
        visibleTag = tagFor(likedHunt), notVisibleTags = listOf(tagFor(myHunt), tagFor(doneHunt)))
  }

  @Composable
  @Test
  fun profileScreen_tabBackgroundChanges() {
    val SELECTED = MaterialTheme.colorScheme.primary
    val UNSELECTED = MaterialTheme.colorScheme.onSurface.copy(alpha = ProfileConstants.ALPHA)
    val profile =
        sampleProfile(
            myHunts = listOf(createHunt("hunt1", "My Hunt")),
            doneHunts = listOf(createHunt("hunt2", "Done Hunt")),
            likedHunts = listOf(createHunt("hunt3", "Liked Hunt")))

    // Load screen
    setProfileScreen(profile)
    composeTestRule.waitForIdle()

    waitForTabColor(ProfileTestTags.TAB_MY_HUNTS, SELECTED)
    checkTabColors(SELECTED, UNSELECTED, UNSELECTED)

    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_DONE_HUNTS).performClick()
    composeTestRule.waitForIdle()

    waitForTabColor(ProfileTestTags.TAB_DONE_HUNTS, SELECTED)
    checkTabColors(UNSELECTED, SELECTED, UNSELECTED)

    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_LIKED_HUNTS).performClick()
    composeTestRule.waitForIdle()

    waitForTabColor(ProfileTestTags.TAB_LIKED_HUNTS, SELECTED)
    checkTabColors(UNSELECTED, UNSELECTED, SELECTED)
  }

  private fun assertEmptyStateForTab(tabTestTag: String? = null) {
    setProfileScreen(emptyProfile())
    tabTestTag?.let { composeTestRule.onNodeWithTag(it).performClick() }
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_LIST).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileTestTags.EMPTY_HUNTS_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysEmptyMessage_whenMyHuntsEmpty() {
    assertEmptyStateForTab()
  }

  @Test
  fun profileScreen_displaysEmptyMessage_whenDoneHuntsEmpty() {
    assertEmptyStateForTab(ProfileTestTags.TAB_DONE_HUNTS)
  }

  @Test
  fun profileScreen_displaysEmptyMessage_whenLikedHuntsEmpty() {
    assertEmptyStateForTab(ProfileTestTags.TAB_LIKED_HUNTS)
  }

  @Test
  fun profileScreen_settingsButton_visibilityDependsOnProfile_notDisplayed() {
    composeTestRule.setContent { ProfileScreen(userId = "otherUser", testMode = false) }

    composeTestRule.onNodeWithTag(ProfileTestTags.SETTINGS).assertIsNotDisplayed()
  }

  @Test
  fun profileScreen_displaysCorrectCalculatedRatings() {
    val myHunts =
        listOf(
            createHuntWithRateAndDifficulty("hunt1", "Hunt 1", reviewRate = 2.0),
            createHuntWithRateAndDifficulty("hunt2", "Hunt 2", reviewRate = 4.0))

    val doneHunts =
        listOf(
            createHuntWithRateAndDifficulty("done1", "Done 1", difficulty = Difficulty.EASY),
            createHuntWithRateAndDifficulty("done2", "Done 2", difficulty = Difficulty.DIFFICULT))

    val baseProfile = sampleProfile(myHunts = myHunts, doneHunts = doneHunts)
    val viewModel = ProfileViewModel()
    val computedProfile = viewModel.buildComputedProfile(baseProfile)

    setProfileScreen(computedProfile)

    // Vérifie la note de review : "3.0/5.0"
    composeTestRule
        .onNodeWithTag(ProfileTestTags.PROFILE_REVIEW_RATING, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals("${computedProfile.author.reviewRate}/${MAX_RATING}")

    // Vérifie la note sport : "3.5/5.0"
    composeTestRule
        .onNodeWithTag(ProfileTestTags.PROFILE_SPORT_RATING, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals("${computedProfile.author.sportRate}/${MAX_RATING}")
  }

  @Test
  fun profileScreen_displaysCorrectReviewCountAndHuntsDone_withoutRepository() {
    val myHunts =
        listOf(
            createHuntWithRateAndDifficulty("hunt1", "Hunt 1"),
            createHuntWithRateAndDifficulty("hunt2", "Hunt 2"))
    val doneHunts =
        listOf(
            createHuntWithRateAndDifficulty("done1", "Done 1"),
            createHuntWithRateAndDifficulty("done2", "Done 2"),
            createHuntWithRateAndDifficulty("done3", "Done 3"))

    val profile = sampleProfile(myHunts = myHunts, doneHunts = doneHunts)

    setProfileScreen(
        profile = profile,
    )

    composeTestRule
        .onNodeWithTag(ProfileTestTags.PROFILE_REVIEWS_COUNT, useUnmergedTree = true)
        .assertIsDisplayed()
    // Assert correct reviews count isn't tested on purpose here since the review doesn't exist in
    // firebase yet

    composeTestRule
        .onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_DONE_COUNT, useUnmergedTree = true)
        .assertIsDisplayed()
        .assert(hasText("${doneHunts.size} Hunts done"))
  }

  @Test
  fun profileScreen_likedHuntsTab_displaysLikedHunts() {
    val likedHunts = List(3) { createHunt("liked$it", "Liked Hunt $it") }
    val profile = sampleProfile(likedHunts = likedHunts)
    setProfileScreen(profile)

    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_LIKED_HUNTS).performClick()

    waitForHuntAndAssertVisible(
        visibleTag = tagFor(likedHunts.first()), notVisibleTags = emptyList())

    likedHunts.forEachIndexed { index, hunt ->
      val tag = tagFor(hunt, index)
      composeTestRule
          .onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_LIST)
          .performScrollToNode(hasTestTag(tag))
      composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
    }
  }

  @Test
  fun profileScreen_likedHuntsTab_showsEmptyMessage_whenNoLikedHunts() {
    val profile = sampleProfile(likedHunts = emptyList())
    setProfileScreen(profile)

    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_LIKED_HUNTS).performClick()

    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_LIST).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileTestTags.EMPTY_HUNTS_MESSAGE).assertIsDisplayed()
  }
}
