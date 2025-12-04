package com.swentseekr.seekr.ui.hunt

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntScreen
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntViewModel

data class DeleteAction(
    val show: Boolean = false,
    val onClick: (() -> Unit)? = null,
)

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

  // Preview dialog state
  var showPreview by remember { mutableStateOf(false) }

  // Test mode
  if (testMode) {
    LaunchedEffect(Unit) { vm.setTestMode(true) }
  }

  // Toast when the hunt is saved
  LaunchedEffect(uiState.saveSuccessful) {
    if (uiState.saveSuccessful) {
      Toast.makeText(context, BaseHuntScreenMessages.HUNT_SAVED, Toast.LENGTH_SHORT).show()
      onDone()
      vm.resetSaveSuccess()
    }
  }

  // Toast for errors
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      vm.clearErrorMsg()
    }
  }

  // Preview Screen before submitting
  if (showPreview) {
    val previewVm = remember { PreviewHuntViewModel(vm.uiState) }

    PreviewHuntScreen(
        viewModel = previewVm,
        onConfirm = { vm.submit() },
        onGoBack = { showPreview = false },
    )
    return
  }

  // Map : point selection
  if (uiState.isSelectingPoints) {
    BaseAddPointsMapScreen(
        initPoints = uiState.points,
        onDone = { locations: List<Location> ->
          val ok = vm.setPoints(locations)
          if (ok) vm.setIsSelectingPoints(false)
        },
        onCancel = { vm.setIsSelectingPoints(false) },
        testMode = testMode,
    )
  } else {
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
                onRemoveOtherImage = vm::removeOtherImage, // local URIs
                onRemoveExistingImage = vm::removeExistingOtherImage, // Firestore URLs
            ),
        navigationCallbacks = HuntNavigationCallbacks(onGoBack = onGoBack),
        deleteAction = deleteAction,
    )
  }
}
