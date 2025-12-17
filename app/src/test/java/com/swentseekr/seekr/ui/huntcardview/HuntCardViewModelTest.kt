package com.swentseekr.seekr.ui.huntcardview

import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryLocal
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepositoryLocal
import com.swentseekr.seekr.model.hunt.ReviewImageRepositoryLocal
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.model.profile.ProfileRepositoryLocal
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModelTestConstantsString.TEST_AUTHOR_ID
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModelTestConstantsString.TEST_HUNT_ID
import com.swentseekr.seekr.ui.profile.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for HuntCardViewModel.
 *
 * Covers hunt loading, likes, completion state, reviews handling, author profile loading, and error
 * scenarios.
 */
class HuntCardViewModelTest {
  private lateinit var viewModel: HuntCardViewModel
  private lateinit var fakeRepository: HuntsRepositoryLocal
  private lateinit var fakeRevRepository: HuntReviewRepositoryLocal
  private lateinit var fakeProRepository: ProfileRepositoryLocal
  private lateinit var fakeImageReviewRepository: ReviewImageRepositoryLocal
  private val testDispatcher = StandardTestDispatcher()

  private val testHunt =
      Hunt(
          uid = TEST_HUNT_ID,
          start =
              Location(
                  HuntCardViewModelTestConstantsNumeric.LOCATION_1,
                  HuntCardViewModelTestConstantsNumeric.LOCATION_2,
                  HuntCardViewModelTestConstantsString.START_LABEL),
          end =
              Location(
                  HuntCardViewModelTestConstantsNumeric.LOCATION_3,
                  HuntCardViewModelTestConstantsNumeric.LOCATION_4,
                  HuntCardViewModelTestConstantsString.END_LABEL),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = HuntCardViewModelTestConstantsString.TEST_TITLE,
          description = HuntCardViewModelTestConstantsString.TEST_DESCRIPTION,
          time = HuntCardViewModelTestConstantsNumeric.TIME_HOURS,
          distance = HuntCardViewModelTestConstantsNumeric.DISTANCE_KM,
          difficulty = Difficulty.EASY,
          authorId = TEST_AUTHOR_ID,
          mainImageUrl = "",
          reviewRate = HuntCardViewModelTestConstantsNumeric.REVIEW_RATE)

  @Before
  fun setUp() = runTest {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = HuntsRepositoryLocal()
    fakeRepository.addHunt(testHunt)
    fakeRevRepository = HuntReviewRepositoryLocal()
    fakeProRepository = ProfileRepositoryLocal()
    fakeImageReviewRepository = ReviewImageRepositoryLocal()

    viewModel =
        HuntCardViewModel(
            fakeRepository, fakeRevRepository, fakeProRepository, fakeImageReviewRepository)

    advanceUntilIdle()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /** Test that the hunt is correctly loaded */
  @Test
  fun loadHunt_correctly() = runTest {
    viewModel.loadHunt(testHunt.uid)
    advanceUntilIdle()

    val state = viewModel.uiState.value

    assertNotNull(state.hunt)
    assertEquals(testHunt.uid, state.hunt?.uid)
    assertFalse(state.isLiked)
    assertFalse(state.isAchieved)
  }

  /** Test in case of non existing hunt laoded */
  @Test
  fun loadHunt_withInvalidId_logsError() = runTest {
    viewModel.loadHunt(HuntCardViewModelTestConstantsString.INVALID_ID)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.hunt)
    assertFalse(state.isLiked)
    assertFalse(state.isAchieved)
  }

  /** Test that loading a valid author profile works correctly */
  @Test
  fun loadAuthorProfile_success() = runTest {
    fakeProRepository.addProfile(
        Profile(
            uid = TEST_AUTHOR_ID,
            author =
                Author(
                    pseudonym = HuntCardViewModelTestConstantsString.AUTHOR_NAME,
                    bio = HuntCardViewModelTestConstantsString.AUTHOR_BIO,
                    profilePicture = 1)))

    viewModel.loadAuthorProfile(TEST_AUTHOR_ID)
    advanceUntilIdle()

    val profile = viewModel.uiState.value.authorProfile
    assertNotNull(profile)
    assertEquals(HuntCardViewModelTestConstantsString.AUTHOR_NAME, profile?.author?.pseudonym)
  }

