package com.swent.seekr

import androidx.compose.ui.test.*
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.ui.components.HuntCard
import com.android.sample.ui.components.HuntCardPreview
import com.swent.seekr.model.author.Author
import com.swent.seekr.model.hunt.Difficulty
import com.swent.seekr.model.hunt.Hunt
import com.swent.seekr.model.hunt.HuntStatus
import com.swent.seekr.model.map.Location
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class HuntCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createFakeHunt() =
      Hunt(
          uid = "hunt123",
          start = Location(40.7128, -74.0060, "New York"),
          end = Location(40.730610, -73.935242, "Brooklyn"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = "City Exploration",
          description = "Discover hidden gems in the city",
          time = 2.5,
          distance = 5.0,
          difficulty = Difficulty.DIFFICULT,
          author = Author("spike man", "", 1, 2.5, 3.0),
          image = R.drawable.ic_launcher_foreground, // ou une image de ton projet
          reviewRate = 4.5)

  @Test
  fun huntCard_displaysTitleAndAuthor() {
    val hunt = createFakeHunt()
    composeTestRule.setContent { HuntCard(hunt) }

    composeTestRule.onNodeWithText(hunt.title).assertIsDisplayed()
    composeTestRule.onNodeWithText("by ${hunt.author.pseudonym}").assertIsDisplayed()
  }

  @Test
  fun huntCard_displaysDistanceDifficultyAndTime() {
    val hunt = createFakeHunt()
    composeTestRule.setContent { HuntCard(hunt) }

    composeTestRule.onNodeWithText(hunt.difficulty.toString()).assertIsDisplayed()
    composeTestRule.onNodeWithText("${hunt.distance} km").assertIsDisplayed()
    composeTestRule.onNodeWithText("${hunt.time} min").assertIsDisplayed()
  }

  @Test
  fun huntCard_displaysFavoriteIcon() {
    val hunt = createFakeHunt()
    composeTestRule.setContent { HuntCard(hunt) }

    // On recherche l’icône favorite par son contentDescription
    composeTestRule.onNodeWithContentDescription("Like Button").assertIsDisplayed()
  }

  @Test
  fun huntCard_preview_displaysCorrectly() {
    composeTestRule.setContent { HuntCardPreview() }

    composeTestRule.onNodeWithText("City Exploration").assertIsDisplayed()
  }
}
