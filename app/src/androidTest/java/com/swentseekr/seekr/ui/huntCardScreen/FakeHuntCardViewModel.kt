package com.swentseekr.seekr.ui.huntCardScreen

import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.ui.huntcardview.HuntCardUiState
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/* A fake implementation of HuntCardViewModel for testing purposes. */
class FakeHuntCardViewModel(
    hunt: Hunt?,
    private val onDeleteReviewCallback:
        ((huntId: String, reviewId: String, authorId: String, currentUserId: String) -> Unit)? =
        null
) : HuntCardViewModel() { // Inherit from HuntCardViewModel
  var freezeReviews = false

  private val _uiState =
      MutableStateFlow(
          HuntCardUiState(
              hunt = hunt,
              isLiked = false,
              reviewList =
                  List(10) { index ->
                    HuntReview(
                        reviewId = "review$index",
                        authorId = "author$index",
                        huntId = "hunt123",
                        rating = 4.0 + (index % 2),
                        comment = "This is review number $index",
                        photos = emptyList())
                  }))
  override val uiState: StateFlow<HuntCardUiState> = _uiState
  override val likedHuntsCache: StateFlow<Set<String>> = _likedHuntsCache

  fun setHuntAuthor(authorId: String) {
    _uiState.value = _uiState.value.copy(hunt = _uiState.value.hunt?.copy(authorId = authorId))
  }

  fun setLikedHunts(hunts: Set<String>) {
    _likedHuntsCache.value = hunts
    _uiState.value = _uiState.value.copy(isLiked = hunts.contains(_uiState.value.hunt?.uid ?: ""))
  }

  override fun onLikeClick(huntID: String) {
    val curr = _uiState.value
    _uiState.value = curr.copy(isLiked = !curr.isLiked)
  }

  override fun loadCurrentUserID() {
    if (!freezeReviews) {
      _uiState.value = _uiState.value.copy(currentUserId = "fakeUser123")
    }
  }

  override fun loadHunt(huntId: String) {
    if (!freezeReviews) {
      _uiState.value = _uiState.value.copy(hunt = _uiState.value.hunt?.copy(uid = huntId))
    }
  }

  override fun loadOtherReview(huntId: String) {}

  override fun loadAuthorProfile(authorId: String) {}

  fun setLiked(liked: Boolean) {
    _uiState.value = _uiState.value.copy(isLiked = liked)
  }

  fun setCurrentUserIdForTest(id: String) {
    _uiState.value = _uiState.value.copy(currentUserId = id)
  }

  fun setReviewsForTest(reviews: List<HuntReview>) {
    _uiState.value = _uiState.value.copy(reviewList = reviews)
  }
}
