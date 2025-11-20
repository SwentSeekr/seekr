package com.swentseekr.seekr.ui.huntCardScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.components.HuntCardPreview
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
          authorId = "0",
          mainImageUrl = R.drawable.ic_launcher_foreground.toString(),
          reviewRate = 4.5)

  @Test
  fun huntCard_displaysTitleAndAuthor() {
    val hunt = createFakeHunt()
    composeTestRule.setContent { HuntCard(hunt, modifier = Modifier.Companion.padding(2.dp)) }

    composeTestRule.onNodeWithText(hunt.title).assertIsDisplayed()
    composeTestRule.onNodeWithText("by ${hunt.authorId}").assertIsDisplayed()
  }

  @Test
  fun huntCard_displaysDistanceDifficultyAndTime() {
    val hunt = createFakeHunt()
    composeTestRule.setContent { HuntCard(hunt, modifier = Modifier.Companion.padding(2.dp)) }

    composeTestRule.onNodeWithText(hunt.difficulty.toString()).assertIsDisplayed()
    composeTestRule.onNodeWithText("${hunt.distance} km").assertIsDisplayed()
    composeTestRule.onNodeWithText("${hunt.time} min").assertIsDisplayed()
  }

  @Test
  fun huntCard_displaysFavoriteIcon() {
    val hunt = createFakeHunt()
    composeTestRule.setContent { HuntCard(hunt, modifier = Modifier.Companion.padding(2.dp)) }

    // On recherche l’icône favorite par son contentDescription
    composeTestRule.onNodeWithContentDescription("Like Button").assertIsDisplayed()
  }

  @Test
  fun huntCard_preview_displaysCorrectly() {
    composeTestRule.setContent { HuntCardPreview() }

    composeTestRule.onNodeWithText("City Exploration").assertIsDisplayed()
  }
}