  /** Test that setErrorMsg and clearErrorMsg work correctly */
  @Test
  fun setErrorMsg_and_clearErrorMsg() = runTest {
    viewModel.setErrorMsg(HuntCardViewModelTestConstantsString.ERROR_MESSAGE)
    assertEquals(
        HuntCardViewModelTestConstantsString.ERROR_MESSAGE, viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  /** Test that the default state has a hunt not null, no like and not achieved */
  @Test
  fun initialUiState_isDefault() {
    val state = viewModel.uiState.value
    assertNull(state.hunt)
    assertFalse(state.isLiked)
    assertFalse(state.isAchieved)
  }

  @Test
  fun loadHunt_withUserDoneHunts_marksAchieved() = runTest {
    val userId = "user_done"
    fakeProRepository.addProfile(Profile(uid = userId))
    fakeProRepository.addDoneHunt(userId, testHunt)

    viewModel.initialize(userId, testHunt)
    viewModel.loadHunt(testHunt.uid)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.isAchieved)
  }

  /** Test that loadOtherReview correctly loads reviews */
  @Test
  fun loadOtherReview_populatesReviews() = runTest {
    val review1 =
        HuntReview(
            HuntCardViewModelTestConstantsString.REVIEW_ID_1,
            HuntCardViewModelTestConstantsString.AUTHOR_ID_1,
            TEST_HUNT_ID,
            HuntCardViewModelTestConstantsNumeric.REVIEW_RATE_MAX,
            HuntCardViewModelTestConstantsString.COMMENT_1)
    val review2 =
        HuntReview(
            HuntCardViewModelTestConstantsString.REVIEW_ID_2,
            HuntCardViewModelTestConstantsString.AUTHOR_ID_2,
            TEST_HUNT_ID,
            HuntCardViewModelTestConstantsNumeric.REVIEW_RATE,
            HuntCardViewModelTestConstantsString.COMMENT_2)
    fakeRevRepository.addReviewHunt(review1)
    fakeRevRepository.addReviewHunt(review2)

    viewModel.loadOtherReview(TEST_HUNT_ID)
    advanceUntilIdle()

    val reviews = viewModel.uiState.value.reviewList
    assertEquals(2, reviews.size)
    assertTrue(reviews.contains(review1))
    assertTrue(reviews.contains(review2))
  }

  /** Test that loadOtherReview handles empty review list */
  @Test
  fun loadOtherReview_emptyList() = runTest {
    viewModel.loadOtherReview(TEST_HUNT_ID)
    advanceUntilIdle()

    val reviews = viewModel.uiState.value.reviewList
    assertTrue(reviews.isEmpty())
  }

  /** Test that deleteReview deletes photos and review, and handles exceptions */
  @Test
  fun deleteReview_repositoryThrows_setsErrorMsg() = runTest {
    // Arrange: review with photo
    val review =
        HuntReview(
            reviewId = "rev-exception",
            authorId = "user1",
            huntId = TEST_HUNT_ID,
            rating = 5.0,
            comment = "comment",
            photos = listOf("photo1"))
    fakeRevRepository.addReviewHunt(review)

    // Replace image repository with one that throws
    val failingImageRepo =
        object : ReviewImageRepositoryLocal() {
          override suspend fun deleteReviewPhoto(url: String) {
            throw RuntimeException("Failed to delete")
          }
        }

    viewModel =
        HuntCardViewModel(fakeRepository, fakeRevRepository, fakeProRepository, failingImageRepo)

    viewModel.deleteReview(
        huntID = TEST_HUNT_ID,
        reviewID = review.reviewId,
        userID = review.authorId,
        currentUserId = review.authorId)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.errorMsg?.contains("Failed to delete") == true)
  }

