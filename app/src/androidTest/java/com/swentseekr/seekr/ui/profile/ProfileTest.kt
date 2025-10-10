package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private fun sampleAuthor() =
      Author(
          pseudonym = "Spike Man",
          bio = "Adventurer",
          profilePicture = R.drawable.empty_user,
          reviewRate = 4.5,
          sportRate = 4.8)

  private fun sampleProfile(
      myHunts: List<Hunt> = emptyList(),
      doneHunts: List<Hunt> = emptyList(),
      likedHunts: List<Hunt> = emptyList(),
      uid: String = "user123"
  ): Profile {
    return Profile(
        uid = uid,
        author = sampleAuthor(),
        myHunts = myHunts.toMutableList(),
        doneHunts = doneHunts.toMutableList(),
        likedHunts = likedHunts.toMutableList())
  }

  private fun createHunt(uid: String, title: String) =
      Hunt(
          uid = uid,
          start = Location(0.0, 0.0, "Start"),
          end = Location(1.0, 1.0, "End"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = title,
          description = "Desc $title",
          time = 1.0,
          distance = 2.0,
          difficulty = Difficulty.EASY,
          author = sampleAuthor(),
          image = R.drawable.empty_user,
          reviewRate = 4.0)

  @Test
  fun profileScreen_displaysProfileInfo_Review() {
    val profile = sampleProfile()
    composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }

    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_REVIEW_RATING).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysProfileInfo_Pseudo() {
    val profile = sampleProfile()
    composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_PSEUDONYM).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysProfileInfo_Sport() {
    val profile = sampleProfile()
    composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_SPORT_RATING).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysProfileInfo_Bio() {
    val profile = sampleProfile()
    composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_BIO).assertIsDisplayed()
  }

  @Test
  fun profileScreen_addHuntButton_visibilityDependsOnProfile() {
    val myProfile = sampleProfile()
    composeTestRule.setContent { ProfileScreen(myProfile, currentUserId = "user123") }
    composeTestRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).assertIsDisplayed()
  }

  @Test
  fun profileScreen_addHuntButton_visibilityDependsOnProfile_notDisplayed() {
    val otherProfile = sampleProfile(uid = "otherUser")
    composeTestRule.setContent { ProfileScreen(otherProfile, currentUserId = "user123") }
    composeTestRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).assertIsNotDisplayed()
  }

  @Test
  fun profileScreen_displaysHuntsInLazyColumn() {
    val myHunts = List(3) { createHunt("hunt$it", "My Hunt $it") }
    val profile = sampleProfile(myHunts = myHunts)
    composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }

    composeTestRule.waitForIdle()
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

    val profile =
        sampleProfile(
            myHunts = listOf(myHunt), doneHunts = listOf(doneHunt), likedHunts = listOf(likedHunt))

    composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }
    composeTestRule.waitForIdle()

    composeTestRule.onNode(hasText("My Hunt", substring = true)).assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_DONE_HUNTS).performClick()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNode(hasText("Done Hunt", substring = true))
        .assertExists()
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(ProfileTestTags.TAB_LIKED_HUNTS).performClick()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNode(hasText("Liked Hunt", substring = true))
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun profileScreen_canScrollThroughManyHunts() {
    val sample = sampleProfile()
    val myHunts = List(20) { createHunt("hunt$it", "Hunt $it") }
    val profile = sample.copy(myHunts = myHunts.toMutableList())

    composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }

    val lastIndex = myHunts.lastIndex
    val lastHuntTag = ProfileTestTags.getTestTagForHuntCard(myHunts[lastIndex], lastIndex)

    composeTestRule.onNodeWithTag(lastHuntTag).assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_LIST)
        .performScrollToNode(hasTestTag(lastHuntTag))

    composeTestRule.onNodeWithTag(lastHuntTag).assertIsDisplayed()
  }
}
