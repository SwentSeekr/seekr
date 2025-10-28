package com.swentseekr.seekr.ui.hunt.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.hunt.BaseHuntScreen

@Composable
fun EditHuntScreen(
    huntId: String,
    editHuntViewModel: EditHuntViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    testMode: Boolean = false,
) {
  LaunchedEffect(huntId) { editHuntViewModel.load(huntId) }

  BaseHuntScreen(
      vm = editHuntViewModel,
      onGoBack = onGoBack,
      onDone = onDone,
      testMode = testMode,
  )
}
