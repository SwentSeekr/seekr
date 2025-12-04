package com.swentseekr.seekr.ui.hunt.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.hunt.BaseHuntScreen
import kotlinx.coroutines.launch

@Composable
fun EditHuntScreen(
    huntId: String,
    editHuntViewModel: EditHuntViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    testMode: Boolean = false,
) {
  LaunchedEffect(huntId) {
    if (huntId.isNotBlank()) {
      editHuntViewModel.load(huntId)
    }
  }

  val scope = rememberCoroutineScope()

  BaseHuntScreen(
      title = "Edit your Hunt",
      vm = editHuntViewModel,
      onGoBack = onGoBack,
      onDone = onDone,
      testMode = testMode,
      onSelectImage = { uri -> editHuntViewModel.updateMainImageUri(uri) },
      // NEW: enable delete menu for edit mode
      showDeleteAction = true,
      onDeleteClick = {
        scope.launch {
          editHuntViewModel.deleteCurrentHunt()
          // After delete, go back (you can swap this for a dedicated onDeleted callback if needed)
          onGoBack()
        }
      },
  )
}
