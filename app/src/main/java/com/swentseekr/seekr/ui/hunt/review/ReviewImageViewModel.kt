package com.swentseekr.seekr.ui.hunt.review

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ReviewImageUIState(val photos: List<String> = emptyList())

class ReviewImageViewModel() : ViewModel() {
  private val _uiState = MutableStateFlow(ReviewImageUIState())
  val uiState: StateFlow<ReviewImageUIState> = _uiState
  // private val _photos = MutableStateFlow<List<String>>(emptyList())
  // val photos: StateFlow<List<String>> = _photos

  fun setPhotos(photoUrls: List<String>) {
    _uiState.value = ReviewImageUIState(photos = photoUrls)
  }
}
