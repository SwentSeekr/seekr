package com.swentseekr.seekr.ui.review

import com.swentseekr.seekr.ui.hunt.review.ReviewImageViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewImageViewModelTest {
  @Test
  fun initial_state_should_have_empty_photos() = runTest {
    val viewModel = ReviewImageViewModel()

    val state = viewModel.uiState.value

    assertEquals(emptyList<String>(), state.photos)
  }

  @Test
  fun setPhotos_should_update_UI_state() = runTest {
    val viewModel = ReviewImageViewModel()

    val samplePhotos = listOf("https://example.com/photo1.jpg", "https://example.com/photo2.jpg")

    viewModel.setPhotos(samplePhotos)

    val state = viewModel.uiState.value

    assertEquals(samplePhotos, state.photos)
  }
}
