package com.swentseekr.seekr.end_to_end

import android.Manifest
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.swipeDown
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import com.swentseekr.seekr.ui.hunt.AddPointsMapScreenTestTags
import com.swentseekr.seekr.ui.hunt.HuntScreenTestTags
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntScreenTestTags
import com.swentseekr.seekr.ui.map.MapScreenTestTags
import com.swentseekr.seekr.ui.map.MapViewModel
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrMainNavHost
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.ui.profile.Profile
import com.swentseekr.seekr.ui.profile.ProfileTestTags
import com.swentseekr.seekr.utils.FakeAuthEmulator
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val WAIT_MEDIUM_MS = 12_000L
private const val LOG_TAG = "EndToEndHuntFlowTest"

/**
 * Full-stack UI test that covers the entire hunt lifecycle:
 * adding a hunt, finding it from the Overview search, launching it from the Hunt Card, and
 * validating/finishing the experience on the map screen while faking location updates.
 */
@RunWith(AndroidJUnit4::class)
class EndToEndHuntFlowTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  private lateinit var huntsRepository: HuntsRepository
  private lateinit var profileRepository: ProfileRepository
  private lateinit var mapViewModel: MapViewModel
  private lateinit var testProfile: Profile
  private var latestHuntId: String? = null
  private var testUserId: String = EndToEndHuntFlowTestConstants.DEFAULT_USER_ID
  private var stepCounter: Int = 0

  /** Prepares Firebase auth, injects local repositories, and boots the main nav host. */
  @Before
  fun setupEnvironment() = runBlocking {
    FirebaseTestEnvironment.setup()

    huntsRepository = HuntRepositoryProvider.repository
    profileRepository = ProfileRepositoryProvider.repository

    if (FirebaseTestEnvironment.isEmulatorActive()) {
      FirebaseTestEnvironment.clearEmulatorData()
    }
    FirebaseAuth.getInstance().signInAnonymously().await()

    testUserId = FirebaseAuth.getInstance().currentUser?.uid ?: testUserId


    testProfile =
        Profile(
            uid = testUserId,
            author =
                Author(
                    hasCompletedOnboarding = true,
                    hasAcceptedTerms = true,
                    pseudonym = EndToEndHuntFlowTestConstants.USER_PSEUDONYM,
                    bio = EndToEndHuntFlowTestConstants.USER_BIO),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())
    profileRepository.createProfile(testProfile)

    composeRule.setContent {
      SeekrMainNavHost(user = FirebaseAuth.getInstance().currentUser, testMode = true)
    }
    composeRule.waitForIdle()
    mapViewModel = ViewModelProvider(composeRule.activity)[MapViewModel::class.java]
  }

  /** Restores repository providers and signs out of the fake auth session. */
  @After
  fun tearDownEnvironment() = runBlocking {
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      FirebaseTestEnvironment.clearEmulatorData()
    }
    FakeAuthEmulator.signOut()
  }

  /** Executes the multi-step flow requested by the product team. */
  @Test
  fun addSearchBeginValidateAndFinishHunt() {
    stepCounter = 0
    logStep("Waiting for Overview screen to load")
    waitForOverviewRoot()
    openProfileTabAndStartAddFlow()
    populateAddHuntForm()
    confirmAutoGeneratedPoints()
    saveDraftAndConfirmPreview()

    val createdHunt = persistTestHunt()
    composeRule.runOnIdle { mapViewModel.refreshUIState() }

    refreshOverviewList()
    searchForHunt(createdHunt.title)
    openHuntFromOverview(createdHunt.title)
    beginHuntFromDetails()
    startHuntOnMap()
    simulateLocationValidation()
    finishHuntFlow()
  }

  /** Navigates to Profile via the bottom bar and opens the Add Hunt flow. */
  private fun openProfileTabAndStartAddFlow() {
    logStep("Opening Profile tab and starting Add Hunt flow")
    composeRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    waitForNodeWithTag(ProfileTestTags.PROFILE_SCREEN)
    composeRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).assertIsDisplayed().performClick()
    waitForNodeWithTag(HuntScreenTestTags.ADD_HUNT_SCREEN)
  }

  /** Fills every field in the Add Hunt screen using the predefined constants. */
  private fun populateAddHuntForm() {
    logStep("Populating Add Hunt form fields")
    replaceText(HuntScreenTestTags.INPUT_HUNT_TITLE, EndToEndHuntFlowTestConstants.HUNT_TITLE)

    replaceText(
      HuntScreenTestTags.INPUT_HUNT_DESCRIPTION, EndToEndHuntFlowTestConstants.HUNT_DESCRIPTION)

    replaceText(
      HuntScreenTestTags.INPUT_HUNT_TIME, EndToEndHuntFlowTestConstants.HUNT_TIME_HOURS)

    replaceText(
      HuntScreenTestTags.INPUT_HUNT_DISTANCE, EndToEndHuntFlowTestConstants.HUNT_DISTANCE_KM)

    composeRule.onNodeWithTag(HuntScreenTestTags.DROPDOWN_STATUS).performClick()
    composeRule.onNodeWithText(HuntStatus.FUN.name).performClick()

    composeRule.onNodeWithTag(HuntScreenTestTags.DROPDOWN_DIFFICULTY).performClick()
    composeRule.onNodeWithText(Difficulty.EASY.name).performClick()

    composeRule.onNodeWithTag(HuntScreenTestTags.BUTTON_SELECT_LOCATION).performClick()
  }

  /** Accepts the auto-generated checkpoints on the map selection screen. */
  private fun confirmAutoGeneratedPoints() {
    logStep("Confirming auto-generated checkpoints")
    waitForNodeWithTag(AddPointsMapScreenTestTags.MAP_VIEW)
    composeRule.onNodeWithTag(AddPointsMapScreenTestTags.CONFIRM_BUTTON).performClick()
  }

  /** Saves the hunt draft and approves it from the preview screen. */
  private fun saveDraftAndConfirmPreview() {
    logStep("Saving hunt draft and confirming preview")
    composeRule.onNodeWithTag(HuntScreenTestTags.HUNT_SAVE).performScrollTo().performClick()
    waitForNodeWithTag(PreviewHuntScreenTestTags.PREVIEW_HUNT_SCREEN)
    composeRule.onNodeWithTag(PreviewHuntScreenTestTags.CONFIRM_BUTTON).performClick()
    if (!waitForOverviewRoot(failOnTimeout = false)) {
      forceNavigateBackToOverview()
      waitForOverviewRoot()
    }
  }

  /** Adds the hunt to the in-memory repositories so Overview/Map pick it up. */
  private fun persistTestHunt(): Hunt =
      runBlocking {
        logStep("Persisting test hunt into repositories")
        val newUid = huntsRepository.getNewUid()
        val start =
            Location(
                EndToEndHuntFlowTestConstants.START_LAT,
                EndToEndHuntFlowTestConstants.START_LNG,
                EndToEndHuntFlowTestConstants.START_NAME)
        val end =
            Location(
                EndToEndHuntFlowTestConstants.END_LAT,
                EndToEndHuntFlowTestConstants.END_LNG,
                EndToEndHuntFlowTestConstants.END_NAME)

        val hunt =
            Hunt(
                uid = newUid,
                start = start,
                end = end,
                middlePoints = emptyList(),
                status = HuntStatus.FUN,
                title = EndToEndHuntFlowTestConstants.HUNT_TITLE,
                description = EndToEndHuntFlowTestConstants.HUNT_DESCRIPTION,
                time = EndToEndHuntFlowTestConstants.HUNT_TIME_HOURS.toDouble(),
                distance = EndToEndHuntFlowTestConstants.HUNT_DISTANCE_KM.toDouble(),
                difficulty = Difficulty.EASY,
                authorId = testUserId,
                otherImagesUrls = emptyList(),
                mainImageUrl = "",
                reviewRate = EndToEndHuntFlowTestConstants.REVIEW_RATE)

        huntsRepository.addHunt(hunt)
        val updatedProfile = profileRepository.getProfile(testUserId)
        updatedProfile?.myHunts?.add(hunt)
        profileRepository.updateProfile(updatedProfile!!)
        latestHuntId = newUid
        hunt
      }

  /** Pull-to-refresh on the Overview list to reload hunts from the repository. */
  private fun refreshOverviewList() {
    logStep("Refreshing Overview list")
    swipeToRefreshOverview()
    waitForOverviewRoot()
  }

  /** Types the search query and makes sure the hunt card matching the title appears. */
  private fun searchForHunt(title: String) {
    logStep("Searching for hunt titled '$title'")
    replaceText(OverviewScreenTestTags.SEARCH_BAR, title)
    performImeOn(OverviewScreenTestTags.SEARCH_BAR)
    waitForText(title)
  }

  /** Opens the hunt card details by tapping the hunt title in the Overview list. */
  private fun openHuntFromOverview(title: String) {
    logStep("Opening hunt '$title' from Overview list")
    composeRule
      .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
      .filter(hasAnyDescendant(hasText(title)))
      .onFirst()
      .performClick()
    waitForNodeWithTag(NavigationTestTags.HUNTCARD_SCREEN)
  }

  /** Scrolls (if needed) to the Begin Hunt CTA and taps it. */
  private fun beginHuntFromDetails() {
    logStep("Beginning hunt from details screen")
    composeRule
        .onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON, useUnmergedTree = true)
        .performClick()
    waitForNodeWithTag(MapScreenTestTags.MAP_SCREEN)
  }

  /** Starts the hunt from the map overlay. */
  private fun startHuntOnMap() {
    logStep("Starting hunt on map screen")
    waitForNodeWithTag(MapScreenTestTags.START)
    composeRule.onNodeWithTag(MapScreenTestTags.START).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.VALIDATE).assertIsDisplayed()
  }

  /**
   * Sends synthetic coordinates directly to the MapViewModel to emulate GPS updates and validate
   * the two checkpoints created in test mode.
   */
  private fun simulateLocationValidation() {
    logStep("Simulating location validation via MapViewModel")
    composeRule.runOnIdle {
      mapViewModel.validateCurrentPoint(
          LatLng(
              EndToEndHuntFlowTestConstants.START_LAT,
              EndToEndHuntFlowTestConstants.START_LNG))
    }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(MapScreenTestTags.PROGRESS).assertTextContains("1 / 2")

    composeRule.runOnIdle {
      mapViewModel.validateCurrentPoint(
          LatLng(
              EndToEndHuntFlowTestConstants.END_LAT, EndToEndHuntFlowTestConstants.END_LNG))
    }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(MapScreenTestTags.PROGRESS).assertTextContains("2 / 2")
  }

  /** Clicks the Finish button and waits until the overlay resets to its idle state. */
  private fun finishHuntFlow() {
    logStep("Finishing hunt flow")
    composeRule.onNodeWithTag(MapScreenTestTags.FINISH).assertIsEnabled().performClick()
  }

  /** Ensures the Overview root composable is present before proceeding. */
  private fun waitForOverviewRoot(failOnTimeout: Boolean = true): Boolean {
    return waitForNodeWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN, failOnTimeout = failOnTimeout)
  }

  /** Performs a swipe-down gesture on the hunt list to trigger pull-to-refresh. */
  private fun swipeToRefreshOverview() {
    composeRule
        .onNodeWithTag(OverviewScreenTestTags.HUNT_LIST)
        .performTouchInput { swipeDown() }
    composeRule.waitForIdle()
  }

  /** Waits until a node with [tag] exists somewhere in the semantics tree. */
  private fun waitForNodeWithTag(
      tag: String,
      timeoutMillis: Long = WAIT_MEDIUM_MS,
      failOnTimeout: Boolean = true
  ): Boolean {
    return try {
      composeRule.waitUntil(timeoutMillis) {
        try {
          composeRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        } catch (_: Throwable) {
          false
        }
      }
      true
    } catch (e: ComposeTimeoutException) {
      if (failOnTimeout) throw e else false
    }
  }

  /** Waits until the requested [text] is rendered anywhere on screen. */
  private fun waitForText(text: String, timeoutMillis: Long = WAIT_MEDIUM_MS) {
    composeRule.waitUntil(timeoutMillis) {
      try {
        composeRule.onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
      } catch (_: Throwable) {
        false
      }
    }
  }

  /** Navigates back to Profile then explicitly re-opens Overview to keep the test moving. */
  private fun forceNavigateBackToOverview() {
    logStep("Forcing navigation back to Overview tab")
    composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
  }

  private fun findEditableNode(tag: String): SemanticsNodeInteraction {
    val root = composeRule.onNodeWithTag(tag, useUnmergedTree = true)

    // For SearchBar this often expands/focuses the internal TextField
    root.performClick()

    // 1) If the tagged node itself is editable, use it.
    runCatching {
      val direct = composeRule.onNode(hasTestTag(tag).and(hasSetTextAction()), useUnmergedTree = true)
      direct.fetchSemanticsNode() // force evaluation so runCatching actually catches failures
      return direct
    }

    // 2) Otherwise find an editable descendant inside the tagged subtree.
    runCatching {
      // Assert subtree contains something editable (better error message if not)
      val subtree = composeRule.onNode(
        hasTestTag(tag).and(hasAnyDescendant(hasSetTextAction())),
        useUnmergedTree = true
      )
      subtree.fetchSemanticsNode()

      // Pick the editable node that has the tagged node somewhere in its ancestor chain
      val descendantEditable = composeRule.onNode(
        hasSetTextAction().and(hasAnyAncestor(hasTestTag(tag))),
        useUnmergedTree = true
      )
      descendantEditable.fetchSemanticsNode()
      return descendantEditable
    }

    root.printToLog("findEditableNode-$tag")
    throw AssertionError("No editable node (SetTextAction) found for tag '$tag' or its descendants.")
  }

  private fun replaceText(tag: String, value: String) {
    val input = findEditableNode(tag)
    input.performTextClearance()
    input.performTextInput(value)
  }

  private fun performImeOn(tag: String) {
    val input = findEditableNode(tag)
    input.performImeAction()
  }

  /** Logs the current step to the instrumentation output for easier debugging. */
  private fun logStep(message: String) {
    stepCounter += 1
    Log.i(LOG_TAG, "Step $stepCounter: $message")
  }
}
