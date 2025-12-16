package com.swentseekr.seekr.ui.hunt.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.hunt.BaseHuntScreen
import com.swentseekr.seekr.ui.hunt.DeleteAction
import kotlinx.coroutines.launch

/**
 * Screen used to edit an existing hunt.
 *
 * @param huntId Identifier of the hunt to edit.
 * @param editHuntViewModel ViewModel providing the hunt data and edit operations.
 * @param onGoBack Callback invoked when the user navigates back.
 * @param onDone Callback invoked when the edit flow is successfully completed.
 * @param testMode When true, configures the screen for instrumentation testing.
 */
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
      title = EditHuntConstantsStrings.TITLE,
      vm = editHuntViewModel,
      onGoBack = onGoBack,
      onDone = onDone,
      testMode = testMode,
      onSelectImage = { uri -> editHuntViewModel.updateMainImageUri(uri) },
      deleteAction =
          DeleteAction(
              show = true,
              onClick = {
                scope.launch {
                  editHuntViewModel.deleteCurrentHunt()
                  onGoBack()
                }
              },
          ),
  )
}
