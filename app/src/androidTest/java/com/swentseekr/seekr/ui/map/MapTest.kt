package com.swentseekr.seekr.ui.map

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import org.junit.Rule
import org.junit.Test

class MapTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.ACCESS_FINE_LOCATION,
          android.Manifest.permission.ACCESS_COARSE_LOCATION)

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
          mainImageUrl = 0.toString(),
          reviewRate = 4.0)

  private fun repo(vararg hunts: Hunt) =
      object : HuntsRepository {
        private val list = hunts.toList()

        override suspend fun addHunt(hunt: Hunt, mainImageUri: Uri?, otherImageUris: List<Uri>) {}

        override suspend fun getAllHunts(): List<Hunt> = list

        override suspend fun getAllMyHunts(authorID: String): List<Hunt> =
            list.filter { it.authorId == authorID }

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

  @Test
  fun mapIsDisplayedOnLocationGranted() {
    val vm = MapViewModel(repository = repo(hunt("1")))
    composeRule.setContent { MapScreen(viewModel = vm, true) }
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
  }

  @Test
  fun permissionRequestPopupIsShownWhenPermissionDenied() {
    val vm = MapViewModel(repository = repo(hunt("1")))
    composeRule.setContent { MapScreen(viewModel = vm, true) }
    composeRule.onNodeWithTag(MapScreenTestTags.PERMISSION_POPUP).assertIsDisplayed()
  }

  @Test
  fun locationPermissionRequestIsRetriggeredOnButtonClick() {
    val vm = MapViewModel(repository = repo(hunt("1")))
    composeRule.setContent { MapScreen(viewModel = vm, true) }
    composeRule.onNodeWithTag(MapScreenTestTags.PERMISSION_POPUP).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.GRANT_LOCATION_PERMISSION).performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
  }

  @Test
  fun permissionPopupNotShownWhenPermissionGranted() {
    val vm = MapViewModel(repository = repo(hunt("1")))
    composeRule.setContent { MapScreen(viewModel = vm) }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.PERMISSION_POPUP).assertDoesNotExist()
  }

  @Test
  fun startHuntButtonDisplaysAndStartsHunt() {
    val h = hunt("1")
    val vm = MapViewModel(repository = repo(h))
    composeRule.setContent { MapScreen(viewModel = vm) }
    composeRule.runOnIdle { vm.onMarkerClick(h) }

    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_VIEW).performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.START).assertIsDisplayed()

    composeRule.onNodeWithTag(MapScreenTestTags.START).performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.PROGRESS).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.VALIDATE).assertIsDisplayed()
  }

  @Test
  fun validateButtonDoesNotWorkIfUserTooFar() {
    val h = hunt("1")
    val vm = MapViewModel(repository = repo(h))
    composeRule.setContent { MapScreen(viewModel = vm) }
    composeRule.runOnIdle { vm.onMarkerClick(h) }

    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_VIEW).performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.START).performClick()

    composeRule
        .onNodeWithTag(MapScreenTestTags.PROGRESS)
        .assertTextContains("0 / 2", substring = true)
    composeRule.onNodeWithTag(MapScreenTestTags.VALIDATE).performClick()
    composeRule
        .onNodeWithTag(MapScreenTestTags.PROGRESS)
        .assertTextContains("0 / 2", substring = true)
  }
}