  /** Test that deleteReview handles non-existing review */
  @Test
  fun deleteReview_reviewRepositoryThrows_setsErrorMsg() = runTest {
    viewModel =
        HuntCardViewModel(
            fakeRepository, fakeRevRepository, fakeProRepository, ReviewImageRepositoryLocal())

    // attempt to delete a non-existing review
    viewModel.deleteReview(
        huntID = TEST_HUNT_ID,
        reviewID = "missing-review",
        userID = "user1",
        currentUserId = "user1")

    advanceUntilIdle()

    val state = viewModel.uiState.value

    assertTrue(
        state.errorMsg?.contains(HuntCardViewModelConstants.ERROR_DELETING_REVIEW_SET_MSG) == true)
  }

  /** Test that check the change of the hunt state from dislike to like and from like to dislike */
  @Test
  fun onLikeClick_isLiked() = runTest {
    val userId = "test_user"

    fakeProRepository.addProfile(Profile(uid = userId))
    viewModel.initialize(userId, testHunt)
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLiked)

    viewModel.onLikeClick(TEST_HUNT_ID)
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isLiked)

    viewModel.onLikeClick(TEST_HUNT_ID)
    advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isLiked)
  }

  /** Test that onDoneClick with null hunt sets error message */
  @Test
  fun onDoneClick_with_null_hunt_sets_error_message() = runTest {
    viewModel.onDoneClick()
    advanceUntilIdle() // wait for coroutines

    // Then
    val state = viewModel.uiState.value
    assertEquals(HuntCardViewModelTestConstantsString.HUNT_NOT_LOADED_MESSAGE, state.errorMsg)
    assertFalse(state.isAchieved)
  }

  /** Test that check that the hunt is marked as achieved */
  @Test
  fun onDoneClick_marks_hunt_as_achieved() = runTest {
    val userId = HuntCardViewModelTestConstantsString.TEST_USER_ID
    fakeProRepository.addProfile(Profile(uid = userId))
    viewModel.initialize(userId, testHunt)
    advanceUntilIdle()
    viewModel.onDoneClick()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isAchieved)
    val doneHunts = fakeProRepository.getDoneHunts(userId)
    assertTrue(doneHunts.contains(testHunt))
  }

  /** Test that check that the hunt is deleted */
  @Test
  fun onDeleteClick_deletesHunt() = runTest {
    viewModel.deleteHunt(testHunt.uid)
    advanceUntilIdle()

    try {
      // should get error since no hunt anymore
      fakeRepository.getHunt(testHunt.uid)
      fail(HuntCardViewModelTestConstantsString.ILLEGAL_EXCEPTION_MESSAGE)
    } catch (e: IllegalArgumentException) {
      assertEquals(
          "${HuntCardViewModelTestConstantsString.ERROR_MESSAGE_PREFIX} ${testHunt.uid} ${HuntCardViewModelTestConstantsString.ERROR_MESSAGE_SUFFIX}",
          e.message)
    }
  }

  /** Test that check no delete if not valid hunt id */
  @Test
  fun deleteHunt_withInvalidId_logsError() = runTest {
    viewModel.deleteHunt(HuntCardViewModelTestConstantsString.NON_EXISTING_ID)
    advanceUntilIdle()

    // Nothing to assert — just ensures no crash/log handled
  }

  @Test
  fun deleteReview_userOwnsReview_deletesSuccessfully() = runTest {
    val review =
        HuntReview(
            reviewId = HuntCardViewModelTestConstantsString.REVIEW_ID_1,
            authorId = HuntCardViewModelTestConstantsString.AUTHOR_ID_1,
            huntId = TEST_HUNT_ID,
            rating = HuntCardViewModelTestConstantsNumeric.REVIEW_RATE,
            comment = HuntCardViewModelTestConstantsString.COMMENT_2)
    fakeRevRepository.addReviewHunt(review)

    viewModel.deleteReview(
        huntID = TEST_HUNT_ID,
        reviewID = HuntCardViewModelTestConstantsString.REVIEW_ID_1,
        userID = HuntCardViewModelTestConstantsString.AUTHOR_ID_1,
        currentUserId = HuntCardViewModelTestConstantsString.AUTHOR_ID_1)

    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.reviewList.isEmpty())
  }

  @Test
  fun deleteReview_wrongUser_setsError() = runTest {
    val review =
        HuntReview(
            reviewId = HuntCardViewModelTestConstantsString.REVIEW_ID_1,
            authorId = HuntCardViewModelTestConstantsString.AUTHOR_ID_1,
            huntId = TEST_HUNT_ID,
            rating = HuntCardViewModelTestConstantsNumeric.REVIEW_RATE,
            comment = HuntCardViewModelTestConstantsString.COMMENT_2)
    fakeRevRepository.addReviewHunt(review)

    viewModel.deleteReview(
        huntID = TEST_HUNT_ID,
        reviewID = HuntCardViewModelTestConstantsString.REVIEW_ID_1,
        userID = HuntCardViewModelTestConstantsString.AUTHOR_ID_1,
        currentUserId = HuntCardViewModelTestConstantsString.COMMENT_3)

    advanceUntilIdle()

    assertEquals(
        HuntCardViewModelTestConstantsString.REVIEW_DELETE_ERROR_MESSAGE,
        viewModel.uiState.value.errorMsg)

    // Review must still exist
    assertEquals(1, fakeRevRepository.getHuntReviews(TEST_HUNT_ID).size)
  }

  /** Test that check the edit change correctly the hunt */
  @Test
  fun onEditClick() = runTest {
    val newHuntValue = testHunt.copy(title = HuntCardViewModelTestConstantsString.EDITED_TITLE)
    viewModel.editHunt(testHunt.uid, newHuntValue)
    advanceUntilIdle()

    val updatedHunt = fakeRepository.getHunt(testHunt.uid)
    assertEquals(HuntCardViewModelTestConstantsString.EDITED_TITLE, updatedHunt.title)
    assertEquals(newHuntValue, updatedHunt)
  }

  /** Test that check that there is an error with not a valid hunt */
  @Test
  fun editHunt_withInvalidId_logsError() = runTest {
    val newHunt =
        testHunt.copy(
            uid = HuntCardViewModelTestConstantsString.NON_EXISTING_ID,
            title = HuntCardViewModelTestConstantsString.SHOULD_FAIL_MESSAGE)
    viewModel.editHunt(HuntCardViewModelTestConstantsString.NON_EXISTING_ID, newHunt)
    advanceUntilIdle()

    // Verify that the hunt was NOT added or changed
    try {
      fakeRepository.getHunt(HuntCardViewModelTestConstantsString.NON_EXISTING_ID)
      fail(HuntCardViewModelTestConstantsString.ILLEGAL_EXCEPTION_MESSAGE)
    } catch (e: IllegalArgumentException) {
      assertEquals(HuntCardViewModelTestConstantsString.HUNT_NOT_FOUND_MESSAGE, e.message)
    }
  }

  /** Test that onLikeClick with null currentUserId returns early */
  @Test
  fun onLikeClick_withNullUserId_returnsEarly() = runTest {
    viewModel.onLikeClick(TEST_HUNT_ID)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.currentUserId)
    assertFalse(state.isLiked)
  }

  /** Test that onLikeClick toggles like state multiple times */
  @Test
  fun onLikeClick_multipleTimes_togglesCorrectly() = runTest {
    val userId = "test_user"

    fakeProRepository.addProfile(Profile(uid = userId))
    viewModel.initialize(userId, testHunt)
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLiked)

    viewModel.onLikeClick(TEST_HUNT_ID)
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isLiked)

    viewModel.onLikeClick(TEST_HUNT_ID)
    advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isLiked)

    viewModel.onLikeClick(TEST_HUNT_ID)
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isLiked)
  }
}
