package com.swentseekr.seekr.ui.huntCardScreen

import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.huntcardview.HuntCardUiState
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeHuntCardViewModelEmptyReview(hunt: Hunt) : HuntCardViewModel() {
  private val _uiState =
      MutableStateFlow(HuntCardUiState(hunt = hunt, isLiked = false, reviewList = emptyList()))
  override val uiState: StateFlow<HuntCardUiState> = _uiState

  override fun onLikeClick(huntID: String) {
    val curr = _uiState.value
    _uiState.value = curr.copy(isLiked = !curr.isLiked)
  }

  override fun loadHunt(huntId: String) {}

  override fun loadOtherReview(huntId: String) {}

  override fun loadAuthorProfile(authorId: String) {}

  override fun loadCurrentUserID() {}
}
