package com.swentseekr.seekr.ui.hunt.add

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.hunt.BaseHuntScreen

/**
 * Composable screen for adding a new hunt.
 *
 * This screen uses [BaseHuntScreen] internally and connects it with [AddHuntViewModel] for handling
 * UI state and actions.
 *
 * @param addHuntViewModel The ViewModel responsible for managing the state of this screen. Defaults
 *   to a [AddHuntViewModel] instance obtained via [viewModel].
 * @param onGoBack Lambda invoked when the user wants to navigate back. Defaults to an empty lambda.
 * @param onDone Lambda invoked when the user finishes adding a hunt. Defaults to an empty lambda.
 * @param testMode Boolean flag to enable test mode. Defaults to false.
 */
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
