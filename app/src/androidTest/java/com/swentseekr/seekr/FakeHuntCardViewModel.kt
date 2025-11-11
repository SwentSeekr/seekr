package com.swentseekr.seekr

import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.ui.huntcardview.HuntCardUiState
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeHuntCardViewModel(hunt: Hunt) : HuntCardViewModel() { // Hérite de HuntCardViewModel
  private val _uiState =
      MutableStateFlow(
          HuntCardUiState(
              hunt = hunt,
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
}
