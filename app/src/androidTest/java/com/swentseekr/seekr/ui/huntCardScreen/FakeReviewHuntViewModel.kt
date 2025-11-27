package com.swentseekr.seekr

import com.swentseekr.seekr.ui.hunt.review.ReviewHuntUIState
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeReviewHuntViewModel : ReviewHuntViewModel() {

  private val _uiState = MutableStateFlow(ReviewHuntUIState())
  override val uiState: StateFlow<ReviewHuntUIState> = _uiState

  override fun loadHunt(huntId: String) {}

  override fun loadAuthorProfile(authorId: String) {}
}
