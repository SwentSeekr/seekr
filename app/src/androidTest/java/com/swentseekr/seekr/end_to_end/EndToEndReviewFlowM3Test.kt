package com.swentseekr.seekr.end_to_end

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
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
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.model.hunt.HuntReviewRepository
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import com.swentseekr.seekr.ui.components.MAX_RATING
import com.swentseekr.seekr.ui.hunt.review.replies.ReviewRepliesTestTags
import com.swentseekr.seekr.ui.map.MapViewModel
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrMainNavHost
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.ui.profile.Profile
import com.swentseekr.seekr.ui.profile.ProfileScreenConstants.ONE_DECIMAL_FORMAT
import com.swentseekr.seekr.ui.profile.ProfileTestTags
import com.swentseekr.seekr.utils.FakeAuthEmulator
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import java.util.Locale
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndReviewFlowM3Test {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  private lateinit var huntsRepository: HuntsRepository
  private lateinit var profileRepository: ProfileRepository
  private lateinit var reviewRepository: HuntReviewRepository
  private lateinit var mapViewModel: MapViewModel

  private var testUserId: String = EndToEndReviewFlowM3TestConstants.DEFAULT_USER_ID

  /**
   * Initializes Firebase test environment, signs in a user, seeds a profile, and mounts the app
   * content under test.
   */
  @Before
  fun setupEnvironment() = runBlocking {
    FirebaseTestEnvironment.setup()

    huntsRepository = HuntRepositoryProvider.repository
    profileRepository = ProfileRepositoryProvider.repository
    reviewRepository = HuntReviewRepositoryProvider.repository

    if (FirebaseTestEnvironment.isEmulatorActive()) {
      FirebaseTestEnvironment.clearEmulatorData()
    }

    FirebaseAuth.getInstance().signInAnonymously().await()
    testUserId = FirebaseAuth.getInstance().currentUser?.uid ?: testUserId

    val authorProfile =
        Profile(
            uid = testUserId,
            author =
                Author(
                    hasCompletedOnboarding = true,
                    hasAcceptedTerms = true,
                    pseudonym = EndToEndReviewFlowM3TestConstants.USER_PSEUDONYM,
                    bio = EndToEndReviewFlowM3TestConstants.USER_BIO),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())
    profileRepository.createProfile(authorProfile)

    composeRule.setContent {
      SeekrMainNavHost(user = FirebaseAuth.getInstance().currentUser, testMode = false)
    }
    composeRule.waitForIdle()

    mapViewModel = ViewModelProvider(composeRule.activity)[MapViewModel::class.java]
  }

  /** Clears emulator data (when enabled) and signs out from the test auth layer. */
  @After
  fun tearDownEnvironment() = runBlocking {
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      FirebaseTestEnvironment.clearEmulatorData()
    }
    FakeAuthEmulator.signOut()
  }

  /**
   * Validates the review flow: seed hunt, add review, reply, and confirm profile rating updates.
   */
  @Test
  fun addReviewRespondAndVerifyProfileRating() {
    waitForOverviewRoot()

    var hunt = seedTestHuntInRepository()
    composeRule.runOnIdle { mapViewModel.refreshUIState() }

    refreshOverviewList()
    searchForHunt(hunt.title)
    openHuntFromOverview(hunt.title)

    val review = addReviewFromSecondaryAccount(hunt)
    hunt = updateHuntRating(hunt, review.rating)

    reopenHuntDetails(hunt.title)
    assertReviewVisible(review.comment)

    sendReplyAsAuthor(EndToEndReviewFlowM3TestConstants.REPLY_COMMENT)
    waitForText(EndToEndReviewFlowM3TestConstants.REPLY_COMMENT)

    verifyProfileRating(EndToEndReviewFlowM3TestConstants.REVIEW_RATING.toDouble())
  }

  /**
   * Navigates back to overview and reopens the hunt details page by searching for its title.
   *
   * @param title Hunt title to search and open.
   */
  private fun reopenHuntDetails(title: String) {
    forceNavigateBackToOverview()
    waitForOverviewRoot()
    searchForHunt(title)
    openHuntFromOverview(title)
  }

  /**
   * Forces a target semantics matcher into view by first attempting `performScrollTo()` and then
   * swiping up on a scroll container.
   *
   * @param target Semantics matcher to make visible.
   * @param scrollContainerTag Test tag of a scroll container to swipe.
   * @param maxSwipes Maximum number of swipe attempts.
   */
  private fun scrollUntilVisible(
      target: SemanticsMatcher,
      scrollContainerTag: String,
      maxSwipes: Int = 10,
  ) {
    fun exists(): Boolean =
        runCatching { composeRule.onAllNodes(target, useUnmergedTree = true).fetchSemanticsNodes() }
            .getOrNull()
            ?.isNotEmpty() == true

    if (exists()) {
      runCatching {
        composeRule.onNode(target, useUnmergedTree = true).performScrollTo()
        composeRule.waitForIdle()
      }
      if (exists()) return
    }

    repeat(maxSwipes) {
      if (exists()) return
      runCatching {
        composeRule.onNodeWithTag(scrollContainerTag, useUnmergedTree = true).performTouchInput {
          swipeUp()
        }
        composeRule.waitForIdle()
      }
    }
  }

  /**
   * Ensures a review comment is reachable in the hunt details UI and asserts it is displayed.
   *
   * @param comment Review text expected on screen.
   */
  private fun assertReviewVisible(comment: String) {
    scrollUntilVisible(hasText(comment), NavigationTestTags.HUNTCARD_SCREEN)
    waitForText(comment)
    composeRule.onAllNodesWithText(comment, useUnmergedTree = true).onFirst().assertIsDisplayed()
  }

  /**
   * Opens the root reply composer by expanding the replies section and expanding the composer if it
   * starts collapsed.
   *
   * @param maxSwipes Maximum number of swipe attempts to bring the composer into the viewport.
   * @param timeoutMillis Timeout used for the final wait on the text field.
   */
  private fun openRootReplyComposerIfNeeded(
      maxSwipes: Int = 10,
      timeoutMillis: Long = EndToEndReviewFlowM3TestConstants.WAIT_MS,
  ) {
    fun exists(tag: String): Boolean =
        runCatching {
              composeRule
                  .onAllNodesWithTag(tag, useUnmergedTree = true)
                  .fetchSemanticsNodes()
                  .isNotEmpty()
            }
            .getOrDefault(false)

    repeat(maxSwipes) {
      if (exists(ReviewRepliesTestTags.ROOT_INLINE_TEXT_FIELD)) return

      if (exists(ReviewRepliesTestTags.ROOT_SEE_REPLIES)) {
        runCatching {
          composeRule
              .onNodeWithTag(ReviewRepliesTestTags.ROOT_SEE_REPLIES, useUnmergedTree = true)
              .performClick()
          composeRule.waitForIdle()
        }
      }

      if (exists(ReviewRepliesTestTags.ROOT_INLINE_COMPOSER) &&
          !exists(ReviewRepliesTestTags.ROOT_INLINE_TEXT_FIELD)) {
        runCatching {
          composeRule
              .onNodeWithTag(ReviewRepliesTestTags.ROOT_INLINE_COMPOSER, useUnmergedTree = true)
              .performClick()
          composeRule.waitForIdle()
        }
      }

      if (!exists(ReviewRepliesTestTags.ROOT_INLINE_TEXT_FIELD)) {
        runCatching {
          composeRule
              .onNodeWithTag(HuntCardScreenTestTags.HUNT_CARD_LIST, useUnmergedTree = true)
              .performTouchInput { swipeUp() }
          composeRule.waitForIdle()
        }
      }
    }

    waitForNodeWithTag(ReviewRepliesTestTags.ROOT_INLINE_TEXT_FIELD, timeoutMillis = timeoutMillis)
  }

  /**
   * Sends a reply as the hunt author through the inline root reply composer.
   *
   * @param reply Reply content to type and submit.
   */
  private fun sendReplyAsAuthor(reply: String) {
    openRootReplyComposerIfNeeded()

    replaceText(ReviewRepliesTestTags.ROOT_INLINE_TEXT_FIELD, reply)
    composeRule
        .onNodeWithTag(ReviewRepliesTestTags.ROOT_INLINE_SEND_BUTTON, useUnmergedTree = true)
        .performClick()
    composeRule.waitForIdle()
  }

  /**
   * Verifies the profile review rating stat card reflects the expected rating.
   *
   * @param expectedRating Rating value expected on the profile header.
   */
  private fun verifyProfileRating(expectedRating: Double) {
    forceNavigateBackToOverview()
    openProfileTab()

    val expectedLeft = String.format(Locale.US, ONE_DECIMAL_FORMAT, expectedRating)

    composeRule.waitUntil(timeoutMillis = EndToEndReviewFlowM3TestConstants.WAIT_MS) {
      runCatching {
            composeRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_REVIEW_RATING, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .any { node ->
                  val text =
                      node.config
                          .getOrNull(SemanticsProperties.Text)
                          ?.joinToString("") { it.text }
                          ?.replace(" ", "")
                          .orEmpty()

                  val parts = text.split("/")
                  val leftOk = parts.getOrNull(0) == expectedLeft
                  val right = parts.getOrNull(1).orEmpty()

                  leftOk &&
                      (right == MAX_RATING.toString() || right == MAX_RATING.toInt().toString())
                }
          }
          .getOrDefault(false)
    }

    val actual =
        composeRule
            .onNodeWithTag(ProfileTestTags.PROFILE_REVIEW_RATING, useUnmergedTree = true)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            ?.joinToString("") { it.text }
            .orEmpty()

    val expectedCandidates =
        listOf("$expectedLeft/${MAX_RATING.toString()}", "$expectedLeft/${MAX_RATING.toInt()}")

    if (expectedCandidates.none { it == actual }) {
      throw AssertionError("Expected one of $expectedCandidates but was '$actual'")
    }
  }

  /** Navigates to the profile tab and waits for the profile screen to be present. */
  private fun openProfileTab() {
    composeRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    waitForNodeWithTag(ProfileTestTags.PROFILE_SCREEN)
  }

  /**
   * Seeds a single hunt in the hunts repository and attaches it to the author profile.
   *
   * @return The created hunt instance.
   */
  private fun seedTestHuntInRepository(): Hunt = runBlocking {
    val newUid = huntsRepository.getNewUid()

    val start =
        Location(
            EndToEndReviewFlowM3TestConstants.START_LAT,
            EndToEndReviewFlowM3TestConstants.START_LNG,
            EndToEndReviewFlowM3TestConstants.START_NAME)

    val end =
        Location(
            EndToEndReviewFlowM3TestConstants.END_LAT,
            EndToEndReviewFlowM3TestConstants.END_LNG,
            EndToEndReviewFlowM3TestConstants.END_NAME)

    val hunt =
        Hunt(
            uid = newUid,
            start = start,
            end = end,
            middlePoints = emptyList(),
            status = HuntStatus.FUN,
            title = EndToEndReviewFlowM3TestConstants.HUNT_TITLE,
            description = EndToEndReviewFlowM3TestConstants.HUNT_DESCRIPTION,
            time = EndToEndReviewFlowM3TestConstants.HUNT_TIME_HOURS.toDouble(),
            distance = EndToEndReviewFlowM3TestConstants.HUNT_DISTANCE_KM.toDouble(),
            difficulty = Difficulty.EASY,
            authorId = testUserId,
            otherImagesUrls = emptyList(),
            mainImageUrl = "",
            reviewRate = 0.0)

    huntsRepository.addHunt(hunt)

    val profile = profileRepository.getProfile(testUserId)!!
    profile.myHunts.add(hunt)
    profileRepository.updateProfile(profile)

    hunt
  }

  /**
   * Adds a review as a distinct reviewer account by writing directly into the review repository.
   *
   * @param hunt Hunt being reviewed.
   * @return The created review instance.
   */
  private fun addReviewFromSecondaryAccount(hunt: Hunt): HuntReview = runBlocking {
    ensureReviewerProfileExists()

    val review =
        HuntReview(
            reviewId = reviewRepository.getNewUid(),
            authorId = EndToEndReviewFlowM3TestConstants.REVIEWER_USER_ID,
            huntId = hunt.uid,
            rating = EndToEndReviewFlowM3TestConstants.REVIEW_RATING.toDouble(),
            comment = EndToEndReviewFlowM3TestConstants.REVIEW_COMMENT,
            photos = emptyList())

    reviewRepository.addReviewHunt(review)
    review
  }

  /**
   * Creates the reviewer profile if it is missing from the profile repository.
   *
   * @param reviewerId Reviewer uid to ensure exists.
   */
  private suspend fun ensureReviewerProfileExists(
      reviewerId: String = EndToEndReviewFlowM3TestConstants.REVIEWER_USER_ID
  ) {
    if (profileRepository.getProfile(reviewerId) != null) return

    val profile =
        Profile(
            uid = reviewerId,
            author =
                Author(
                    hasCompletedOnboarding = true,
                    hasAcceptedTerms = true,
                    pseudonym = EndToEndReviewFlowM3TestConstants.REVIEWER_PSEUDONYM,
                    bio = EndToEndReviewFlowM3TestConstants.REVIEWER_BIO),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())

    profileRepository.createProfile(profile)
  }

  /**
   * Updates the hunt rating in the hunts repository.
   *
   * @param hunt Hunt to update.
   * @param rating Rating to apply.
   * @return The updated hunt instance.
   */
  private fun updateHuntRating(hunt: Hunt, rating: Double): Hunt = runBlocking {
    val updated = hunt.copy(reviewRate = rating)

    huntsRepository.editHunt(
        hunt.uid,
        updated,
        mainImageUri = null,
        addedOtherImages = emptyList(),
        removedOtherImages = emptyList(),
        removedMainImageUrl = null)

    updated
  }

  /** Refreshes the overview list via swipe-to-refresh and waits for overview readiness. */
  private fun refreshOverviewList() {
    swipeToRefreshOverview()
    waitForOverviewRoot()
  }

  /**
   * Types a query into the overview search bar and triggers IME action.
   *
   * @param title Search query to enter.
   */
  private fun searchForHunt(title: String) {
    replaceText(OverviewScreenTestTags.SEARCH_BAR, title)
    performImeOn(OverviewScreenTestTags.SEARCH_BAR)
    waitForText(title)
  }

  /**
   * Opens the first hunt card matching the provided title from the overview list.
   *
   * @param title Title to match in the overview list.
   */
  private fun openHuntFromOverview(title: String) {
    composeRule
        .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
        .filter(hasAnyDescendant(hasText(title)))
        .onFirst()
        .performClick()

    waitForNodeWithTag(NavigationTestTags.HUNTCARD_SCREEN)
  }

  /**
   * Waits for the overview root to be present.
   *
   * @param failOnTimeout Controls whether a timeout throws.
   * @return True if the node appeared within the timeout; false otherwise.
   */
  private fun waitForOverviewRoot(failOnTimeout: Boolean = true): Boolean =
      waitForNodeWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN, failOnTimeout = failOnTimeout)

  /** Performs swipe-to-refresh on the overview list container. */
  private fun swipeToRefreshOverview() {
    composeRule.onNodeWithTag(OverviewScreenTestTags.HUNT_LIST).performTouchInput { swipeDown() }
    composeRule.waitForIdle()
  }

  /**
   * Waits until a node with a given tag exists in the semantics tree.
   *
   * @param tag Test tag to wait for.
   * @param timeoutMillis Maximum time to wait.
   * @param failOnTimeout Controls whether a timeout throws.
   * @return True if the node appeared within the timeout; false otherwise.
   */
  private fun waitForNodeWithTag(
      tag: String,
      timeoutMillis: Long = EndToEndReviewFlowM3TestConstants.WAIT_MS,
      failOnTimeout: Boolean = true,
  ): Boolean {
    return try {
      composeRule.waitUntil(timeoutMillis) {
        runCatching {
              composeRule
                  .onAllNodesWithTag(tag, useUnmergedTree = true)
                  .fetchSemanticsNodes()
                  .isNotEmpty()
            }
            .getOrDefault(false)
      }
      true
    } catch (e: ComposeTimeoutException) {
      if (failOnTimeout) throw e else false
    }
  }

  /**
   * Waits until a text node exists in the semantics tree.
   *
   * @param text Text to wait for.
   * @param timeoutMillis Maximum time to wait.
   */
  private fun waitForText(
      text: String,
      timeoutMillis: Long = EndToEndReviewFlowM3TestConstants.WAIT_MS,
  ) {
    composeRule.waitUntil(timeoutMillis) {
      runCatching {
            composeRule
                .onAllNodesWithText(text, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
          }
          .getOrDefault(false)
    }
  }

  /** Navigates back to overview and selects the overview tab to normalize navigation state. */
  private fun forceNavigateBackToOverview() {
    composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
  }

  /**
   * Finds an editable semantics node associated with a tag by checking direct and descendant
   * set-text actions.
   *
   * @param tag Test tag associated with an editable field.
   * @return A semantics interaction targeting a node that supports set-text actions.
   */
  private fun findEditableNode(tag: String): SemanticsNodeInteraction {
    composeRule.onNodeWithTag(tag, useUnmergedTree = true).performClick()

    runCatching {
      val direct =
          composeRule.onNode(hasTestTag(tag).and(hasSetTextAction()), useUnmergedTree = true)
      direct.fetchSemanticsNode()
      return direct
    }

    runCatching {
      val nested =
          composeRule.onNode(
              hasSetTextAction().and(hasAnyAncestor(hasTestTag(tag))), useUnmergedTree = true)
      nested.fetchSemanticsNode()
      return nested
    }

    throw AssertionError("No editable node found for tag '$tag'.")
  }

  /**
   * Clears the current text and inputs a replacement into a tagged field.
   *
   * @param tag Test tag associated with the field.
   * @param value Replacement text.
   */
  private fun replaceText(tag: String, value: String) {
    val input = findEditableNode(tag)
    input.performTextClearance()
    input.performTextInput(value)
  }

  /**
   * Performs IME action on the field associated with the provided tag.
   *
   * @param tag Test tag associated with the field.
   */
  private fun performImeOn(tag: String) {
    findEditableNode(tag).performImeAction()
  }
}
