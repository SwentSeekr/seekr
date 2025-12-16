package com.swentseekr.seekr.ui.huntCardScreen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.FakeReviewHuntViewModel
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.model.profile.createReview
import com.swentseekr.seekr.ui.components.HuntCardScreen
import com.swentseekr.seekr.ui.components.HuntCardScreenStrings
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags.REVIEW_COMMENT
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlin.apply
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HuntCardScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createFakeHunt() =
      Hunt(
          uid = HuntCardScreenConstantStrings.TEST_HUNT_ID,
          start =
              Location(
                  HuntCardScreenConstantNumbers.LOCATION_LAT_1,
                  HuntCardScreenConstantNumbers.LOCATION_LNG_1,
                  HuntCardScreenConstantStrings.LOCATION_NAME_1),
          end =
              Location(
                  HuntCardScreenConstantNumbers.LOCATION_LAT_2,
                  HuntCardScreenConstantNumbers.LOCATION_LNG_2,
                  HuntCardScreenConstantStrings.LOCATION_NAME_2),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = HuntCardScreenConstantStrings.TITLE,
          description = HuntCardScreenConstantStrings.DESCRIPTION,
          time = HuntCardScreenConstantNumbers.TIME_HOURS,
          distance = HuntCardScreenConstantNumbers.DISTANCE_KM,
          difficulty = Difficulty.DIFFICULT,
          authorId = HuntCardScreenConstantStrings.AUTHOR_ID,
          otherImagesUrls = emptyList(),
          mainImageUrl = "",
          reviewRate = HuntCardScreenConstantNumbers.REVIEW_RATE)

  private fun setHuntContent(
      hunt: Hunt,
      onGoBack: () -> Unit = {},
      beginHunt: () -> Unit = {},
      addReview: () -> Unit = {},
  ) {
    composeTestRule.setContent {
      HuntCardScreen(
          huntId = hunt.uid,
          huntCardViewModel = FakeHuntCardViewModel(hunt),
          onGoBack = onGoBack,
          beginHunt = beginHunt,
          addReview = addReview,
          navController = rememberNavController())
    }
  }

  @Test
  fun testAllUIElementsAreDisplayed() {
    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TEST_HUNT_ID,
          huntCardViewModel = FakeHuntCardViewModel(createFakeHunt()),
          onGoBack = {},
          beginHunt = {},
          addReview = {},
          navController = rememberNavController())
    }

    // Verifies the presence of principal UI elements
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.GO_BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.TITLE_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.AUTHOR_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE_PAGER).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.MAP_CONTAINER).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.HUNT_CARD_LIST)
        .performScrollToNode(hasText(HuntCardScreenConstantStrings.ADD_REVIEW_LABEL))

    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.REVIEW_BUTTON).assertIsDisplayed()
  }

  @Test
  fun testButtonsTriggerCallbacks() {
    var goBackClicked = false
    var beginClicked = false
    var reviewClicked = false

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TEST_HUNT_ID,
          huntCardViewModel = FakeHuntCardViewModel(createFakeHunt()),
          onGoBack = { goBackClicked = true },
          beginHunt = { beginClicked = true },
          addReview = { reviewClicked = true },
          navController = rememberNavController())
    }

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.HUNT_CARD_LIST)
        .performScrollToNode(hasTestTag(HuntCardScreenTestTags.REVIEW_BUTTON))

    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.GO_BACK_BUTTON).performClick()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.REVIEW_BUTTON).performClick()

    assertTrue(goBackClicked)
    assertTrue(beginClicked)
    assertTrue(reviewClicked)
  }

  @Test
  fun testLikeButtonTogglesState() {
    val fakeVm = FakeHuntCardViewModel(createFakeHunt())

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TEST_HUNT_ID,
          huntCardViewModel = fakeVm,
          navController = rememberNavController())
    }

    // Initially: not liked
    assertTrue(!fakeVm.uiState.value.isLiked)

    // Click like
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.LIKE_BUTTON).performClick()

    // ViewModel should now show liked = true
    assertTrue(fakeVm.uiState.value.isLiked)

    // Click again → unlike
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.LIKE_BUTTON).performClick()

    assertTrue(!fakeVm.uiState.value.isLiked)
  }

  @Test
  fun testReviewsAreDisplayed() {
    val fakeVm = FakeHuntCardViewModel(createFakeHunt())
    val fakeReviewVm = FakeReviewHuntViewModel()

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TEST_HUNT_ID,
          huntCardViewModel = fakeVm,
          reviewViewModel = fakeReviewVm,
          navController = rememberNavController())
    }

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.HUNT_CARD_LIST)
        .performScrollToNode(hasText(HuntCardScreenConstantStrings.ADD_REVIEW_LABEL))
    composeTestRule.onAllNodesWithTag(HuntCardScreenTestTags.REVIEW_CARD).onFirst().assertExists()
  }

  @Test
  fun testAddReviewButtonShownForOtherUsers() {
    val fakeVm =
        FakeHuntCardViewModel(
            createFakeHunt().copy(authorId = HuntCardScreenConstantStrings.AUTHOR_ID))

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TEST_HUNT_ID,
          huntCardViewModel = fakeVm,
          navController = rememberNavController())
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.HUNT_CARD_LIST)
        .performScrollToNode(hasText(HuntCardScreenConstantStrings.ADD_REVIEW_LABEL))

    composeTestRule
        .onNodeWithText(HuntCardScreenConstantStrings.ADD_REVIEW_LABEL)
        .assertIsDisplayed()
  }

  @Test
  fun testMapIsVisible() {
    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TEST_HUNT_ID,
          huntCardViewModel = FakeHuntCardViewModel(createFakeHunt()),
          navController = rememberNavController())
    }

    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.MAP_CONTAINER).assertIsDisplayed()
  }

  @Test
  fun huntCardScreen_showsDotsWhenMultipleImages() {
    val huntWithImages =
        createFakeHunt()
            .copy(
                mainImageUrl = HuntCardScreenConstantStrings.MAIN_IMAGE_URL,
                otherImagesUrls =
                    listOf(
                        HuntCardScreenConstantStrings.OTHER_IMAGE_URL_2,
                        HuntCardScreenConstantStrings.OTHER_IMAGE_URL_3))

    setHuntContent(hunt = huntWithImages)

    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE_PAGER).assertExists()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE_INDICATOR_ROW).assertExists()

    repeat(3) { index ->
      composeTestRule
          .onNodeWithTag(HuntCardScreenTestTags.IMAGE_INDICATOR_DOT_PREFIX + index)
          .assertExists()
    }
  }

  @Test
  fun huntCardScreen_noDotsWhenSingleImage() {
    val singleImageHunt =
        createFakeHunt()
            .copy(
                mainImageUrl = HuntCardScreenConstantStrings.SINGLE_IMAGE_URL,
                otherImagesUrls = emptyList(),
            )

    setHuntContent(hunt = singleImageHunt)

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.IMAGE_CAROUSEL_CONTAINER)
        .assertIsDisplayed()

    // Indicator row should not exist
    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.IMAGE_INDICATOR_ROW, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun huntCardScreen_tapCenterImageOpensFullscreen() {
    val huntWithImage =
        createFakeHunt()
            .copy(
                mainImageUrl = HuntCardScreenConstantStrings.FULLSCREEN_IMAGE_URL,
                otherImagesUrls = emptyList(),
            )

    setHuntContent(hunt = huntWithImage)

    // Click center page (index 0)
    composeTestRule
        .onNodeWithTag(
            HuntCardScreenTestTags.IMAGE_PAGE_PREFIX +
                HuntCardScreenConstantNumbers.FIRST_IMAGE_INDEX)
        .assertIsDisplayed()
        .performClick()

    // Full-screen dialog appears
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE_FULLSCREEN).assertIsDisplayed()
  }

  @Test
  fun testNoReviewsMessageIsDisplayed() {
    val hunt = createFakeHunt()

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = hunt.uid,
          huntCardViewModel = FakeHuntCardViewModelEmptyReview(hunt),
          navController = rememberNavController())
    }

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.HUNT_CARD_LIST)
        .performScrollToNode(hasText(HuntCardScreenStrings.NO_REVIEW))

    // The "No Reviews" text should be visible
    composeTestRule.onNodeWithText(HuntCardScreenStrings.NO_REVIEW).assertExists()
  }

  @Test
  fun likeButton_reflectsLikedHuntsCacheInitially() {
    val fakeVm = FakeHuntCardViewModel(createFakeHunt())
    fakeVm.setLiked(true)

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TEST_HUNT_ID,
          huntCardViewModel = fakeVm,
          navController = rememberNavController())
    }

    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.LIKE_BUTTON).assertExists()
  }

  @Test
  fun likeButton_togglesStateAndUi_onClick() {
    val fakeVm = FakeHuntCardViewModel(createFakeHunt())

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = "testHunt", huntCardViewModel = fakeVm, navController = rememberNavController())
    }

    val likeButton = composeTestRule.onNodeWithTag(HuntCardScreenTestTags.LIKE_BUTTON)

    assertFalse(fakeVm.uiState.value.isLiked)

    likeButton.performClick()
    composeTestRule.waitForIdle()
    assertTrue(fakeVm.uiState.value.isLiked)

    likeButton.performClick()
    composeTestRule.waitForIdle()
    assertFalse(fakeVm.uiState.value.isLiked)
  }

  @Test
  fun likeButton_updatesUiBasedOnLikedHuntsCache() {
    val fakeVm = FakeHuntCardViewModel(createFakeHunt())
    fakeVm.setLikedHunts(setOf(HuntCardScreenConstantStrings.TEST_HUNT_ID))

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TEST_HUNT_ID,
          huntCardViewModel = fakeVm,
          navController = rememberNavController())
    }

    val likeButton = composeTestRule.onNodeWithTag(HuntCardScreenTestTags.LIKE_BUTTON)

    assertTrue(fakeVm.uiState.value.isLiked)

    likeButton.performClick()
    composeTestRule.waitForIdle()
    assertTrue(!fakeVm.uiState.value.isLiked)
  }

  @Test
  fun launchedEffects_loadCurrentUserAndHunt() {
    val fakeVm = FakeHuntCardViewModel(createFakeHunt())

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TEST_HUNT_ID,
          huntCardViewModel = fakeVm,
          navController = rememberNavController())
    }

    composeTestRule.waitForIdle()

    assertTrue(fakeVm.uiState.value.currentUserId != null)
    assertTrue(fakeVm.uiState.value.hunt?.uid == HuntCardScreenConstantStrings.TEST_HUNT_ID)
  }

  @Test
  fun addReviewButton_isNotShown_whenCurrentUserAlreadyReviewed() {
    // Given: a hunt where the current user is NOT the author
    val hunt = createFakeHunt().copy(authorId = HuntCardScreenConstantStrings.OTHER_AUTHOR_ID)

    // Fake VM where current user (set by loadCurrentUserID) has already reviewed the hunt
    val fakeVm =
        FakeHuntCardViewModel(hunt).apply {
          // FakeHuntCardViewModel.loadCurrentUserID() sets currentUserId = "fakeUser123"
          // so we create a review with that same authorId
          setReviewsForTest(
              listOf(
                  com.swentseekr.seekr.model.hunt.HuntReview(
                      reviewId = "review-1",
                      authorId = "fakeUser123",
                      huntId = hunt.uid,
                      rating = 4.0,
                      comment = "Already reviewed",
                      photos = emptyList())))
        }

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = hunt.uid, huntCardViewModel = fakeVm, navController = rememberNavController())
    }

    composeTestRule.waitForIdle()

    // Then: the "Add review" button should NOT be in the tree at all
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.REVIEW_BUTTON).assertDoesNotExist()
  }

  @Test
  fun reviewWithEmptyCommentDoesNotShowCommentText() {
    val hunt = createHunt(uid = "hunt1", title = "Test Hunt")
    val review = createReview(comment = "")

    val viewModel =
        FakeHuntCardViewModel(hunt).apply {
          freezeReviews = true
          setReviewsForTest(listOf(review))
          setCurrentUserIdForTest("user1")
        }

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = hunt.uid, huntCardViewModel = viewModel, navController = rememberNavController())
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.HUNT_CARD_LIST)
        .performScrollToNode(hasTestTag(HuntCardScreenTestTags.REVIEW_CARD))

    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.REVIEW_CARD).assertIsDisplayed()

    composeTestRule.onNodeWithTag(REVIEW_COMMENT).assertDoesNotExist()
  }

  @Test
  fun huntCardScreeLoadingStateShowsCircularProgress() {
    val viewModel = FakeHuntCardViewModel(hunt = null)
    composeTestRule.setContent {
      HuntCardScreen(
          huntId = "nonExistingHunt",
          huntCardViewModel = viewModel,
          navController = rememberNavController())
    }

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.CIRCULAR_PROGRESS_INDICATOR)
        .assertIsDisplayed()
  }

  @Test
  fun reviewWithNoPhotosDoesNotShowSeePicturesButton() {
    val hunt = createHunt(uid = "hunt1", title = "Test Hunt")
    val review = createReview(photos = emptyList())

    val viewModel =
        FakeHuntCardViewModel(hunt).apply {
          freezeReviews = true
          setReviewsForTest(listOf(review))
        }

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = hunt.uid, huntCardViewModel = viewModel, navController = rememberNavController())
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.HUNT_CARD_LIST)
        .performScrollToNode(hasTestTag(HuntCardScreenTestTags.REVIEW_CARD))

    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.SEE_PICTURES_BUTTON).assertDoesNotExist()
  }

  @Test
  fun emptyReviewsShowEmptyState() {
    val hunt = createHunt(uid = "hunt1", title = "Test Hunt")
    val viewModel = FakeHuntCardViewModel(hunt).apply { setReviewsForTest(emptyList()) }

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = hunt.uid, huntCardViewModel = viewModel, navController = rememberNavController())
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.HUNT_CARD_LIST)
        .performScrollToNode(hasTestTag(HuntCardScreenTestTags.MODERN_EMPTY_REVIEWS_STATE))

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.MODERN_EMPTY_REVIEWS_STATE)
        .assertIsDisplayed()
  }

  @Test
  fun nonEmptyReviewsShowReviewCardsWithAuthorProfiles() {
    val hunt = createHunt(uid = "hunt1", title = "Test Hunt")
    val review = createReview(authorId = "author1", comment = "Great hunt!")
    val viewModel =
        FakeHuntCardViewModel(hunt).apply {
          setCurrentUserIdForTest("author1")
          setReviewsForTest(listOf(review))
        }

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = hunt.uid, huntCardViewModel = viewModel, navController = rememberNavController())
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.HUNT_CARD_LIST)
        .performScrollToNode(hasTestTag(HuntCardScreenTestTags.REVIEW_CARD))

    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.REVIEW_CARD).assertIsDisplayed()

    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.HUNT_CARD_LIST)
  }

  @Test
  fun reviewWithPhotosShowsSeePicturesButton() {
    val hunt = createHunt(uid = "hunt1", title = "Test Hunt")
    val review = createReview(photos = listOf("photo1.jpg", "photo2.jpg"))

    val viewModel =
        FakeHuntCardViewModel(hunt).apply {
          freezeReviews = true
          setReviewsForTest(listOf(review))
        }

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = hunt.uid, huntCardViewModel = viewModel, navController = rememberNavController())
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.HUNT_CARD_LIST)
        .performScrollToNode(hasTestTag(HuntCardScreenTestTags.REVIEW_CARD))

    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.SEE_PICTURES_BUTTON).assertIsDisplayed()
  }
}
