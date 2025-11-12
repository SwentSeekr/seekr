package com.swentseekr.seekr.ui.review

import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryLocal
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepositoryLocal
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ReviewHuntViewModelTest {

  private lateinit var fakeHuntsRepository: HuntsRepositoryLocal
  private lateinit var fakeReviewRepository: HuntReviewRepositoryLocal
  private val testDispatcher = StandardTestDispatcher()

  private lateinit var viewModel: ReviewHuntViewModel
  private val testHunt =
      Hunt(
          uid = "hunt123",
          start = Location(1.0, 2.0, "Start"),
          end = Location(3.0, 4.0, "End"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = "Test Hunt",
          description = "Test Description",
          time = 1.0,
          distance = 5.0,
          difficulty = Difficulty.EASY,
          authorId = "author123",
          mainImageUrl = "",
          reviewRate = 4.0)

  @Before
  fun setUp() = runTest {
    Dispatchers.setMain(testDispatcher)
    fakeHuntsRepository = HuntsRepositoryLocal()
    fakeReviewRepository = HuntReviewRepositoryLocal()
    fakeHuntsRepository.addHunt(testHunt)

    viewModel = ReviewHuntViewModel(fakeHuntsRepository, fakeReviewRepository)
    advanceUntilIdle()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun loadHunt_updatesUiStateWithHuntId() = runTest {
    viewModel.loadHunt(testHunt.uid)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(testHunt, state.hunt)
  }

  @Test
  fun setErrorMsg_and_clearErrorMsg() = runTest {
    viewModel.setErrorMsg("Something went wrong")
    assertEquals("Something went wrong", viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setReviewText_blank_setsInvalidMessage() = runTest {
    viewModel.setReviewText("")
    val state = viewModel.uiState.value
    assertEquals("The review cannot be empty", state.invalidReviewText)
  }

  @Test
  fun setReviewText_valid_clearsInvalidMessage() = runTest {
    viewModel.setReviewText("Nice hunt!")
    assertNull(viewModel.uiState.value.invalidReviewText)
  }

  @Test
  fun setRating_invalid_setsError() = runTest {
    viewModel.setRating(6.0)
    assertEquals("Rating must be between 1 and 5", viewModel.uiState.value.invalidRating)
  }

  @Test
  fun setRating_valid_clearsError() = runTest {
    viewModel.setRating(4.0)
    assertNull(viewModel.uiState.value.invalidRating)
  }

  @Test
  fun updateRating_updatesUiStateRating() = runTest {
    viewModel.updateRating(3.5)
    assertEquals(3.5, viewModel.uiState.value.rating, 0.0)
  }

  @Test
  fun addPhoto_addsToList() = runTest {
    val photo = "url"
    viewModel.addPhoto(photo)
    assertTrue(viewModel.uiState.value.photos.contains(photo))
  }

  @Test
  fun removePhoto_removesFromList() = runTest {
    val photo = ""
    viewModel.addPhoto(photo)
    viewModel.removePhoto(photo)
    assertFalse(viewModel.uiState.value.photos.contains(photo))
  }

  @Test
  fun submitReviewHunt_withInvalidData_setsError() = runTest {
    val initialState = viewModel.uiState.value
    viewModel.submitReviewHunt("user123", testHunt)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("At least one field is not valid", state.errorMsg)
    assertEquals("", state.huntId)
    assertEquals("", state.userId)
    assertEquals("", state.reviewText)
    assertEquals(0.0, state.rating, 0.0)
    assertFalse(state.isSubmitted)
    assertFalse(state.saveSuccessful)
    assertNull(state.invalidReviewText)
    assertNull(state.invalidRating)
    assertTrue(state.photos.isEmpty())
  }

  @Test
  fun submitReviewHunt_withValidData_savesReview() = runTest {
    viewModel.setReviewText("Great hunt!")
    viewModel.setRating(4.0)

    viewModel.submitReviewHunt("user123", testHunt)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.saveSuccessful)
    assertTrue(state.isSubmitted)

    val reviews = fakeReviewRepository.getHuntReviews(testHunt.uid)
    assertEquals(1, reviews.size)
    assertEquals("Great hunt!", reviews.first().comment)
    assertEquals(4.0, reviews.first().rating, 0.0)
  }

  @Test
  fun clearForm_afterSuccess_resetsForm() = runTest {
    viewModel.loadHunt(testHunt.uid)
    advanceUntilIdle()
    viewModel.setReviewText("Amazing!")
    viewModel.setRating(5.0)
    viewModel.submitReviewHunt("user123", testHunt)
    advanceUntilIdle()

    viewModel.clearForm()
    val state = viewModel.uiState.value
    assertEquals(testHunt, state.hunt)
    assertEquals("", state.reviewText)
  }

  @Test
  fun clearForm_whenNotSuccessful_setsErrorMsg() = runTest {
    viewModel.clearForm()
    assertEquals(
        "Cannot clear form, review not submitted successfully.", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun clearForm_afterCancel_resetsForm() = runTest {
    viewModel.loadHunt(testHunt.uid)
    advanceUntilIdle()
    viewModel.setReviewText("Amazing!")
    viewModel.setRating(5.0)
    viewModel.submitReviewHunt("user123", testHunt)
    advanceUntilIdle()

    viewModel.clearFormCancel()
    val state = viewModel.uiState.value
    assertEquals(testHunt, state.hunt)
    assertEquals("", state.reviewText)
  }

  @Test
  fun clearForm_afterCancel_whenNothing_resetsForm() = runTest {
    viewModel.loadHunt(testHunt.uid)
    advanceUntilIdle()

    viewModel.clearFormCancel()
    val state = viewModel.uiState.value
    assertEquals(testHunt, state.hunt)
    assertEquals("", state.reviewText)
  }

  @Test
  fun deleteReview_whenUserIsAuthor_deletesReviewSuccessfully() = runTest {
    viewModel.setReviewText("Great hunt!")
    viewModel.setRating(5.0)
    viewModel.submitReviewHunt("user123", testHunt)
    advanceUntilIdle()

    val createdReview = fakeReviewRepository.getHuntReviews(testHunt.uid).first()
    assertEquals("user123", createdReview.authorId)

    // Inject currentUserId manually
    viewModel.deleteReview(createdReview.reviewId, "user123", currentUserId = "user123")
    advanceUntilIdle()

    val reviewsAfterDelete = fakeReviewRepository.getHuntReviews(testHunt.uid)
    assertTrue(reviewsAfterDelete.isEmpty())
  }

  @Test
  fun deleteReview_whenUserIsNotAuthor_setsErrorMessage() = runTest {
    viewModel.setReviewText("Nice!")
    viewModel.setRating(4.0)
    viewModel.submitReviewHunt("user123", testHunt)
    advanceUntilIdle()

    val createdReview = fakeReviewRepository.getHuntReviews(testHunt.uid).first()

    viewModel.deleteReview(createdReview.reviewId, "user123", currentUserId = "otherUser456")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("You can only delete your own review.", state.errorMsg)

    val reviewsAfterAttempt = fakeReviewRepository.getHuntReviews(testHunt.uid)
    assertEquals(1, reviewsAfterAttempt.size)
  }
}
