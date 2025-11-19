package com.swentseekr.seekr.ui.preview

import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.HuntUIState
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePreviewHuntViewModel(
    initialState: HuntUIState =
        HuntUIState(
            title = "Treasure Hunt",
            description = "A fun adventure",
            time = "30",
            distance = "2.5",
            difficulty = Difficulty.INTERMEDIATE,
            status = HuntStatus.FUN,
            points =
                listOf(Location(1.0, 1.0, "A"), Location(2.0, 2.0, "B"), Location(3.0, 3.0, "C")),
            mainImageUrl = "",
            otherImagesUris = emptyList(),
            reviewRate = 4.5),
    sourceState: StateFlow<HuntUIState>
) : PreviewHuntViewModel(sourceState) {

  private val _fakeState = MutableStateFlow(initialState)
  override val uiState: StateFlow<HuntUIState> = _fakeState

  fun setState(state: HuntUIState) {
    _fakeState.value = state
  }

  fun buildHunt(state: HuntUIState): Hunt {
    return Hunt(
        uid = "fake_id",
        start = state.points.firstOrNull() ?: Location(0.0, 0.0, "1"),
        end = state.points.lastOrNull() ?: Location(0.0, 0.0, "2"),
        middlePoints = state.points.drop(1).dropLast(1),
        status = state.status ?: HuntStatus.FUN,
        title = state.title,
        description = state.description,
        time = state.time.toDoubleOrNull() ?: 0.0,
        distance = state.distance.toDoubleOrNull() ?: 0.0,
        difficulty = state.difficulty ?: Difficulty.EASY,
        authorId = "fake_author",
        mainImageUrl = state.mainImageUrl,
        otherImagesUrls = state.otherImagesUrls,
        reviewRate = state.reviewRate)
  }
}
