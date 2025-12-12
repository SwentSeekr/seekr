package com.swentseekr.seekr.ui.map

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import junit.framework.TestCase.assertTrue
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

        override suspend fun editHunt(
            huntID: String,
            newValue: Hunt,
            mainImageUri: Uri?,
            addedOtherImages: List<Uri>,
            removedOtherImages: List<String>,
            removedMainImageUrl: String?
        ) {}

        override suspend fun deleteHunt(uid: String) {}

        override fun getNewUid(): String = "x"
      }

  private fun setupMapWithSingleHunt(testMode: Boolean = false): Pair<MapViewModel, Hunt> {
    val h = hunt(Constants.HUNT_UID_1)
    val vm = MapViewModel(repository = repo(h))
    composeRule.setContent { MapScreen(viewModel = vm, testMode = testMode) }
    return vm to h
  }

  private fun focusHunt(vm: MapViewModel, h: Hunt) {
    composeRule.runOnIdle { vm.onMarkerClick(h) }
  }

  private fun openHunt(vm: MapViewModel, h: Hunt) {
    focusHunt(vm, h)
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_VIEW).performClick()
  }

  private fun startHuntFromMap(vm: MapViewModel, h: Hunt) {
    openHunt(vm, h)
    composeRule.onNodeWithTag(MapScreenTestTags.START).performClick()
  }

  @Test
  fun mapScreenShowsMapAndNoPopupInitially() {
    val vm = MapViewModel(repository = repo(hunt(Constants.HUNT_UID_1), hunt(Constants.HUNT_UID_2)))
    composeRule.setContent { MapScreen(viewModel = vm) }

    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_CARD).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).assertDoesNotExist()
  }

  @Test
  fun selectingHuntShowsPopupWithAllTaggedElements() {
    val h = hunt(Constants.HUNT_UID_1, Constants.FAKE)
    val vm = MapViewModel(repository = repo(h))
    composeRule.setContent { MapScreen(viewModel = vm) }

    focusHunt(vm, h)

    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_CARD).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_DESC).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_CANCEL).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_VIEW).assertIsDisplayed()
    composeRule.onNodeWithText(Constants.FAKE).assertIsDisplayed()
    composeRule.onNodeWithText(MapScreenStrings.Cancel).assertIsDisplayed()
    composeRule.onNodeWithText(MapScreenStrings.ViewHunt).assertIsDisplayed()
  }

  @Test
  fun pressingViewHuntHidesPopupAndShowsBackButton() {
    val (vm, h) = setupMapWithSingleHunt()

    openHunt(vm, h)

    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_CARD).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).assertIsDisplayed()
    composeRule.onNodeWithText(MapScreenStrings.BackToAllHunts).assertIsDisplayed()
  }

  @Test
  fun pressingBackToAllHuntsRemovesBackButtonAndPopupStaysHidden() {
    val (vm, h) = setupMapWithSingleHunt()

    openHunt(vm, h)
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).performClick()

    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_CARD).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
  }

  @Test
  fun pressingCancelHidesPopupAndLeavesAllHuntsMode() {
    val (vm, h) = setupMapWithSingleHunt()

    focusHunt(vm, h)
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_CANCEL).performClick()

    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_CARD).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
  }

  @Test
  fun mapIsDisplayedOnLocationGranted() {
    val (vm, _) = setupMapWithSingleHunt(testMode = true)
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
  }

  @Test
  fun permissionRequestPopupIsShownWhenPermissionDenied() {
    val (vm, _) = setupMapWithSingleHunt(testMode = true)
    composeRule.onNodeWithTag(MapScreenTestTags.PERMISSION_POPUP).assertIsDisplayed()
  }

  @Test
  fun locationPermissionRequestIsRetriggeredOnButtonClick() {
    val (vm, _) = setupMapWithSingleHunt(testMode = true)
    composeRule.onNodeWithTag(MapScreenTestTags.PERMISSION_POPUP).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.GRANT_LOCATION_PERMISSION).performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
  }

  @Test
  fun permissionPopupNotShownWhenPermissionGranted() {
    val (vm, _) = setupMapWithSingleHunt()
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.PERMISSION_POPUP).assertDoesNotExist()
  }

  @Test
  fun startHuntButtonDisplaysAndStartsHunt() {
    val (vm, h) = setupMapWithSingleHunt()
    focusHunt(vm, h)

    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_VIEW).performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.START).assertIsDisplayed()

    composeRule.onNodeWithTag(MapScreenTestTags.START).performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.PROGRESS).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.VALIDATE).assertIsDisplayed()
  }

  @Test
  fun validateButtonDoesNotWorkIfUserTooFar() {
    val (vm, h) = setupMapWithSingleHunt()

    startHuntFromMap(vm, h)

    composeRule
        .onNodeWithTag(MapScreenTestTags.PROGRESS)
        .assertTextContains("0 / 2", substring = true)
    composeRule.onNodeWithTag(MapScreenTestTags.VALIDATE).performClick()
    composeRule
        .onNodeWithTag(MapScreenTestTags.PROGRESS)
        .assertTextContains("0 / 2", substring = true)
  }

  @Test
  fun stopHuntButtonShowsConfirmationDialog() {
    val (vm, h) = setupMapWithSingleHunt()

    startHuntFromMap(vm, h)

    composeRule
        .onNodeWithTag(MapScreenTestTags.BUTTON_BACK)
        .assertIsDisplayed()
        .assertTextContains(MapScreenStrings.StopHunt)

    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).performClick()

    composeRule.onNodeWithTag(MapScreenTestTags.STOP_POPUP).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.CONFIRM).assertIsDisplayed()
    composeRule
        .onNodeWithTag(MapScreenTestTags.CONFIRM)
        .assertTextContains(MapScreenStrings.ConfirmStopHunt)
  }

  @Test
  fun confirmingStopHuntResetsStateToInitial() {
    val (vm, h) = setupMapWithSingleHunt()

    startHuntFromMap(vm, h)
    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.CONFIRM).performClick()

    composeRule.onNodeWithTag(MapScreenTestTags.BUTTON_BACK).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.POPUP_CARD).assertDoesNotExist()
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()

    composeRule.runOnIdle {
      val state = vm.uiState.value
      assert(!state.isFocused)
      assert(!state.isHuntStarted)
      assert(state.validatedCount == 0)
      assert(state.selectedHunt == null)
    }
  }

  @Test
  fun fullscreenCheckpointImage_closesWhenCloseButtonClicked() {
    composeRule.setContent {
      var isOpen by remember { mutableStateOf(true) }

      if (isOpen) {
        FullscreenCheckpointImage(
            imageUrl = "https://example.com/image.jpg",
            contentDescription = "Checkpoint image",
            onClose = { isOpen = false },
        )
      }
    }

    composeRule.onNodeWithTag(MapScreenTestTags.CLOSE_CHECKPOINT_IMAGE).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.CLOSE_CHECKPOINT_IMAGE).performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.CLOSE_CHECKPOINT_IMAGE).assertDoesNotExist()
  }

  @Test
  fun nextCheckpointSection_showsImage_and_isClickable_whenImageUrlPresent() {
    var imageClicked = false

    composeRule.setContent {
      MaterialTheme {
        NextCheckpointSection(
            checkpointName = "Checkpoint 1",
            checkpointDescription = "Some description",
            distanceToNext = 42,
            imageUrl = "https://example.com/image.png",
            onImageClick = { imageClicked = true })
      }
    }

    composeRule
        .onNodeWithTag(MapScreenTestTags.NEXT_CHECKPOINT_IMAGE)
        .assertIsDisplayed()
        .performClick()
    assertTrue(imageClicked)
  }
}
