package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.test.assertCountEquals
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
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.onAllNodesWithTag

import org.junit.Rule
import org.junit.Test

const val UI_WAIT_TIMEOUT = 5_000L
fun hasBackgroundColor(expected: Color) = SemanticsMatcher.expectValue(BackgroundColorKey, expected)



class ProfileScreenTest {
  @get:Rule val composeTestRule = createComposeRule()


    private fun sampleAuthor() =
      Author(
          pseudonym = "Spike Man",
          bio = "Adventurer",
          profilePicture = R.drawable.profile_picture,
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

    private fun checkTabColors(
        myHuntsColor: Color,
        doneHuntsColor: Color,
        likedHuntsColor: Color
    ) {
        composeTestRule.onNodeWithTag(ProfileTestTags.TAB_MY_HUNTS).assert(hasBackgroundColor(myHuntsColor))
        composeTestRule.onNodeWithTag(ProfileTestTags.TAB_DONE_HUNTS).assert(hasBackgroundColor(doneHuntsColor))
        composeTestRule.onNodeWithTag(ProfileTestTags.TAB_LIKED_HUNTS).assert(hasBackgroundColor(likedHuntsColor))
    }

    private fun waitForTabColor(tabTag: String, expectedColor: Color) {
        composeTestRule.waitUntil(timeoutMillis = UI_WAIT_TIMEOUT) {
            composeTestRule.onAllNodesWithTag(tabTag)
                .fetchSemanticsNodes()
                .any { it.config.getOrNull(BackgroundColorKey) == expectedColor }
        }
    }

    private fun waitForHuntAndAssertVisible(huntTitle: String, notVisible: List<String>) {
        composeTestRule.waitUntil(UI_WAIT_TIMEOUT) {
            composeTestRule.onAllNodes(hasText(huntTitle, substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasText(huntTitle, substring = true)).assertIsDisplayed()
        notVisible.forEach {
            composeTestRule.onNode(hasText(it, substring = true)).assertDoesNotExist()
        }
    }


    @Test
  fun profileScreen_displaysProfileInfo() {
    val profile = sampleProfile()
    composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }

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
                profile.copy(author = profile.author.copy(profilePicture = 0)),
                currentUserId = "user123"
            )
        }
        composeTestRule.onNodeWithTag("EMPTY_PROFILE_PICTURE").assertIsDisplayed()
    }


  @Test
  fun profileScreen_addHuntButton_visibilityDependsOnProfile_notDisplayed() {
    val otherProfile = sampleProfile(uid = "otherUser")
    composeTestRule.setContent { ProfileScreen(otherProfile, currentUserId = "user123") }
    composeTestRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).assertIsNotDisplayed()
  }

    @Test
    fun profileScreen_emptyHuntsShowsNothing() {
        val profile = sampleProfile(myHunts = emptyList(), doneHunts = emptyList(), likedHunts = emptyList())
        composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }
        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_LIST).assertIsDisplayed()
        composeTestRule.onAllNodes(hasText("Hunt", substring = true)).assertCountEquals(0)
    }

  @Test
  fun profileScreen_displaysHuntsInLazyColumn() {
    val myHunts = List(3) { createHunt("hunt$it", "My Hunt $it") }
    val profile = sampleProfile(myHunts = myHunts)
    composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }
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

      val profile = sampleProfile(
          myHunts = listOf(myHunt),
          doneHunts = listOf(doneHunt),
          likedHunts = listOf(likedHunt)
      )

      composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }
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

    // colors will be refactored as soon as we implement the theme
    @Test
    fun profileScreen_tabBackgroundChanges() {
        val profile = sampleProfile(
            myHunts = listOf(createHunt("hunt1", "My Hunt")),
            doneHunts = listOf(createHunt("hunt2", "Done Hunt")),
            likedHunts = listOf(createHunt("hunt3", "Liked Hunt"))
        )

        composeTestRule.setContent { ProfileScreen(profile, currentUserId = "user123") }

        waitForTabColor(ProfileTestTags.TAB_MY_HUNTS, Color.Green)
        checkTabColors(Color.Green, Color.White, Color.White)

        composeTestRule.onNodeWithTag(ProfileTestTags.TAB_DONE_HUNTS).performClick()
        waitForTabColor(ProfileTestTags.TAB_DONE_HUNTS, Color.Green)
        checkTabColors(Color.White, Color.Green, Color.White)

        composeTestRule.onNodeWithTag(ProfileTestTags.TAB_LIKED_HUNTS).performClick()
        waitForTabColor(ProfileTestTags.TAB_LIKED_HUNTS, Color.Green)
        checkTabColors(Color.White, Color.White, Color.Green)
    }


}
