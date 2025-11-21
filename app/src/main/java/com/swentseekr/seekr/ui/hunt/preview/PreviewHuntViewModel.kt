package com.swentseekr.seekr.ui.hunt.preview

import androidx.lifecycle.ViewModel
import com.swentseekr.seekr.ui.hunt.HuntUIState
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for previewing Hunt UI states.
 *
 * This ViewModel takes a [StateFlow] of [HuntUIState] as a source and exposes it directly. It is
 * designed to facilitate the previewing of different UI states in isolation.
 *
 * @property sourceState The source [StateFlow] providing the [HuntUIState].
 */
open class PreviewHuntViewModel(private val sourceState: StateFlow<HuntUIState>) : ViewModel() {

  open val uiState: StateFlow<HuntUIState> = sourceState
}
