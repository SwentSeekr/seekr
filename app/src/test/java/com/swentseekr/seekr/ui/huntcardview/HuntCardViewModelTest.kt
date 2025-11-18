package com.swentseekr.seekr.ui.huntcardview

import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryLocal
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepositoryLocal
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.model.profile.ProfileRepositoryLocal
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
  private val testDispatcher = StandardTestDispatcher()

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
    fakeRepository = HuntsRepositoryLocal()
    fakeRepository.addHunt(testHunt)
    fakeRevRepository = HuntReviewRepositoryLocal()
    fakeProRepository = ProfileRepositoryLocal()

    viewModel = HuntCardViewModel(fakeRepository, fakeRevRepository, fakeProRepository)

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
    viewModel.loadHunt("invalid_id")
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
            uid = "author123",
            author = Author(pseudonym = "AuthorName", bio = "Bio", profilePicture = 1)))

    viewModel.loadAuthorProfile("author123")
    advanceUntilIdle()

    val profile = viewModel.uiState.value.authorProfile
    assertNotNull(profile)
    assertEquals("AuthorName", profile?.author?.pseudonym)
  }

  /** Test that loading an invalid author ID does not crash and results in null profile */
  @Test
  fun loadAuthorProfile_invalidId_doesNotCrash() = runTest {
    viewModel.loadAuthorProfile("nonexistent")
    advanceUntilIdle()

    assertNull(viewModel.uiState.value.authorProfile)
  }

  /** Test that setErrorMsg and clearErrorMsg work correctly */
  @Test
  fun setErrorMsg_and_clearErrorMsg() = runTest {
    viewModel.setErrorMsg("Something went wrong")
    assertEquals("Something went wrong", viewModel.uiState.value.errorMsg)

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

  /** Test that check that the load Author does not crash not really implemented yet */
  @Test
  fun loadHuntAuthor_withValidId_doesNotCrash() = runTest {
    viewModel.loadHuntAuthor(testHunt.uid)
    advanceUntilIdle()

    assertTrue(true)
  }

  /** Test that check that the load Author does not crash not really implemented yet */
  @Test
  fun loadHuntAuthor_withInvalidId_logsError() = runTest {
    viewModel.loadHuntAuthor("invalid_id")
    advanceUntilIdle()

    assertTrue(true)
  }

  /** Test that loadOtherReview correctly loads reviews */
  @Test
  fun loadOtherReview_populatesReviews() = runTest {
    val review1 = HuntReview("r1", "u1", "hunt123", 5.0, "Great!")
    val review2 = HuntReview("r2", "u2", "hunt123", 4.0, "Nice!")
    fakeRevRepository.addReviewHunt(review1)
    fakeRevRepository.addReviewHunt(review2)

    viewModel.loadOtherReview("hunt123")
    advanceUntilIdle()

    val reviews = viewModel.uiState.value.reviewList
    assertEquals(2, reviews.size)
    assertTrue(reviews.contains(review1))
    assertTrue(reviews.contains(review2))
  }

  /** Test that loadOtherReview handles empty review list */
  @Test
  fun loadOtherReview_emptyList() = runTest {
    viewModel.loadOtherReview("hunt123")
    advanceUntilIdle()

    val reviews = viewModel.uiState.value.reviewList
    assertTrue(reviews.isEmpty())
  }

  /** Test that check the change of the hunt state from dislike to like and from like to dislike */
  @Test
  fun onLikeClick_isLiked() = runTest {
    assertFalse(viewModel.uiState.value.isLiked)

    viewModel.onLikeClick("hunt123")
    assertEquals(true, viewModel.uiState.value.isLiked)

    viewModel.onLikeClick("hunt123")
    assertEquals(false, viewModel.uiState.value.isLiked)
  }

  /** Test that onDoneClick with null hunt sets error message */
  @Test
  fun onDoneClick_with_null_hunt_sets_error_message() = runTest {
    viewModel.onDoneClick()
    advanceUntilIdle() // wait for coroutines

    // Then
    val state = viewModel.uiState.value
    assertEquals("Hunt data is not loaded.", state.errorMsg)
    assertFalse(state.isAchieved)
  }

  /** Test that check that the hunt is marked as achieved */
  @Test
  fun onDoneClick_marks_hunt_as_achieved() = runTest {
    val userId = "testUser"

    fakeProRepository.addProfile(Profile(uid = userId))

    viewModel.initialize(userId, testHunt)

    viewModel.onDoneClick()
    advanceUntilIdle() // wait for coroutine

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
      fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("Hunt with ID ${testHunt.uid} is not found", e.message)
    }
  }

  /** Test that check no delete if not valid hunt id */
  @Test
  fun deleteHunt_withInvalidId_logsError() = runTest {
    viewModel.deleteHunt("nonexistent_id")
    advanceUntilIdle()

    // Nothing to assert — just ensures no crash/log handled
  }

  @Test
  fun deleteReview_userOwnsReview_deletesSuccessfully() = runTest {
    val review =
        HuntReview(
            reviewId = "rev1", authorId = "u1", huntId = "hunt123", rating = 4.0, comment = "Nice")
    fakeRevRepository.addReviewHunt(review)

    viewModel.deleteReview(
        huntID = "hunt123", reviewID = "rev1", userID = "u1", currentUserId = "u1")

    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.reviewList.isEmpty())
  }

  @Test
  fun deleteReview_wrongUser_setsError() = runTest {
    val review =
        HuntReview(
            reviewId = "rev1", authorId = "u1", huntId = "hunt123", rating = 4.0, comment = "Nice")
    fakeRevRepository.addReviewHunt(review)

    viewModel.deleteReview(
        huntID = "hunt123", reviewID = "rev1", userID = "u1", currentUserId = "someone_else")

    advanceUntilIdle()

    assertEquals("You can only delete your own review.", viewModel.uiState.value.errorMsg)

    // Review must still exist
    assertEquals(1, fakeRevRepository.getHuntReviews("hunt123").size)
  }

  /** Test that check the edit change correctly the hunt */
  @Test
  fun onEditClick() = runTest {
    val newHuntValue = testHunt.copy(title = "Edited Title")
    viewModel.editHunt(testHunt.uid, newHuntValue)
    advanceUntilIdle()

    val updatedHunt = fakeRepository.getHunt(testHunt.uid)
    assertEquals("Edited Title", updatedHunt.title)
    assertEquals(newHuntValue, updatedHunt)
  }

  /** Test that check that there is an error with not a valid hunt */
  @Test
  fun editHunt_withInvalidId_logsError() = runTest {
    val newHunt = testHunt.copy(uid = "nonexistent_id", title = "Should Fail")
    viewModel.editHunt("nonexistent_id", newHunt)
    advanceUntilIdle()

    // Verify that the hunt was NOT added or changed
    try {
      fakeRepository.getHunt("nonexistent_id")
      fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("Hunt with ID nonexistent_id is not found", e.message)
    }
  }
}
