package com.swentseekr.seekr.ui.hunt

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntScreen
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntViewModel

/**
 * Configuration for displaying a delete action in the hunt UI.
 *
 * @param show Whether the delete option should be visible.
 * @param onClick Callback invoked when the delete action is confirmed.
 */
data class DeleteAction(
    val show: Boolean = false,
    val onClick: (() -> Unit)? = null,
)

/**
 * High-level container for the "add/edit hunt" flow.
 *
 * This composable orchestrates:
 * - Test mode configuration.
 * - Toasts for success and error messages.
 * - Preview screen before submission.
 * - Map-based point selection.
 * - The main hunt fields screen.
 *
 * @param title Title displayed in the top app bar.
 * @param vm ViewModel providing the state and actions for the hunt being edited.
 * @param onGoBack Callback invoked when the user navigates back.
 * @param onDone Callback invoked when the hunt has been successfully saved.
 * @param testMode When true, configures the screen for instrumentation testing.
 * @param onSelectImage Callback invoked when a main image is selected.
 * @param deleteAction Configuration for the delete action displayed in the UI.
 */
@Composable
fun BaseHuntScreen(
    title: String = "Add your Hunt",
    vm: BaseHuntViewModel,
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    testMode: Boolean = false,
    onSelectImage: (Uri?) -> Unit = {},
    deleteAction: DeleteAction = DeleteAction(),
) {
  val uiState by vm.uiState.collectAsState()
  val context = LocalContext.current

  // Controls whether the preview screen is displayed.
  var showPreview by remember { mutableStateOf(false) }

  // Enable test-specific behavior when requested.
  if (testMode) {
    LaunchedEffect(Unit) { vm.setTestMode(true) }
  }

  // Display a toast and trigger completion when the hunt is successfully saved.
  LaunchedEffect(uiState.saveSuccessful) {
    if (uiState.saveSuccessful) {
      Toast.makeText(context, BaseHuntScreenMessages.HUNT_SAVED, Toast.LENGTH_SHORT).show()
      onDone()
      vm.resetSaveSuccess()
    }
  }

  // Display error messages as toasts and clear them in the ViewModel.
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      vm.clearErrorMsg()
    }
  }

  // Preview screen shown before submitting the hunt.
  if (showPreview) {
    val previewVm = remember { PreviewHuntViewModel(vm.uiState) }

    PreviewHuntScreen(
        viewModel = previewVm,
        onConfirm = { vm.submit() },
        onGoBack = { showPreview = false },
    )
    return
  }

  // Map screen for selecting hunt points.
  if (uiState.isSelectingPoints) {
    BaseAddPointsMapScreen(
        initPoints = uiState.points,
        onDone = { locations: List<Location> ->
          val locationsWithImages = vm.attachCheckpointImages(locations)
          val ok = vm.setPoints(locationsWithImages)
          if (ok) vm.setIsSelectingPoints(false)
        },
        onCancel = { vm.setIsSelectingPoints(false) },
        testMode = testMode,
        onCheckpointImagePicked = vm::registerCheckpointImage)
  } else {
    // Main hunt fields screen with callbacks wired to the ViewModel.
    BaseHuntFieldsScreen(
        title = title,
        uiState = uiState,
        fieldCallbacks =
            HuntFieldCallbacks(
                onTitleChange = vm::setTitle,
                onDescriptionChange = vm::setDescription,
                onTimeChange = vm::setTime,
                onDistanceChange = vm::setDistance,
                onDifficultySelect = vm::setDifficulty,
                onStatusSelect = vm::setStatus,
                onSelectLocations = { vm.setIsSelectingPoints(true) },
                onSave = { showPreview = true },
            ),
        imageCallbacks =
            ImageCallbacks(
                onSelectImage = onSelectImage,
                onSelectOtherImages = vm::updateOtherImagesUris,
                onRemoveOtherImage = vm::removeOtherImage, // Local URIs.
                onRemoveExistingImage = vm::removeExistingOtherImage, // Remote URLs (Firestore).
            ),
        navigationCallbacks = HuntNavigationCallbacks(onGoBack = onGoBack),
        deleteAction = deleteAction,
    )
  }
}
