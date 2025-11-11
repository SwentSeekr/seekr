package com.swentseekr.seekr.ui.hunt.add

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.hunt.BaseHuntScreen

@Composable
fun AddHuntScreen(
    addHuntViewModel: AddHuntViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    testMode: Boolean = false,
) {
  BaseHuntScreen(
      vm = addHuntViewModel,
      onGoBack = onGoBack,
      onDone = onDone,
      testMode = testMode,
      onSelectImage = { uri -> addHuntViewModel.updateMainImageUri(uri) })
}
