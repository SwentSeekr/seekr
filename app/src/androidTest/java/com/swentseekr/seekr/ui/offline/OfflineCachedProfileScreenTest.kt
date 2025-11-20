package com.swentseekr.seekr.ui.offline

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.profile.Profile
import org.junit.Rule
import org.junit.Test

class OfflineCachedProfileScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun showsOfflineMessage_whenProfileIsNull() {
    composeRule.setContent { MaterialTheme { OfflineCachedProfileScreen(profile = null) } }

    composeRule.onNodeWithText(OfflineProfileConstants.OFFLINE_NO_PROFILE).assertExists()
  }

  @Test
  fun showsProfileAndNoHuntsMessage_whenProfileHasNoHunts() {
    val profile =
        sampleProfile(
            pseudonym = "OfflineUser",
            bio = "Cached bio",
            myHunts = emptyList(),
            doneHunts = emptyList(),
            likedHunts = emptyList())

    composeRule.setContent { MaterialTheme { OfflineCachedProfileScreen(profile = profile) } }

    composeRule.onNodeWithText("OfflineUser").assertExists()
    composeRule.onNodeWithText("Cached bio").assertExists()
    composeRule.onNodeWithText(OfflineProfileConstants.NO_HUNTS_YET).assertExists()
  }

  @Test
  fun hidesBio_whenBlank() {
    val profile =
        sampleProfile(
            pseudonym = "NoBioUser",
            bio = "",
            myHunts = emptyList(),
            doneHunts = emptyList(),
            likedHunts = emptyList())

    composeRule.setContent { MaterialTheme { OfflineCachedProfileScreen(profile = profile) } }

    composeRule.onNodeWithText("NoBioUser").assertExists()
    composeRule.onNodeWithText("").assertDoesNotExist()
    composeRule.onNodeWithText(OfflineProfileConstants.NO_HUNTS_YET).assertExists()
  }

  @Test
  fun switchesTabs_andShowsCorrespondingHunts() {
    val myHuntTitle = "My Hunt Title"
    val doneHuntTitle = "Done Hunt Title"
    val likedHuntTitle = "Liked Hunt Title"

    val profile =
        sampleProfile(
            pseudonym = "TabsUser",
            bio = "Some bio",
            myHunts = listOf(sampleHunt(uid = "my1", title = myHuntTitle)),
            doneHunts = listOf(sampleHunt(uid = "done1", title = doneHuntTitle)),
            likedHunts = listOf(sampleHunt(uid = "liked1", title = likedHuntTitle)))

    composeRule.setContent { MaterialTheme { OfflineCachedProfileScreen(profile = profile) } }

    composeRule.onNodeWithText(myHuntTitle).assertExists()

    composeRule.onNodeWithContentDescription(OfflineProfileConstants.TAB_DONE_HUNTS).performClick()
    composeRule.onNodeWithText(doneHuntTitle).assertExists()

    composeRule.onNodeWithContentDescription(OfflineProfileConstants.TAB_LIKED_HUNTS).performClick()
    composeRule.onNodeWithText(likedHuntTitle).assertExists()
  }

  private fun sampleProfile(
      pseudonym: String,
      bio: String,
      myHunts: List<Hunt>,
      doneHunts: List<Hunt>,
      likedHunts: List<Hunt>
  ): Profile {
    val author =
        Author(
            pseudonym = pseudonym, bio = bio, profilePicture = 0, reviewRate = 4.5, sportRate = 3.5)
    return Profile(
        uid = "user1",
        author = author,
        myHunts = myHunts.toMutableList(),
        doneHunts = doneHunts.toMutableList(),
        likedHunts = likedHunts.toMutableList())
  }

  private fun sampleHunt(uid: String, title: String): Hunt {
    val start = Location(46.0, 6.0, "Start")
    val end = Location(47.0, 7.0, "End")
    return Hunt(
        uid = uid,
        start = start,
        end = end,
        middlePoints = emptyList(),
        status = HuntStatus.DISCOVER,
        title = title,
        description = "Desc for $title",
        time = 60.0,
        distance = 10.0,
        difficulty = Difficulty.EASY,
        authorId = "user1",
        otherImagesUrls = emptyList(),
        mainImageUrl = "https://example.com/image.jpg",
        reviewRate = 4.0)
  }
}
