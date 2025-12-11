package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation.compose.rememberNavController
import com.swentseekr.seekr.model.profile.createReview
import com.swentseekr.seekr.model.profile.mockProfileData
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

  @Test
  fun testProfileReviewsScreen_displaysAllElements() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      ProfileReviewsScreen(
          userId = sampleProfile.uid,
          profileViewModel = ProfileViewModel(),
          onGoBack = {},
          navController = navController,
          testProfile = sampleProfile,
          testReviews = sampleReviews)
    }

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
          navController = navController,
          testProfile = sampleProfile,
          testReviews = sampleReviews)
    }

    composeTestRule.onNodeWithTag(ProfileReviewsTestTags.BACK_BUTTON).performClick()
    assertTrue(backClicked)
  }

  @Test
  fun testNoReviewsMessageDisplayedWhenEmpty() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      ProfileReviewsScreen(
          userId = sampleProfile.uid,
          profileViewModel = ProfileViewModel(),
          onGoBack = {},
          navController = navController,
          testProfile = sampleProfile,
          testReviews = emptyList())
    }

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
  fun testRatingTextSingularReview() {
    val singleReview =
        listOf(createReview(reviewId = "review1", huntId = sampleProfile.myHunts[0].uid))

    val testProfile = sampleProfile.copy(author = sampleProfile.author.copy(reviewRate = 4.5))

    composeTestRule.setContent {
      val navController = rememberNavController()
      ProfileReviewsScreen(
          userId = testProfile.uid,
          profileViewModel = ProfileViewModel(),
          onGoBack = {},
          navController = navController,
          testProfile = testProfile,
          testReviews = singleReview)
    }

    composeTestRule
        .onNodeWithTag(ProfileReviewsTestTags.RATING_TEXT)
        .assertIsDisplayed()
        .assertTextContains("4.5/5.0 - 1 review")
  }

  @Test
  fun testRatingTextMultipleReviews() {
    val testProfile = sampleProfile.copy(author = sampleProfile.author.copy(reviewRate = 4.2))

    composeTestRule.setContent {
      val navController = rememberNavController()
      ProfileReviewsScreen(
          userId = testProfile.uid,
          profileViewModel = ProfileViewModel(),
          onGoBack = {},
          navController = navController,
          testProfile = testProfile,
          testReviews = sampleReviews)
    }

    composeTestRule
        .onNodeWithTag(ProfileReviewsTestTags.RATING_TEXT)
        .assertIsDisplayed()
        .assertTextContains("4.2/5.0 - 2 reviews")
  }
  /*@Test
  fun testHuntHeaderClickableNavigates() {
      lateinit var navController: NavHostController

      val fakeHuntsRepository = HuntsRepositoryLocal()
      val fakeReviewRepository = HuntReviewRepositoryLocal()
      val fakeProfileRepository = ProfileRepositoryLocal()
      val fakeImageRepository = ReviewImageRepositoryLocal()

      val hunt = sampleProfile.myHunts[0]
      runBlocking {
          fakeHuntsRepository.addHunt(hunt)
          sampleReviews.forEach { review ->
              fakeReviewRepository.addReviewHunt(review)
          }
      }

      val reviewHuntViewModel = ReviewHuntViewModel(
          fakeHuntsRepository,
          fakeReviewRepository,
          fakeProfileRepository,
          fakeImageRepository,
          dispatcher = Dispatchers.Main
      )

      composeTestRule.setContent {
          navController = rememberNavController()
          ProfileReviewsScreen(
              userId = sampleProfile.uid,
              profileViewModel = ProfileViewModel(),
              onGoBack = {},
              navController = navController,
              testProfile = sampleProfile,
              testReviews = sampleReviews,
              reviewHuntViewModel = reviewHuntViewModel
          )
      }

      val huntId = hunt.uid

      composeTestRule.waitForIdle()

      composeTestRule.mainClock.advanceTimeBy(1000)
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag(ProfileReviewsTestTags.REVIEWS_LIST)
          .performScrollToNode(hasTestTag("hunt_header_$huntId"))

      composeTestRule.onNodeWithTag("hunt_header_$huntId")
          .assertIsDisplayed()
          .performClick()

      assertTrue(navController.currentBackStackEntry?.destination?.route?.contains(huntId) == true)
  }*/

  @Test
  fun testDividerItemsAreDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      ProfileReviewsScreen(
          userId = sampleProfile.uid,
          profileViewModel = ProfileViewModel(),
          navController = navController,
          testProfile = sampleProfile,
          testReviews = sampleReviews)
    }

    sampleProfile.myHunts.forEach { hunt ->
      composeTestRule.onNodeWithTag("divider_${hunt.uid}").assertIsDisplayed()
    }
  }

  @Test
  fun testDividerItemsRendered() {
    composeTestRule.setContent {
      ProfileReviewsScreen(
          userId = sampleProfile.uid,
          profileViewModel = ProfileViewModel(),
          onGoBack = {},
          navController = rememberNavController(),
          testProfile = sampleProfile,
          testReviews = sampleReviews)
    }

    val huntId = sampleProfile.myHunts[0].uid
    composeTestRule.onNode(hasTestTag("divider_$huntId")).assertIsDisplayed()
  }

  /*@Test
  fun testLoadHuntAndAuthorCalledForEachReview() {
      val reviewHuntViewModel = object : ReviewHuntViewModel(
          HuntsRepositoryLocal(),
          HuntReviewRepositoryLocal(),
          ProfileRepositoryLocal(),
          ReviewImageRepositoryLocal()
      ) {
          val loadedHunts = mutableListOf<String>()
          val loadedAuthors = mutableListOf<String>()

          override fun loadHunt(huntId: String) {
              loadedHunts.add(huntId)
          }

          override fun loadAuthorProfile(userId: String) {
              loadedAuthors.add(userId)
          }
      }

      composeTestRule.setContent {
          ProfileReviewsScreen(
              userId = sampleProfile.uid,
              profileViewModel = ProfileViewModel(),
              reviewHuntViewModel = reviewHuntViewModel,
              onGoBack = {},
              navController = rememberNavController(),
              testProfile = sampleProfile,
              testReviews = sampleReviews
          )
      }

      composeTestRule.waitForIdle()

      sampleReviews.map { it.huntId }.distinct().forEach { huntId ->
          assertTrue(reviewHuntViewModel.loadedHunts.contains(huntId))
      }
      sampleReviews.forEach { review ->
          assertTrue(reviewHuntViewModel.loadedAuthors.contains(review.authorId))
      }

  }*/
}
