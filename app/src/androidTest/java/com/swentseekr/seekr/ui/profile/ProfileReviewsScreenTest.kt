package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation.compose.rememberNavController
import com.swentseekr.seekr.FakeReviewHuntViewModel
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryLocal
import com.swentseekr.seekr.model.hunt.HuntsRepositoryLocal
import com.swentseekr.seekr.model.hunt.ReviewImageRepositoryLocal
import com.swentseekr.seekr.model.profile.ProfileRepositoryLocal
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.model.profile.createReview
import com.swentseekr.seekr.model.profile.mockProfileData
import com.swentseekr.seekr.model.profile.sampleProfile
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import com.swentseekr.seekr.ui.components.ModernReviewCard
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileReviewsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleProfile = mockProfileData()

  private val sampleReviews =
      listOf(
          createReview(reviewId = "review1", huntId = sampleProfile.myHunts[0].uid),
          createReview(reviewId = "review2", huntId = sampleProfile.myHunts[0].uid))

  private fun setContent(
      profile: Profile = sampleProfile,
      reviews: List<HuntReview> = sampleReviews,
      reviewHuntViewModel: ReviewHuntViewModel = ReviewHuntViewModel(),
      onGoBack: () -> Unit = {}
  ) {
    composeTestRule.setContent {
      val navController = rememberNavController()
      ProfileReviewsScreen(
          userId = profile.uid,
          profileViewModel = ProfileViewModel(),
          reviewHuntViewModel = reviewHuntViewModel,
          onGoBack = onGoBack,
          editReview = {},
          navController = navController,
          testProfile = profile,
          testReviews = reviews,
          testHuntsById = profile.myHunts.associateBy { it.uid })
    }
  }

  @Test
  fun testProfileReviewsScreen_displaysAllElements() {
    setContent()
    composeTestRule.onNodeWithTag(ProfileReviewsTestTags.TOP_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileReviewsTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileReviewsTestTags.RATING_SUMMARY).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileReviewsTestTags.RATING_TEXT).assertIsDisplayed()
    sampleReviews.forEach { review ->
      composeTestRule
          .onNodeWithTag(ProfileReviewsTestTags.REVIEWS_LIST)
          .performScrollToNode(hasTestTag(ProfileReviewsTestTags.reviewCardTag(review.reviewId)))

      composeTestRule
          .onNodeWithTag(ProfileReviewsTestTags.reviewCardTag(review.reviewId))
          .assertIsDisplayed()
    }
  }

  @Test
  fun testBackButton_clickable() {
    var backClicked = false
    composeTestRule.setContent {
      val navController = rememberNavController()
      ProfileReviewsScreen(
          userId = sampleProfile.uid,
          profileViewModel = ProfileViewModel(),
          onGoBack = { backClicked = true },
          editReview = {},
          navController = navController,
          testProfile = sampleProfile,
          testReviews = sampleReviews)
    }

    composeTestRule.onNodeWithTag(ProfileReviewsTestTags.BACK_BUTTON).performClick()
    assertTrue(backClicked)
  }

  @Test
  fun testNoReviewsMessageDisplayedWhenEmpty() {
    setContent(reviews = emptyList())
    composeTestRule.onNodeWithTag(ProfileReviewsTestTags.EMPTY_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun testLoadingState_whenProfileNull() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      ProfileReviewsScreen(
          userId = "someUserId",
          profileViewModel = ProfileViewModel(),
          onGoBack = {},
          editReview = {},
          navController = navController,
          testProfile = null,
          testReviews = emptyList())
    }
    composeTestRule.onNodeWithTag(ProfileReviewsTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileReviewsTestTags.LOADING).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileReviewsTestTags.RATING_SUMMARY).assertDoesNotExist()
    composeTestRule.onNodeWithTag(ProfileReviewsTestTags.REVIEWS_LIST).assertDoesNotExist()
  }

  @Test
  fun testRatingText_singularReview() {
    val singularReviewProfile =
        sampleProfile.copy(author = sampleProfile.author.copy(reviewRate = 4.5))

    setContent(profile = singularReviewProfile, reviews = listOf(sampleReviews[0]))
    composeTestRule
        .onNodeWithTag(ProfileReviewsTestTags.RATING_TEXT)
        .assertTextContains("4.5/5.0 - 1 review")
  }

  @Test
  fun testRatingText_multipleReviews() {
    val multipleReviewsProfile =
        sampleProfile.copy(author = sampleProfile.author.copy(reviewRate = 4.2))

    setContent(profile = multipleReviewsProfile, reviews = sampleReviews)
    composeTestRule
        .onNodeWithTag(ProfileReviewsTestTags.RATING_TEXT)
        .assertTextContains("4.2/5.0 - 2 reviews")
  }

  @Test
  fun testDividerAndHuntHeadersDisplayed() {
    val hunt1 = createHunt(uid = "hunt1", title = "First Hunt")
    val hunt2 = createHunt(uid = "hunt2", title = "Second Hunt")
    val profile = sampleProfile(myHunts = listOf(hunt1, hunt2), uid = "user123")
    val reviews =
        listOf(
            createReview(reviewId = "review1", huntId = hunt1.uid),
            createReview(reviewId = "review2", huntId = hunt2.uid))
    setContent(profile = profile, reviews = reviews)

    listOf(hunt1, hunt2).forEach { hunt ->
      composeTestRule
          .onNodeWithTag(ProfileReviewsTestTags.REVIEWS_LIST)
          .performScrollToNode(hasTestTag("${ProfileTestsConstants.HUNT_HEADER}${hunt.uid}"))
      composeTestRule
          .onNodeWithTag("${ProfileTestsConstants.HUNT_HEADER}${hunt.uid}")
          .assertIsDisplayed()
      composeTestRule
          .onNode(hasTestTag("${ProfileTestsConstants.DIVIDER}${hunt.uid}"))
          .assertIsDisplayed()
    }
  }

  @Test
  fun testLoadHuntAndAuthorCalledForEachReview() {
    val loadedHunts = mutableListOf<String>()
    val loadedAuthors = mutableListOf<String>()
    val fakeViewModel =
        object :
            ReviewHuntViewModel(
                HuntsRepositoryLocal(),
                HuntReviewRepositoryLocal(),
                ProfileRepositoryLocal(),
                ReviewImageRepositoryLocal()) {
          override fun loadHunt(huntId: String) {
            loadedHunts.add(huntId)
          }

          override fun loadAuthorProfile(userId: String) {
            loadedAuthors.add(userId)
          }
        }

    setContent(reviewHuntViewModel = fakeViewModel)

    composeTestRule.waitForIdle()

    sampleReviews.map { it.huntId }.distinct().forEach { assertTrue(loadedHunts.contains(it)) }
    sampleReviews.forEach { assertTrue(loadedAuthors.contains(it.authorId)) }
  }

  @Test
  fun testDeleteReviewCallback() {
    setContent()
    val firstReviewId = sampleReviews[0].reviewId
    composeTestRule
        .onNodeWithTag(ProfileReviewsTestTags.reviewCardTag(firstReviewId))
        .assertIsDisplayed()
        .performClick()
  }

  @Test
  fun testHuntHeaderDisplaysWhenHuntIsAvailable() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      ProfileReviewsScreen(
          userId = sampleProfile.uid,
          profileViewModel = ProfileViewModel(),
          onGoBack = {},
          editReview = {},
          navController = navController,
          testProfile = sampleProfile,
          testReviews = sampleReviews)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(ProfileReviewsTestTags.REVIEWS_LIST).assertIsDisplayed()

    sampleReviews.forEach { review ->
      composeTestRule
          .onNodeWithTag(ProfileReviewsTestTags.REVIEWS_LIST)
          .performScrollToNode(hasTestTag(ProfileReviewsTestTags.reviewCardTag(review.reviewId)))

      composeTestRule
          .onNodeWithTag(ProfileReviewsTestTags.reviewCardTag(review.reviewId))
          .assertIsDisplayed()
    }
  }

  @Test
  fun testHuntHeaderWithNullHuntDoesNotRender() {
    val reviewWithUnknownHunt = listOf(createReview(reviewId = "reviewX", huntId = "unknown_hunt"))

    setContent(reviews = reviewWithUnknownHunt)

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(ProfileReviewsTestTags.reviewCardTag("reviewX"))
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(ProfileTestsConstants.UNKNOWN).assertDoesNotExist()
  }

  @Test
  fun reviewCard_shows_edit_menu_for_current_user() {
    val currentUserId = "user123"
    val review =
        HuntReview(
            reviewId = "review1",
            authorId = currentUserId,
            huntId = "hunt1",
            comment = "Compétent",
            rating = 5.0,
            photos = emptyList())

    composeTestRule.setContent {
      ModernReviewCard(
          review = review,
          reviewHuntViewModel = FakeReviewHuntViewModel(),
          currentUserId = currentUserId,
          navController = rememberNavController(),
          onDeleteReview = {},
          onEdit = {},
          authorProfile = null)
    }

    // Open the menu
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.DOTBUTOON).performClick()

    // Check Edit button is displayed
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
  }
}
