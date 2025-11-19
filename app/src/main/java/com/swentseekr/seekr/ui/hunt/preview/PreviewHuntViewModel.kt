package com.swentseekr.seekr.ui.hunt.preview

import androidx.lifecycle.ViewModel
import com.swentseekr.seekr.ui.hunt.HuntUIState
import kotlinx.coroutines.flow.StateFlow

open class PreviewHuntViewModel(private val sourceState: StateFlow<HuntUIState>) : ViewModel() {

  open val uiState: StateFlow<HuntUIState> = sourceState
}
