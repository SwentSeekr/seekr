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
                  HuntCardViewModelTestConstantsNumeric.Location1,
                  HuntCardViewModelTestConstantsNumeric.Location2,
                  HuntCardViewModelTestConstantsString.Start),
          end =
              Location(
                  HuntCardViewModelTestConstantsNumeric.Location3,
                  HuntCardViewModelTestConstantsNumeric.Location4,
                  HuntCardViewModelTestConstantsString.End),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = HuntCardViewModelTestConstantsString.TestTile,
          description = HuntCardViewModelTestConstantsString.TestDescription,
          time = HuntCardViewModelTestConstantsNumeric.Time,
          distance = HuntCardViewModelTestConstantsNumeric.Distance,
          difficulty = Difficulty.EASY,
          authorId = TEST_AUTHOR_ID,
          mainImageUrl = "",
          reviewRate = HuntCardViewModelTestConstantsNumeric.ReviewRate)

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
    viewModel.loadHunt(HuntCardViewModelTestConstantsString.InvalidId)
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
                    pseudonym = HuntCardViewModelTestConstantsString.AuthorName,
                    bio = HuntCardViewModelTestConstantsString.AuthorBio,
                    profilePicture = 1)))

    viewModel.loadAuthorProfile(TEST_AUTHOR_ID)
    advanceUntilIdle()

    val profile = viewModel.uiState.value.authorProfile
    assertNotNull(profile)
    assertEquals(HuntCardViewModelTestConstantsString.AuthorName, profile?.author?.pseudonym)
  }

  /** Test that setErrorMsg and clearErrorMsg work correctly */
  @Test
  fun setErrorMsg_and_clearErrorMsg() = runTest {
    viewModel.setErrorMsg(HuntCardViewModelTestConstantsString.ErrorMessage)
    assertEquals(
        HuntCardViewModelTestConstantsString.ErrorMessage, viewModel.uiState.value.errorMsg)

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
            HuntCardViewModelTestConstantsString.ReviewId1,
            HuntCardViewModelTestConstantsString.AuthorId1,
            TEST_HUNT_ID,
            HuntCardViewModelTestConstantsNumeric.ReviewRate5,
            HuntCardViewModelTestConstantsString.Comment1)
    val review2 =
        HuntReview(
            HuntCardViewModelTestConstantsString.ReviewId2,
            HuntCardViewModelTestConstantsString.AuthorId2,
            TEST_HUNT_ID,
            HuntCardViewModelTestConstantsNumeric.ReviewRate,
            HuntCardViewModelTestConstantsString.Comment2)
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
        state.errorMsg?.contains(HuntCardViewModelConstants.ErrorDeletingReviewSetMsg) == true)
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
    assertEquals(HuntCardViewModelTestConstantsString.HuntNotLoaded, state.errorMsg)
    assertFalse(state.isAchieved)
  }

  /** Test that check that the hunt is marked as achieved */
  @Test
  fun onDoneClick_marks_hunt_as_achieved() = runTest {
    val userId = HuntCardViewModelTestConstantsString.TestUser
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
      fail(HuntCardViewModelTestConstantsString.IllegalExeption_Message)
    } catch (e: IllegalArgumentException) {
      assertEquals(
          "${HuntCardViewModelTestConstantsString.MessageErrorStart} ${testHunt.uid} ${HuntCardViewModelTestConstantsString.MessageErrorEnd}",
          e.message)
    }
  }

  /** Test that check no delete if not valid hunt id */
  @Test
  fun deleteHunt_withInvalidId_logsError() = runTest {
    viewModel.deleteHunt(HuntCardViewModelTestConstantsString.NonExistingId)
    advanceUntilIdle()

    // Nothing to assert — just ensures no crash/log handled
  }

  @Test
  fun deleteReview_userOwnsReview_deletesSuccessfully() = runTest {
    val review =
        HuntReview(
            reviewId = HuntCardViewModelTestConstantsString.Rev1,
            authorId = HuntCardViewModelTestConstantsString.AuthorId1,
            huntId = TEST_HUNT_ID,
            rating = HuntCardViewModelTestConstantsNumeric.ReviewRate,
            comment = HuntCardViewModelTestConstantsString.Comment2)
    fakeRevRepository.addReviewHunt(review)

    viewModel.deleteReview(
        huntID = TEST_HUNT_ID,
        reviewID = HuntCardViewModelTestConstantsString.Rev1,
        userID = HuntCardViewModelTestConstantsString.AuthorId1,
        currentUserId = HuntCardViewModelTestConstantsString.AuthorId1)

    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.reviewList.isEmpty())
  }

  @Test
  fun deleteReview_wrongUser_setsError() = runTest {
    val review =
        HuntReview(
            reviewId = HuntCardViewModelTestConstantsString.Rev1,
            authorId = HuntCardViewModelTestConstantsString.AuthorId1,
            huntId = TEST_HUNT_ID,
            rating = HuntCardViewModelTestConstantsNumeric.ReviewRate,
            comment = HuntCardViewModelTestConstantsString.Comment2)
    fakeRevRepository.addReviewHunt(review)

    viewModel.deleteReview(
        huntID = TEST_HUNT_ID,
        reviewID = HuntCardViewModelTestConstantsString.Rev1,
        userID = HuntCardViewModelTestConstantsString.AuthorId1,
        currentUserId = HuntCardViewModelTestConstantsString.Comment3)

    advanceUntilIdle()

    assertEquals(
        HuntCardViewModelTestConstantsString.ReviewErrorMessage, viewModel.uiState.value.errorMsg)

    // Review must still exist
    assertEquals(1, fakeRevRepository.getHuntReviews(TEST_HUNT_ID).size)
  }

  /** Test that check the edit change correctly the hunt */
  @Test
  fun onEditClick() = runTest {
    val newHuntValue = testHunt.copy(title = HuntCardViewModelTestConstantsString.EditTitle)
    viewModel.editHunt(testHunt.uid, newHuntValue)
    advanceUntilIdle()

    val updatedHunt = fakeRepository.getHunt(testHunt.uid)
    assertEquals(HuntCardViewModelTestConstantsString.EditTitle, updatedHunt.title)
    assertEquals(newHuntValue, updatedHunt)
  }

  /** Test that check that there is an error with not a valid hunt */
  @Test
  fun editHunt_withInvalidId_logsError() = runTest {
    val newHunt =
        testHunt.copy(
            uid = HuntCardViewModelTestConstantsString.NonExistingId,
            title = HuntCardViewModelTestConstantsString.Fail)
    viewModel.editHunt(HuntCardViewModelTestConstantsString.NonExistingId, newHunt)
    advanceUntilIdle()

    // Verify that the hunt was NOT added or changed
    try {
      fakeRepository.getHunt(HuntCardViewModelTestConstantsString.NonExistingId)
      fail(HuntCardViewModelTestConstantsString.IllegalExeption_Message)
    } catch (e: IllegalArgumentException) {
      assertEquals(HuntCardViewModelTestConstantsString.ErrorMessageNotFound, e.message)
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
