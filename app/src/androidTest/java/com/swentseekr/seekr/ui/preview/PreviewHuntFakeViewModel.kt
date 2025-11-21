package com.swentseekr.seekr.ui.preview

import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.ui.hunt.HuntUIState
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePreviewHuntViewModel(
    initialState: HuntUIState = PreviewTestConstants.exampleUiState,
    sourceState: StateFlow<HuntUIState>
) : PreviewHuntViewModel(sourceState) {

  private val _fakeState = MutableStateFlow(initialState)
  override val uiState: StateFlow<HuntUIState> = _fakeState

  fun updateState(state: HuntUIState) {
    _fakeState.value = state
  }
}
