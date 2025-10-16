package com.swentseekr.seekr.ui.map

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import org.junit.Rule
import org.junit.Test

class MapTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private fun hunt(uid: String, title: String = "Hunt $uid") =
      Hunt(
          uid = uid,
          start = Location(46.52, 6.63, "Start$uid"),
          end = Location(46.53, 6.64, "End$uid"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = title,
          description = "desc $uid",
          time = 1.0,
          distance = 1.0,
          difficulty = Difficulty.EASY,
          authorId = "A",
          image = 0,
          reviewRate = 4.0)

  private fun repo(vararg hunts: Hunt) =
      object : HuntsRepository {
        private val list = hunts.toList()

        override suspend fun addHunt(hunt: Hunt) {}

        override suspend fun getAllHunts(): List<Hunt> = list

        override suspend fun getHunt(uid: String): Hunt = list.first { it.uid == uid }

        override suspend fun editHunt(uid: String, updatedHunt: Hunt) {}

        override suspend fun deleteHunt(uid: String) {}

        override fun getNewUid(): String = "x"
      }

  @Test
  fun mapScreenShowsMapAndNoPopupInitially() {
    val vm = MapViewModel(repository = repo(hunt("1"), hunt("2")))
    composeRule.setContent { MapScreen(viewModel = vm) }

    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_CARD).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).assertDoesNotExist()
  }

  @Test
  fun selectingHuntShowsPopupWithAllTaggedElements() {
    val h = hunt("1", "Fake Hunt")
    val vm = MapViewModel(repository = repo(h))
    composeRule.setContent { MapScreen(viewModel = vm) }

    composeRule.runOnIdle { vm.onMarkerClick(h) }

    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_CARD).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_DESC).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_CANCEL).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_VIEW).assertIsDisplayed()
    composeRule.onNodeWithText("Fake Hunt").assertIsDisplayed()
    composeRule.onNodeWithText("Cancel").assertIsDisplayed()
    composeRule.onNodeWithText("View Hunt").assertIsDisplayed()
  }

  @Test
  fun pressingViewHuntHidesPopupAndShowsBackButton() {
    val h = hunt("1")
    val vm = MapViewModel(repository = repo(h))
    composeRule.setContent { MapScreen(viewModel = vm) }

    composeRule.runOnIdle { vm.onMarkerClick(h) }
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_VIEW).performClick()

    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_CARD).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).assertIsDisplayed()
    composeRule.onNodeWithText("Back to all hunts").assertIsDisplayed()
  }

  @Test
  fun pressingBackToAllHuntsRemovesBackButtonAndPopupStaysHidden() {
    val h = hunt("1")
    val vm = MapViewModel(repository = repo(h))
    composeRule.setContent { MapScreen(viewModel = vm) }

    composeRule.runOnIdle { vm.onMarkerClick(h) }
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_VIEW).performClick()

    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).performClick()

    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_CARD).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
  }

  @Test
  fun pressingCancelHidesPopupAndLeavesAllHuntsMode() {
    val h = hunt("1")
    val vm = MapViewModel(repository = repo(h))
    composeRule.setContent { MapScreen(viewModel = vm) }

    composeRule.runOnIdle { vm.onMarkerClick(h) }
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_CANCEL).performClick()

    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_CARD).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
  }
}
