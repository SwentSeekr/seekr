package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.model.profile.emptyProfile
import com.swentseekr.seekr.model.profile.sampleProfile
import org.junit.Rule
import org.junit.Test

const val UI_WAIT_TIMEOUT = 15_000L

fun hasBackgroundColor(expected: Color) = SemanticsMatcher.expectValue(BackgroundColorKey, expected)

class ProfileScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private fun setProfileScreen(profile: Profile) {
    val profile = profile
    composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }
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
  }

  @Test
  fun profileScreen_profilePictureFallback() {
    val profile = sampleProfile()
    composeTestRule.setContent {
      ProfileScreen(
          profile.copy(author = profile.author.copy(profilePicture = 0)), currentUserId = "user123")
    }
    composeTestRule.onNodeWithTag(ProfileTestTags.EMPTY_PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profileScreen_addHuntButton_visibilityDependsOnProfile_notDisplayed() {
    setProfileScreen(sampleProfile(uid = "otherUser"))
    composeTestRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).assertIsNotDisplayed()
  }

  @Test
  fun profileScreen_emptyHuntsShowsNothing() {
    setProfileScreen(
        sampleProfile(myHunts = emptyList(), doneHunts = emptyList(), likedHunts = emptyList()))
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_LIST).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileTestTags.EMPTY_HUNTS_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysHuntsInLazyColumn() {
    val myHunts = List(3) { createHunt("hunt$it", "My Hunt $it") }
    setProfileScreen(sampleProfile(myHunts = myHunts))
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_LIST).assertIsDisplayed()
    myHunts.forEachIndexed { index, hunt ->
      composeTestRule
          .onNodeWithTag(ProfileTestTags.getTestTagForHuntCard(hunt, index))
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

    composeTestRule.onNodeWithTag(lastHuntTag).assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_LIST)
        .performScrollToNode(hasTestTag(lastHuntTag))

    composeTestRule.onNodeWithTag(lastHuntTag).assertIsDisplayed()
  }

  // colors will be refactored as soon as we implement the theme
  @Test
  fun profileScreen_tabBackgroundChanges() {
    val profile =
        sampleProfile(
            myHunts = listOf(createHunt("hunt1", "My Hunt")),
            doneHunts = listOf(createHunt("hunt2", "Done Hunt")),
            likedHunts = listOf(createHunt("hunt3", "Liked Hunt")))
    setProfileScreen(profile)
    composeTestRule.waitForIdle()
    waitForTabColor(ProfileTestTags.TAB_MY_HUNTS, Color.Green)
    checkTabColors(Color.Green, Color.White, Color.White)

    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_DONE_HUNTS).performClick()
    composeTestRule.waitForIdle()
    waitForTabColor(ProfileTestTags.TAB_DONE_HUNTS, Color.Green)
    checkTabColors(Color.White, Color.Green, Color.White)

    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_LIKED_HUNTS).performClick()
    composeTestRule.waitForIdle()
    waitForTabColor(ProfileTestTags.TAB_LIKED_HUNTS, Color.Green)
    checkTabColors(Color.White, Color.White, Color.Green)
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
}
