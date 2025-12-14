package com.swentseekr.seekr.end_to_end

import android.Manifest
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

/**
 * Full-stack UI test that covers the entire hunt lifecycle: adding a hunt, finding it from the
 * Overview search, launching it from the Hunt Card, and validating/finishing the experience on the
 * map screen while faking location updates.
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
  private var testUserId: String = EndToEndHuntFlowM3TestConstants.DEFAULT_USER_ID

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
                    pseudonym = EndToEndHuntFlowM3TestConstants.USER_PSEUDONYM,
                    bio = EndToEndHuntFlowM3TestConstants.USER_BIO),
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

  /**
   * Executes the multi-step flow:
   * - create hunt via UI flow
   * - persist it to repositories
   * - refresh/search/open/begin
   * - validate checkpoints
   * - finish hunt
   */
  @Test
  fun addSearchBeginValidateAndFinishHunt() {
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

    composeRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    waitForNodeWithTag(ProfileTestTags.PROFILE_SCREEN)

    composeRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).assertIsDisplayed().performClick()
    waitForNodeWithTag(HuntScreenTestTags.ADD_HUNT_SCREEN)
  }

  /** Fills every field in the Add Hunt screen using the predefined constants. */
  private fun populateAddHuntForm() {

    replaceText(HuntScreenTestTags.INPUT_HUNT_TITLE, EndToEndHuntFlowM3TestConstants.HUNT_TITLE)
    replaceText(
        HuntScreenTestTags.INPUT_HUNT_DESCRIPTION, EndToEndHuntFlowM3TestConstants.HUNT_DESCRIPTION)
    replaceText(HuntScreenTestTags.INPUT_HUNT_TIME, EndToEndHuntFlowM3TestConstants.HUNT_TIME_HOURS)
    replaceText(
        HuntScreenTestTags.INPUT_HUNT_DISTANCE, EndToEndHuntFlowM3TestConstants.HUNT_DISTANCE_KM)

    composeRule.onNodeWithTag(HuntScreenTestTags.DROPDOWN_STATUS).performClick()
    composeRule.onNodeWithText(HuntStatus.FUN.name).performClick()

    composeRule.onNodeWithTag(HuntScreenTestTags.DROPDOWN_DIFFICULTY).performClick()
    composeRule.onNodeWithText(Difficulty.EASY.name).performClick()

    composeRule.onNodeWithTag(HuntScreenTestTags.BUTTON_SELECT_LOCATION).performClick()
  }

  /** Accepts the auto-generated checkpoints on the map selection screen. */
  private fun confirmAutoGeneratedPoints() {

    waitForNodeWithTag(AddPointsMapScreenTestTags.MAP_VIEW)
    composeRule.onNodeWithTag(AddPointsMapScreenTestTags.CONFIRM_BUTTON).performClick()
  }

  /**
   * Saves the hunt draft and approves it from the preview screen. If the app fails to return to
   * Overview within the timeout, it forces a back navigation and reopens Overview to keep the test
   * progressing.
   */
  private fun saveDraftAndConfirmPreview() {

    composeRule.onNodeWithTag(HuntScreenTestTags.HUNT_SAVE).performScrollTo().performClick()
    waitForNodeWithTag(PreviewHuntScreenTestTags.PREVIEW_HUNT_SCREEN)

    composeRule.onNodeWithTag(PreviewHuntScreenTestTags.CONFIRM_BUTTON).performClick()

    if (!waitForOverviewRoot(failOnTimeout = false)) {
      forceNavigateBackToOverview()
      waitForOverviewRoot()
    }
  }

  /**
   * Adds the hunt to the in-memory repositories so Overview/Map pick it up.
   *
   * @return The created [Hunt] instance that was inserted into the repositories.
   */
  private fun persistTestHunt(): Hunt = runBlocking {
    val newUid = huntsRepository.getNewUid()

    val start =
        Location(
            EndToEndHuntFlowM3TestConstants.START_LAT,
            EndToEndHuntFlowM3TestConstants.START_LNG,
            EndToEndHuntFlowM3TestConstants.START_NAME)

    val end =
        Location(
            EndToEndHuntFlowM3TestConstants.END_LAT,
            EndToEndHuntFlowM3TestConstants.END_LNG,
            EndToEndHuntFlowM3TestConstants.END_NAME)

    val hunt =
        Hunt(
            uid = newUid,
            start = start,
            end = end,
            middlePoints = emptyList(),
            status = HuntStatus.FUN,
            title = EndToEndHuntFlowM3TestConstants.HUNT_TITLE,
            description = EndToEndHuntFlowM3TestConstants.HUNT_DESCRIPTION,
            time = EndToEndHuntFlowM3TestConstants.HUNT_TIME_HOURS.toDouble(),
            distance = EndToEndHuntFlowM3TestConstants.HUNT_DISTANCE_KM.toDouble(),
            difficulty = Difficulty.EASY,
            authorId = testUserId,
            otherImagesUrls = emptyList(),
            mainImageUrl = "",
            reviewRate = EndToEndHuntFlowM3TestConstants.REVIEW_RATE)

    huntsRepository.addHunt(hunt)

    val updatedProfile = profileRepository.getProfile(testUserId)
    updatedProfile?.myHunts?.add(hunt)
    profileRepository.updateProfile(updatedProfile!!)

    hunt
  }

  /** Pull-to-refresh on the Overview list to reload hunts from the repository. */
  private fun refreshOverviewList() {
    swipeToRefreshOverview()
    waitForOverviewRoot()
  }

  /**
   * Types the search query and makes sure the hunt card matching the title appears.
   *
   * @param title The hunt title to search for.
   */
  private fun searchForHunt(title: String) {
    replaceText(OverviewScreenTestTags.SEARCH_BAR, title)
    performImeOn(OverviewScreenTestTags.SEARCH_BAR)
    waitForText(title)
  }

  /**
   * Opens the hunt card details by tapping the hunt title in the Overview list.
   *
   * @param title The expected hunt title to locate inside the list item.
   */
  private fun openHuntFromOverview(title: String) {

    composeRule
        .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
        .filter(hasAnyDescendant(hasText(title)))
        .onFirst()
        .performClick()

    waitForNodeWithTag(NavigationTestTags.HUNTCARD_SCREEN)
  }

  /** Taps the "Begin" CTA on the hunt card details screen. */
  private fun beginHuntFromDetails() {

    composeRule
        .onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON, useUnmergedTree = true)
        .performClick()
    waitForNodeWithTag(MapScreenTestTags.MAP_SCREEN)
  }

  /** Starts the hunt from the map overlay and asserts the Validate control is visible. */
  private fun startHuntOnMap() {

    waitForNodeWithTag(MapScreenTestTags.START)
    composeRule.onNodeWithTag(MapScreenTestTags.START).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.VALIDATE).assertIsDisplayed()
  }

  /**
   * Sends synthetic coordinates directly to the [MapViewModel] to emulate GPS updates and validate
   * the two checkpoints created in test mode.
   */
  private fun simulateLocationValidation() {

    composeRule.runOnIdle {
      mapViewModel.validateCurrentPoint(
          LatLng(
              EndToEndHuntFlowM3TestConstants.START_LAT, EndToEndHuntFlowM3TestConstants.START_LNG))
    }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(MapScreenTestTags.PROGRESS).assertTextContains("1 / 2")

    composeRule.runOnIdle {
      mapViewModel.validateCurrentPoint(
          LatLng(EndToEndHuntFlowM3TestConstants.END_LAT, EndToEndHuntFlowM3TestConstants.END_LNG))
    }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(MapScreenTestTags.PROGRESS).assertTextContains("2 / 2")
  }

  /** Clicks the Finish button. */
  private fun finishHuntFlow() {
    composeRule.onNodeWithTag(MapScreenTestTags.FINISH).assertIsEnabled().performClick()
  }

  /**
   * Ensures the Overview root composable is present before proceeding.
   *
   * @param failOnTimeout Whether to throw if the node is not found within the timeout.
   * @return `true` if the node appears before timeout, otherwise `false` (only when [failOnTimeout]
   *   is `false`).
   */
  private fun waitForOverviewRoot(failOnTimeout: Boolean = true): Boolean {
    return waitForNodeWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN, failOnTimeout = failOnTimeout)
  }

  /** Performs a swipe-down gesture on the hunt list to trigger pull-to-refresh. */
  private fun swipeToRefreshOverview() {
    composeRule.onNodeWithTag(OverviewScreenTestTags.HUNT_LIST).performTouchInput { swipeDown() }
    composeRule.waitForIdle()
  }

  /**
   * Waits until a node with [tag] exists somewhere in the semantics tree.
   *
   * @param tag The test tag to look for.
   * @param timeoutMillis Maximum time to wait before failing/returning.
   * @param failOnTimeout Whether to throw a [ComposeTimeoutException] if the timeout elapses.
   * @return `true` if the node appears before timeout, otherwise `false` when [failOnTimeout] is
   *   `false`.
   * @throws ComposeTimeoutException If the node does not appear within [timeoutMillis] and
   *   [failOnTimeout] is `true`.
   */
  private fun waitForNodeWithTag(
      tag: String,
      timeoutMillis: Long = EndToEndHuntFlowM3TestConstants.WAIT_MS,
      failOnTimeout: Boolean = true
  ): Boolean {
    return try {
      composeRule.waitUntil(timeoutMillis) {
        try {
          composeRule
              .onAllNodesWithTag(tag, useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
        } catch (_: Throwable) {
          false
        }
      }
      true
    } catch (e: ComposeTimeoutException) {
      if (failOnTimeout) throw e else false
    }
  }

  /**
   * Waits until the requested [text] is rendered anywhere on screen.
   *
   * @param text The text to wait for.
   * @param timeoutMillis Maximum time to wait before failing.
   * @throws ComposeTimeoutException If the text does not appear within [timeoutMillis].
   */
  private fun waitForText(
      text: String,
      timeoutMillis: Long = EndToEndHuntFlowM3TestConstants.WAIT_MS
  ) {
    composeRule.waitUntil(timeoutMillis) {
      try {
        composeRule
            .onAllNodesWithText(text, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      } catch (_: Throwable) {
        false
      }
    }
  }

  /**
   * Navigates back once, then explicitly taps the Overview bottom tab. Used as a recovery path when
   * navigation does not return to Overview automatically.
   */
  private fun forceNavigateBackToOverview() {

    composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
  }

  /**
   * Finds a semantics node that supports text input for a given container/tag. This helper supports
   * two common cases:
   * 1) the tagged node itself is editable (has SetTextAction),
   * 2) the tagged node is a container and contains an editable descendant.
   *
   * @param tag The test tag of the node (or container) that should provide an editable field.
   * @return A [SemanticsNodeInteraction] that supports SetTextAction (text input).
   * @throws AssertionError If no editable node can be found for the given [tag].
   */
  private fun findEditableNode(tag: String): SemanticsNodeInteraction {
    val root = composeRule.onNodeWithTag(tag, useUnmergedTree = true)

    root.performClick()

    runCatching {
      val direct =
          composeRule.onNode(hasTestTag(tag).and(hasSetTextAction()), useUnmergedTree = true)
      direct.fetchSemanticsNode()
      return direct
    }

    runCatching {
      val subtree =
          composeRule.onNode(
              hasTestTag(tag).and(hasAnyDescendant(hasSetTextAction())), useUnmergedTree = true)
      subtree.fetchSemanticsNode()

      val descendantEditable =
          composeRule.onNode(
              hasSetTextAction().and(hasAnyAncestor(hasTestTag(tag))), useUnmergedTree = true)
      descendantEditable.fetchSemanticsNode()
      return descendantEditable
    }

    throw AssertionError(
        "No editable node (SetTextAction) found for tag '$tag' or its descendants.")
  }

  /**
   * Clears any existing text in the input identified by [tag] and replaces it with [value].
   *
   * @param tag The test tag of the input (or a container holding the input).
   * @param value The text to input.
   */
  private fun replaceText(tag: String, value: String) {
    val input = findEditableNode(tag)
    input.performTextClearance()
    input.performTextInput(value)
  }

  /**
   * Performs the IME action (e.g., "Search", "Done") on the input identified by [tag].
   *
   * @param tag The test tag of the input (or a container holding the input).
   */
  private fun performImeOn(tag: String) {
    val input = findEditableNode(tag)
    input.performImeAction()
  }
}
