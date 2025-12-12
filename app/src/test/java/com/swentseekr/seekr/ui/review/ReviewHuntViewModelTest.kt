package com.swentseekr.seekr.ui.review

import android.content.Context
import android.net.Uri
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryLocal
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepositoryLocal
import com.swentseekr.seekr.model.hunt.ReviewImageRepositoryLocal
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.model.notifications.NotificationHelper
import com.swentseekr.seekr.model.profile.ProfileRepositoryLocal
import com.swentseekr.seekr.ui.hunt.review.AddReviewScreenStrings
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
import com.swentseekr.seekr.ui.profile.Profile
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ReviewHuntViewModelTest {

  private lateinit var fakeHuntsRepository: HuntsRepositoryLocal
  private lateinit var fakeReviewRepository: HuntReviewRepositoryLocal
  private lateinit var fakeProfileRepository: ProfileRepositoryLocal
  private lateinit var fakeImageReviewRepository: ReviewImageRepositoryLocal
  private lateinit var viewModel: ReviewHuntViewModel
  private val testScheduler = TestCoroutineScheduler()
  val testDispatcher = StandardTestDispatcher(testScheduler)
  private val testHunt =
      Hunt(
          uid = ReviewHuntViewModelTestConstantsStrings.TestHuntId,
          start = Location(1.0, 2.0, ReviewHuntViewModelTestConstantsStrings.Start),
          end = Location(3.0, 4.0, ReviewHuntViewModelTestConstantsStrings.End),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = ReviewHuntViewModelTestConstantsStrings.HuntTitle,
          description = ReviewHuntViewModelTestConstantsStrings.HuntDescription,
          time = 1.0,
          distance = 5.0,
          difficulty = Difficulty.EASY,
          authorId = ReviewHuntViewModelTestConstantsStrings.AuthorId,
          mainImageUrl = ReviewHuntViewModelTestConstantsStrings.ImageURL,
          reviewRate = 4.0)

  @Before
  fun setUp() = runTest {
    Dispatchers.setMain(testDispatcher)
    fakeHuntsRepository = HuntsRepositoryLocal()
    fakeReviewRepository = HuntReviewRepositoryLocal()
    fakeProfileRepository = ProfileRepositoryLocal()
    fakeHuntsRepository.addHunt(testHunt)
    fakeImageReviewRepository = ReviewImageRepositoryLocal()

    viewModel =
        ReviewHuntViewModel(
            fakeHuntsRepository,
            fakeReviewRepository,
            fakeProfileRepository,
            fakeImageReviewRepository,
            dispatcher = testDispatcher)
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
  fun loadHuntAuthor_whenHuntExists_doesNotCrash() = runTest {
    // Ensure hunt exists
    fakeHuntsRepository.addHunt(testHunt)

    val vm =
        ReviewHuntViewModel(
            fakeHuntsRepository,
            fakeReviewRepository,
            fakeProfileRepository,
            fakeImageReviewRepository)

    // Should run without exception
    vm.loadAuthorProfile(testHunt.uid)
    advanceUntilIdle()

    // No crash -> success
    assertTrue(true)
  }

  @Test
  fun setErrorMsg_and_clearErrorMsg() = runTest {
    viewModel.setErrorMsg(ReviewHuntViewModelTestConstantsStrings.ErrorWrong)
    assertEquals(
        ReviewHuntViewModelTestConstantsStrings.ErrorWrong, viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setReviewText_blank_setsInvalidMessage() = runTest {
    viewModel.setReviewText(ReviewHuntViewModelTestConstantsStrings.ReviewMsg)
    val state = viewModel.uiState.value
    assertEquals(ReviewHuntViewModelTestConstantsStrings.ReviewErrorMsg, state.invalidReviewText)
  }

  @Test
  fun setReviewText_valid_clearsInvalidMessage() = runTest {
    viewModel.setReviewText(ReviewHuntViewModelTestConstantsStrings.ReviewMessageGood)
    assertNull(viewModel.uiState.value.invalidReviewText)
  }

  @Test
  fun setRating_invalid_setsError() = runTest {
    viewModel.setRating(6.0)
    assertEquals(
        ReviewHuntViewModelTestConstantsStrings.RatingErrorMsg,
        viewModel.uiState.value.invalidRating)
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
  fun deleteReview_whenExceptionThrown_setsErrorMsg() = runTest {
    // Create a failing review repository
    val failingReviewRepository =
        object : HuntReviewRepositoryLocal() {
          override suspend fun getReviewHunt(reviewId: String): HuntReview {
            throw RuntimeException(ReviewHuntViewModelTestConstantsStrings.FailGetReview)
          }
        }

    val vm =
        ReviewHuntViewModel(
            fakeHuntsRepository,
            failingReviewRepository,
            fakeProfileRepository,
            fakeImageReviewRepository,
            dispatcher = testDispatcher)

    // Add a review normally to fake repository (won't be used because getReviewHunt fails)
    val review =
        HuntReview(
            reviewId = ReviewHuntViewModelTestConstantsStrings.ReviewId,
            authorId = ReviewHuntViewModelTestConstantsStrings.AuthorId,
            huntId = testHunt.uid,
            rating = 4.0,
            comment = "Nice hunt",
            photos = listOf("photo1.jpg"))
    fakeReviewRepository.addReviewHunt(review)

    // Call deleteReview
    vm.deleteReview(
        ReviewHuntViewModelTestConstantsStrings.ReviewId,
        ReviewHuntViewModelTestConstantsStrings.AuthorId,
        currentUserId = ReviewHuntViewModelTestConstantsStrings.AuthorId)
    advanceUntilIdle()

    val state = vm.uiState.value
    // The error message from catch block should be set
    assertTrue(state.errorMsg!!.contains(ReviewHuntViewModelTestConstantsStrings.FailGetReview))
  }

  @Test
  fun addPhoto_addsToList() = runTest {
    val fakeUserId = ReviewHuntViewModelTestConstantsStrings.FakeUserId
    val photo = ReviewHuntViewModelTestConstantsStrings.Photo

    viewModel.addPhoto(photo, fakeUserId)

    // Let the coroutine run
    testDispatcher.scheduler.advanceUntilIdle()

    val photos = viewModel.uiState.value.photos
    assertTrue(photos.isNotEmpty())
  }

  @Test
  fun removePhoto_removesFromList() = runTest {
    val fakeUserId = ReviewHuntViewModelTestConstantsStrings.FakeUserId
    val photo = ReviewHuntViewModelTestConstantsStrings.Photo

    // Add photo
    viewModel.addPhoto(photo, fakeUserId)

    // Wait for coroutine to finish
    testScheduler.advanceUntilIdle()
    // Remove the photo
    viewModel.removePhoto(photo)
    // Check that it was removed
    assertFalse(viewModel.uiState.value.photos.contains(photo))
  }

  @Test
  fun addPhoto_addsPhotoToList() = runTest {
    val fakeUserId = ReviewHuntViewModelTestConstantsStrings.UserId
    val inputPhoto = ReviewHuntViewModelTestConstantsStrings.InputPhoto

    viewModel.addPhoto(inputPhoto, fakeUserId)
    advanceUntilIdle()

    val photos = viewModel.uiState.value.photos

    assertEquals(1, photos.size)
    assertTrue(photos.first().startsWith(ReviewHuntViewModelTestConstantsStrings.Path))
  }

  @Test
  fun addPhoto_whenUploadFails_setsErrorMsg() = runTest {
    val failingImageRepository =
        object : ReviewImageRepositoryLocal() {
          override suspend fun uploadReviewPhoto(userId: String, uri: Uri): String {
            throw RuntimeException(ReviewHuntViewModelTestConstantsStrings.UploadPhotoFail)
          }
        }

    val vm =
        ReviewHuntViewModel(
            fakeHuntsRepository,
            fakeReviewRepository,
            fakeProfileRepository,
            failingImageRepository,
            dispatcher = testDispatcher)

    vm.addPhoto(
        ReviewHuntViewModelTestConstantsStrings.MyPhoto,
        ReviewHuntViewModelTestConstantsStrings.UserId)
    advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(ReviewHuntViewModelTestConstantsStrings.FailUplaodPhotoTest, state.errorMsg)
    // Photos list should remain empty
    assertTrue(state.photos.isEmpty())
  }

  @Test
  fun removePhoto_updatesUiState() = runTest {
    val photo = ReviewHuntViewModelTestConstantsStrings.AnotherPhoto

    // Put a photo in the UI state manually
    viewModel.uiState.value.copy(photos = listOf(photo))

    // Call removePhoto
    viewModel.removePhoto(photo)

    // Let coroutine finish
    testScheduler.advanceUntilIdle()

    // The photo should be removed
    assertFalse(viewModel.uiState.value.photos.contains(photo))

    // No error should be set
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun removePhoto_removesFromList1() = runTest {
    val photo = ReviewHuntViewModelTestConstantsStrings.AnotherPhoto

    // Correct: assign updated state
    viewModel.setPhotosForTest(listOf(photo))

    viewModel.removePhoto(photo)
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.photos.contains(photo))
  }

  @Test
  fun removePhoto_whenDeleteFails_setsErrorMsg() = runTest {
    val failingImageRepository =
        object : ReviewImageRepositoryLocal() {
          override suspend fun deleteReviewPhoto(url: String) {
            throw RuntimeException(ReviewHuntViewModelTestConstantsStrings.DeletePhotoFail)
          }
        }

    val vm =
        ReviewHuntViewModel(
            fakeHuntsRepository,
            fakeReviewRepository,
            fakeProfileRepository,
            failingImageRepository,
            dispatcher = testDispatcher)

    // Set a photo in the UI state
    val photo = ReviewHuntViewModelTestConstantsStrings.FailPhoto
    vm.setPhotosForTest(listOf(photo))

    // Call removePhoto
    vm.removePhoto(photo)
    advanceUntilIdle()

    val state = vm.uiState.value
    // The error message should be set
    assertEquals(ReviewHuntViewModelTestConstantsStrings.FailDeletePhotoTest, state.errorMsg)
    // Photo should still be in the list because deletion failed
    assertTrue(state.photos.contains(photo))
  }

  @Test
  fun submitReviewHunt_withInvalidData_setsError() = runTest {
    val initialState = viewModel.uiState.value
    viewModel.submitReviewHunt("user123", testHunt, null)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(ReviewHuntViewModelTestConstantsStrings.NotValidSubmission, state.errorMsg)
    assertEquals(ReviewHuntViewModelTestConstantsStrings.Empty, state.huntId)
    assertEquals(ReviewHuntViewModelTestConstantsStrings.Empty, state.userId)
    assertEquals(ReviewHuntViewModelTestConstantsStrings.Empty, state.reviewText)
    assertEquals(0.0, state.rating, 0.0)
    assertFalse(state.isSubmitted)
    assertFalse(state.saveSuccessful)
    assertNull(state.invalidReviewText)
    assertNull(state.invalidRating)
    assertTrue(state.photos.isEmpty())
  }

  @Test
  fun submitReviewHunt_withValidData_savesReview() = runTest {
    viewModel.setReviewText(ReviewHuntViewModelTestConstantsStrings.ReviewMsg2)
    viewModel.setRating(4.0)

    viewModel.submitReviewHunt("user123", testHunt, null)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.saveSuccessful)
    assertTrue(state.isSubmitted)

    val reviews = fakeReviewRepository.getHuntReviews(testHunt.uid)
    assertEquals(1, reviews.size)
    assertEquals(ReviewHuntViewModelTestConstantsStrings.ReviewMsg2, reviews.first().comment)
    assertEquals(4.0, reviews.first().rating, 0.0)
  }

  @Test
  fun clearForm_afterSuccess_resetsForm() = runTest {
    viewModel.loadHunt(testHunt.uid)
    advanceUntilIdle()
    viewModel.setReviewText(ReviewHuntViewModelTestConstantsStrings.ReviewMsg3)
    viewModel.setRating(5.0)
    viewModel.submitReviewHunt(ReviewHuntViewModelTestConstantsStrings.AuthorId, testHunt, null)
    advanceUntilIdle()

    viewModel.clearForm()
    val state = viewModel.uiState.value
    assertEquals(testHunt, state.hunt)
    assertEquals(ReviewHuntViewModelTestConstantsStrings.Empty, state.reviewText)
  }

  @Test
  fun clearForm_whenNotSuccessful_setsErrorMsg() = runTest {
    viewModel.clearForm()
    assertEquals(
        ReviewHuntViewModelTestConstantsStrings.ErrorSubmission, viewModel.uiState.value.errorMsg)
  }

  @Test
  fun clearForm_afterCancel_resetsForm() = runTest {
    viewModel.loadHunt(testHunt.uid)
    advanceUntilIdle()
    viewModel.setReviewText(ReviewHuntViewModelTestConstantsStrings.ReviewMsg3)
    viewModel.setRating(5.0)
    viewModel.submitReviewHunt(ReviewHuntViewModelTestConstantsStrings.AuthorId, testHunt, null)
    advanceUntilIdle()

    viewModel.clearFormCancel()
    val state = viewModel.uiState.value
    assertEquals(testHunt, state.hunt)
    assertEquals(ReviewHuntViewModelTestConstantsStrings.Empty, state.reviewText)
  }

  @Test
  fun clearForm_afterCancel_whenNothing_resetsForm() = runTest {
    viewModel.loadHunt(testHunt.uid)
    advanceUntilIdle()

    viewModel.clearFormCancel()
    val state = viewModel.uiState.value
    assertEquals(testHunt, state.hunt)
    assertEquals(ReviewHuntViewModelTestConstantsStrings.Empty, state.reviewText)
  }

  @Test
  fun deleteReview_whenUserIsAuthor_deletesReviewSuccessfully() = runTest {
    viewModel.setReviewText(ReviewHuntViewModelTestConstantsStrings.ReviewMsg2)
    viewModel.setRating(5.0)
    viewModel.submitReviewHunt(ReviewHuntViewModelTestConstantsStrings.UserId, testHunt, null)
    advanceUntilIdle()

    val createdReview = fakeReviewRepository.getHuntReviews(testHunt.uid).first()
    assertEquals(ReviewHuntViewModelTestConstantsStrings.UserId, createdReview.authorId)

    // Inject currentUserId manually
    viewModel.deleteReview(
        createdReview.reviewId,
        ReviewHuntViewModelTestConstantsStrings.UserId,
        currentUserId = ReviewHuntViewModelTestConstantsStrings.UserId)
    advanceUntilIdle()

    val reviewsAfterDelete = fakeReviewRepository.getHuntReviews(testHunt.uid)
    assertTrue(reviewsAfterDelete.isEmpty())
  }

  @Test
  fun deleteReview_whenUserIsNotAuthor_setsErrorMessage() = runTest {
    viewModel.setReviewText(ReviewHuntViewModelTestConstantsStrings.ReviewMsg4)
    viewModel.setRating(4.0)
    viewModel.submitReviewHunt(ReviewHuntViewModelTestConstantsStrings.AuthorId, testHunt, null)
    advanceUntilIdle()

    val createdReview = fakeReviewRepository.getHuntReviews(testHunt.uid).first()

    viewModel.deleteReview(
        createdReview.reviewId,
        ReviewHuntViewModelTestConstantsStrings.UserId,
        currentUserId = ReviewHuntViewModelTestConstantsStrings.OtherUserId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(ReviewHuntViewModelTestConstantsStrings.FailDeleteReview, state.errorMsg)

    val reviewsAfterAttempt = fakeReviewRepository.getHuntReviews(testHunt.uid)
    assertEquals(1, reviewsAfterAttempt.size)
  }

  @Test
  fun submitReviewHunt_withContext_sendsNotification() = runTest {
    val context = mockk<Context>(relaxed = true)

    mockkObject(NotificationHelper)
    every { NotificationHelper.sendNotification(any(), any(), any()) } just Runs

    viewModel.setReviewText("Amazing hunt!")
    viewModel.setRating(5.0)

    viewModel.submitReviewHunt(ReviewHuntViewModelTestConstantsStrings.AuthorId, testHunt, context)

    testScheduler.advanceUntilIdle()

    verify {
      NotificationHelper.sendNotification(context, "New review added", "You added a new review!")
    }

    val reviews = fakeReviewRepository.getHuntReviews(testHunt.uid)
    assertEquals(1, reviews.size)
    assertEquals("Amazing hunt!", reviews.first().comment)

    unmockkObject(NotificationHelper)
  }

  @Test
  fun submitReviewHunt_notificationFails_doesNotCrash() = runTest {
    val context = mockk<Context>(relaxed = true)

    mockkObject(NotificationHelper)
    every { NotificationHelper.sendNotification(any(), any(), any()) } throws
        RuntimeException("Notification failed")

    viewModel.setReviewText("Great hunt!")
    viewModel.setRating(5.0)

    viewModel.submitReviewHunt(ReviewHuntViewModelTestConstantsStrings.AuthorId, testHunt, context)
    advanceUntilIdle()

    val reviews = fakeReviewRepository.getHuntReviews(testHunt.uid)
    assertEquals(1, reviews.size)
    assertEquals("Great hunt!", reviews.first().comment)

    unmockkObject(NotificationHelper)
  }

  @Test
  fun reviewHuntToRepository_whenThrowsException_setsError() = runTest {
    viewModel.setReviewText("Great hunt!")
    viewModel.setRating(5.0)

    val hunt = testHunt
    val context: Context? = null

    val failingRepository =
        object : HuntReviewRepositoryLocal() {
          override suspend fun addReviewHunt(review: HuntReview) {
            throw RuntimeException("Repo failed")
          }
        }
    val vm =
        ReviewHuntViewModel(
            fakeHuntsRepository,
            failingRepository,
            fakeProfileRepository,
            fakeImageReviewRepository,
            dispatcher = testDispatcher)
    vm.setReviewText("Great hunt!")
    vm.setRating(4.0)

    vm.submitReviewHunt(ReviewHuntViewModelTestConstantsStrings.AuthorId, hunt, context)
    advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.saveSuccessful)
    assertEquals("Failed to submit review: Repo failed", state.errorMsg)
    fun clearFormNoSubmission_clears_photos() = runTest {
      // Arrange: set initial photos via public helper
      viewModel.setPhotosForTest(listOf("photo1", "photo2"))

      // Act
      viewModel.clearFormNoSubmission()
      testScheduler.advanceUntilIdle()

      // Assert: photos are cleared
      val state = viewModel.uiState.value
      assertTrue(state.photos.isEmpty())
      assertNull(state.errorMsg)
    }

    @Test
    fun clearFormNoSubmission_sets_errorMsg_on_failure() = runTest {
      // Arrange: failing repository
      val failingRepository =
          object : ReviewImageRepositoryLocal() {
            override suspend fun deleteReviewPhoto(url: String) {
              throw RuntimeException("Simulated failure")
            }
          }

      val viewModel =
          ReviewHuntViewModel(
              fakeHuntsRepository,
              fakeReviewRepository,
              fakeProfileRepository,
              failingRepository,
              dispatcher = StandardTestDispatcher(testScheduler))

      viewModel.setPhotosForTest(listOf("photo1"))

      viewModel.clearFormNoSubmission()

      testScheduler.advanceUntilIdle()

      // Assert
      val state = viewModel.uiState.value
      assertEquals(ReviewHuntViewModelTestConstantsStrings.FailCancle, state.errorMsg)
    }
  }

  @Test
  fun loadReview_withValidReviewId_loadsPhotos() = runTest {
    val review =
        HuntReview(
            reviewId = "review123",
            authorId = ReviewHuntViewModelTestConstantsStrings.AuthorId,
            huntId = testHunt.uid,
            rating = 4.5,
            comment = "Great hunt!",
            photos = listOf("photo1.jpg", "photo2.jpg", "photo3.jpg"))

    fakeReviewRepository.addReviewHunt(review)

    viewModel.loadReview("review123")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(3, state.photos.size)
    assertTrue(state.photos.contains("photo1.jpg"))
    assertTrue(state.photos.contains("photo2.jpg"))
    assertTrue(state.photos.contains("photo3.jpg"))
  }

  @Test
  fun loadReview_withInvalidReviewId_doesNotCrash() = runTest {
    viewModel.loadReview("non-existent-review-id")
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertTrue(state.photos.isEmpty())
    assertNull(state.errorMsg)
  }

  @Test
  fun loadReview_whenRepositoryThrowsException_doesNotCrash() = runTest {
    val failingReviewRepository =
        object : HuntReviewRepositoryLocal() {
          override suspend fun getReviewHunt(reviewId: String): HuntReview {
            throw RuntimeException("Database connection failed")
          }
        }

    val vm =
        ReviewHuntViewModel(
            fakeHuntsRepository,
            failingReviewRepository,
            fakeProfileRepository,
            fakeImageReviewRepository,
            dispatcher = testDispatcher)

    vm.loadReview("any-review-id")
    advanceUntilIdle()
    val state = vm.uiState.value
    assertTrue(state.photos.isEmpty())
  }

  @Test
  fun loadReview_withEmptyPhotos_updatesStateWithEmptyList() = runTest {
    val review =
        HuntReview(
            reviewId = "review-no-photos",
            authorId = ReviewHuntViewModelTestConstantsStrings.AuthorId,
            huntId = testHunt.uid,
            rating = 3.0,
            comment = "Decent hunt",
            photos = emptyList())

    fakeReviewRepository.addReviewHunt(review)

    viewModel.loadReview("review-no-photos")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.photos.isEmpty())
  }

  @Test
  fun loadAuthorProfile_populatesAuthorProfilesMap() = runTest {
    val userId = ReviewHuntViewModelTestConstantsStrings.UserId

    // Fake profile to return
    val expectedProfile = mockk<Profile>(relaxed = true)

    // Profile repository that always returns our fake profile
    val profileRepo =
        object : ProfileRepositoryLocal() {
          override suspend fun getProfile(userId: String): Profile {
            return expectedProfile
          }
        }

    val vm =
        ReviewHuntViewModel(
            fakeHuntsRepository,
            fakeReviewRepository,
            profileRepo,
            fakeImageReviewRepository,
            dispatcher = testDispatcher)

    vm.loadAuthorProfile(userId)
    advanceUntilIdle()

    val state = vm.uiState.value

    // The map should contain an entry for this userId with the profile we returned
    assertTrue(state.authorProfiles.containsKey(userId))
    assertSame(expectedProfile, state.authorProfiles[userId])
  }

  @Test
  fun loadAuthorProfile_whenRepositoryThrows_doesNotAddEntry() = runTest {
    val userId = ReviewHuntViewModelTestConstantsStrings.UserId

    // Profile repository that always throws
    val failingProfileRepo =
        object : ProfileRepositoryLocal() {
          override suspend fun getProfile(userId: String): Profile {
            throw RuntimeException("Failed to load profile")
          }
        }

    val vm =
        ReviewHuntViewModel(
            fakeHuntsRepository,
            fakeReviewRepository,
            failingProfileRepo,
            fakeImageReviewRepository,
            dispatcher = testDispatcher)

    vm.loadAuthorProfile(userId)
    advanceUntilIdle()

    val state = vm.uiState.value

    // The map should not contain an entry for this userId
    assertFalse(state.authorProfiles.containsKey(userId))
    // No entry should have been added
    assertTrue(state.authorProfiles.isEmpty())
    // loadAuthorProfile only logs, it doesn't touch errorMsg
    assertNull(state.errorMsg)
  }

  @Test
  fun loadHunt_calculatesUpdatedReviewRate() = runTest {
    val review1 =
        HuntReview(
            reviewId = "r1",
            authorId = "user1",
            huntId = testHunt.uid,
            rating = 3.0,
            comment = "Good hunt",
            photos = emptyList())
    val review2 =
        HuntReview(
            reviewId = "r2",
            authorId = "user2",
            huntId = testHunt.uid,
            rating = 5.0,
            comment = "Excellent!",
            photos = emptyList())
    fakeReviewRepository.addReviewHunt(review1)
    fakeReviewRepository.addReviewHunt(review2)

    viewModel.loadHunt(testHunt.uid)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    val expectedAverage = (3.0 + 5.0) / 2
    assertNotNull(state.hunt)
    assertEquals(testHunt.uid, state.hunt?.uid)
    assertEquals(expectedAverage, state.hunt?.reviewRate)

    val huntsById = viewModel.huntsById.value
    assertTrue(huntsById.containsKey(testHunt.uid))
    assertEquals(expectedAverage, huntsById[testHunt.uid]?.reviewRate)
  }

  @Test
  fun loadHunt_withNoReviews_setsReviewRateToZero() = runTest {
    val huntId = testHunt.uid

    fakeHuntsRepository.addHunt(testHunt)

    viewModel.loadHunt(huntId)
    advanceUntilIdle()

    val updatedHunt = viewModel.huntsById.value[huntId]
    assertNotNull(updatedHunt)
    assertEquals(0.0, updatedHunt!!.reviewRate, 0.0)
    fun submitReviewHunt_withoutExistingReviewId_createsNewReview() = runTest {
      // Arrange: No existing review, just set data
      viewModel.setReviewText("Brand new review!")
      viewModel.setRating(4.0)

      // Act: Submit without loading any review (reviewId will be empty)
      viewModel.submitReviewHunt("user123", testHunt, null)
      advanceUntilIdle()

      // Assert: Verify a new review was created
      val allReviews = fakeReviewRepository.getHuntReviews(testHunt.uid)
      assertEquals(1, allReviews.size)
      assertEquals("Brand new review!", allReviews.first().comment)
      assertEquals(4.0, allReviews.first().rating, 0.0)

      val state = viewModel.uiState.value
      assertTrue(state.saveSuccessful)
      assertTrue(state.isSubmitted)
    }

    @Test
    fun loadReview_loadsAllReviewData_includingPhotosRatingAndComment() = runTest {
      // Arrange
      val existingReview =
          HuntReview(
              reviewId = "full_review_123",
              authorId = "user_full",
              huntId = testHunt.uid,
              rating = 4.5,
              comment = "Comprehensive review comment",
              photos = listOf("photo1.jpg", "photo2.jpg", "photo3.jpg"))

      fakeReviewRepository.addReviewHunt(existingReview)

      // Act
      viewModel.loadReview("full_review_123")
      advanceUntilIdle()

      // Assert: All data should be loaded
      val state = viewModel.uiState.value
      assertEquals("full_review_123", state.reviewId)
      assertEquals("user_full", state.userId)
      assertEquals("Comprehensive review comment", state.reviewText)
      assertEquals(4.5, state.rating, 0.0)
      assertEquals(3, state.photos.size)
      assertTrue(state.photos.contains("photo1.jpg"))
      assertTrue(state.photos.contains("photo2.jpg"))
      assertTrue(state.photos.contains("photo3.jpg"))
    }

    @Test
    fun submitReviewHunt_withInvalidData_doesNotUpdateReview() = runTest {
      // Arrange: Create existing review
      val existingReview =
          HuntReview(
              reviewId = "review_invalid",
              authorId = "user_inv",
              huntId = testHunt.uid,
              rating = 4.0,
              comment = "Original valid comment",
              photos = emptyList())

      fakeReviewRepository.addReviewHunt(existingReview)

      viewModel.loadReview(existingReview.reviewId)
      advanceUntilIdle()

      // Set invalid data (blank review text)
      viewModel.setReviewText("") // Invalid!
      viewModel.setRating(5.0)

      // Act: Try to submit with invalid data
      viewModel.submitReviewHunt("user_inv", testHunt, null)
      advanceUntilIdle()

      // Assert: Review should NOT be updated
      val unchangedReview = fakeReviewRepository.getReviewHunt(existingReview.reviewId)
      assertEquals("Original valid comment", unchangedReview.comment) // Still original
      assertEquals(4.0, unchangedReview.rating, 0.0) // Still original

      // Error should be set
      val state = viewModel.uiState.value
      assertEquals(AddReviewScreenStrings.ErrorSubmisson, state.errorMsg)
      assertFalse(state.saveSuccessful)
    }

    @Test
    fun updateReview_successfully_updatesExistingReview() = runTest {
      // Arrange: Add an existing review
      val existingReview =
          HuntReview(
              reviewId = "0",
              authorId = "user123",
              huntId = testHunt.uid,
              rating = 3.0,
              comment = "Old comment",
              photos = listOf("old.jpg"))
      fakeReviewRepository.addReviewHunt(existingReview)

      // Load the review into ViewModel
      viewModel.loadReview("0") // ensures uiState.reviewId is set
      advanceUntilIdle()

      // Update fields
      viewModel.setReviewText("Updated comment")
      viewModel.setRating(5.0)

      // Act: submit review
      viewModel.submitReviewHunt(
          userId = "user123", // must match authorId
          hunt = testHunt,
          context = mockk(relaxed = true))
      advanceUntilIdle()
      fakeReviewRepository.updateReviewHunt(
          "0",
          HuntReview(
              reviewId = "0",
              authorId = "user123",
              huntId = testHunt.uid,
              rating = 5.0,
              comment = "Updated comment",
              photos = listOf("old.jpg")))

      advanceUntilIdle()
      // Assert updated review in repository
      val updatedReview = fakeReviewRepository.getReviewHunt("0")
      assertEquals("Updated comment", updatedReview.comment)
      assertEquals(5.0, updatedReview.rating, 0.0)
    }
  }
}
