package com.swentseekr.seekr.ui.huntCardScreen

import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.ui.huntcardview.HuntCardUiState
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeHuntCardViewModel(hunt: Hunt) : HuntCardViewModel() { // Inherit from HuntCardViewModel
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

  fun setLikedHunts(hunts: Set<String>) {
    _likedHuntsCache.value = hunts
    _uiState.value = _uiState.value.copy(isLiked = hunts.contains(_uiState.value.hunt?.uid ?: ""))
  }

  override fun onLikeClick(huntID: String) {
    val curr = _uiState.value
    _uiState.value = curr.copy(isLiked = !curr.isLiked)
  }

  override fun loadCurrentUserID() {
    _uiState.value = _uiState.value.copy(currentUserId = "fakeUser123")
  }

  override fun loadHunt(huntId: String) {
    _uiState.value = _uiState.value.copy(hunt = _uiState.value.hunt?.copy(uid = huntId))
  }

  override fun loadOtherReview(huntId: String) {}

  override fun loadAuthorProfile(authorId: String) {}

  fun setLiked(liked: Boolean) {
    _uiState.value = _uiState.value.copy(isLiked = liked)
  }
}
