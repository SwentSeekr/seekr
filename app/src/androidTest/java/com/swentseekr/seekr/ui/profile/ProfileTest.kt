package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.model.profile.createHuntWithRateAndDifficulty
import com.swentseekr.seekr.model.profile.emptyProfile
import com.swentseekr.seekr.model.profile.sampleProfile
import com.swentseekr.seekr.ui.components.RatingTestTags
import com.swentseekr.seekr.ui.components.RatingType
import com.swentseekr.seekr.ui.theme.*
import org.junit.Rule
import org.junit.Test

const val UI_WAIT_TIMEOUT = 3_000L

fun hasBackgroundColor(expected: Color) = SemanticsMatcher.expectValue(BackgroundColorKey, expected)

class ProfileScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private fun setProfileScreen(profile: Profile) {
    composeTestRule.setContent {
      ProfileScreen(userId = "user123", testMode = true, testProfile = profile)
    }
  }

  private fun checkTabColors(myHuntsColor: Color, doneHuntsColor: Color, likedHuntsColor: Color) {
    composeTestRule
        .onNodeWithTag(ProfileTestTags.TAB_MY_HUNTS)
        .assert(hasBackgroundColor(myHuntsColor))
    composeTestRule
        .onNodeWithTag(ProfileTestTags.TAB_DONE_HUNTS)
        .assert(hasBackgroundColor(doneHuntsColor))
    composeTestRule
        .onNodeWithTag(ProfileTestTags.TAB_LIKED_HUNTS)
        .assert(hasBackgroundColor(likedHuntsColor))
  }

  private fun waitForTabColor(tabTag: String, expectedColor: Color) {
    composeTestRule.waitUntil(timeoutMillis = UI_WAIT_TIMEOUT) {
      try {
        val nodes = composeTestRule.onAllNodesWithTag(tabTag).fetchSemanticsNodes()
        nodes.isNotEmpty() && nodes.any { it.config.getOrNull(BackgroundColorKey) == expectedColor }
      } catch (e: Exception) {
        false
      }
    }
  }

  private fun waitForHuntAndAssertVisible(huntTitle: String, notVisible: List<String>) {
    composeTestRule.waitUntil(UI_WAIT_TIMEOUT) {
      try {
        composeTestRule
            .onAllNodes(hasText(huntTitle, substring = true))
            .fetchSemanticsNodes()
            .isNotEmpty()
      } catch (e: Exception) {
        false
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNode(hasText(huntTitle, substring = true)).assertIsDisplayed()
    notVisible.forEach {
      composeTestRule.onNode(hasText(it, substring = true)).assertDoesNotExist()
    }
  }

  @Test
  fun profileScreen_displaysProfileInfo() {
    setProfileScreen(sampleProfile())

    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_REVIEW_RATING).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_PSEUDONYM).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_SPORT_RATING).assertIsDisplayed()
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
      composeTestRule
          .onAllNodesWithTag(ProfileTestTags.getTestTagForHuntCard(hunt, index))
          .onFirst()
          .assertIsDisplayed()
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
    waitForHuntAndAssertVisible("My Hunt", listOf("Done Hunt", "Liked Hunt"))
    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_DONE_HUNTS).performClick()
    waitForHuntAndAssertVisible("Done Hunt", listOf("My Hunt", "Liked Hunt"))
    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_LIKED_HUNTS).performClick()
    waitForHuntAndAssertVisible("Liked Hunt", listOf("My Hunt", "Done Hunt"))
  }

  @Test
  fun profileScreen_canScrollThroughManyHunts() {
    val sample = sampleProfile()
    val myHunts = List(20) { createHunt("hunt$it", "Hunt $it") }
    setProfileScreen(sample.copy(myHunts = myHunts.toMutableList()))

    val lastIndex = myHunts.lastIndex
    val lastHuntTag = ProfileTestTags.getTestTagForHuntCard(myHunts[lastIndex], lastIndex)

    composeTestRule.onAllNodesWithTag(lastHuntTag).onFirst().assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_LIST)
        .performScrollToNode(hasTestTag(lastHuntTag))

    composeTestRule.onAllNodesWithTag(lastHuntTag).onFirst().assertIsDisplayed()
  }

  @Test
  fun profileScreen_tabBackgroundChanges() {
    val profile =
        sampleProfile(
            myHunts = listOf(createHunt("hunt1", "My Hunt")),
            doneHunts = listOf(createHunt("hunt2", "Done Hunt")),
            likedHunts = listOf(createHunt("hunt3", "Liked Hunt")))
    setProfileScreen(profile)
    composeTestRule.waitForIdle()
    waitForTabColor(ProfileTestTags.TAB_MY_HUNTS, Green)
    checkTabColors(Green, White, White)

    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_DONE_HUNTS).performClick()
    composeTestRule.waitForIdle()
    waitForTabColor(ProfileTestTags.TAB_DONE_HUNTS, Green)
    checkTabColors(White, Green, White)

    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_LIKED_HUNTS).performClick()
    composeTestRule.waitForIdle()
    waitForTabColor(ProfileTestTags.TAB_LIKED_HUNTS, Green)
    checkTabColors(White, White, Green)
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

    composeTestRule.onAllNodesWithTag(RatingTestTags.full(0, RatingType.STAR)).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(RatingTestTags.full(1, RatingType.STAR)).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(RatingTestTags.full(2, RatingType.STAR)).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(RatingTestTags.half(RatingType.STAR)).assertCountEquals(0)
    composeTestRule.onAllNodesWithTag(RatingTestTags.empty(0, RatingType.STAR)).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(RatingTestTags.empty(1, RatingType.STAR)).assertCountEquals(1)

    composeTestRule.onAllNodesWithTag(RatingTestTags.full(0, RatingType.SPORT)).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(RatingTestTags.full(1, RatingType.SPORT)).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(RatingTestTags.full(2, RatingType.SPORT)).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(RatingTestTags.half(RatingType.SPORT)).assertCountEquals(0)
    composeTestRule
        .onAllNodesWithTag(RatingTestTags.empty(0, RatingType.SPORT))
        .assertCountEquals(1)
    composeTestRule
        .onAllNodesWithTag(RatingTestTags.empty(1, RatingType.SPORT))
        .assertCountEquals(1)
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

    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_REVIEWS_COUNT).assertIsDisplayed()
    // Assert correct reviews count isn't tested on purpose here since the review doesn't exist in
    // firebase yet

    composeTestRule
        .onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_DONE_COUNT)
        .assertIsDisplayed()
        .assert(hasText("- ${doneHunts.size} Hunts done"))
  }
}
