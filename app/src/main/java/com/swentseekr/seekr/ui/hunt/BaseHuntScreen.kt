package com.swentseekr.seekr.ui.hunt

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.swentseekr.seekr.model.map.Location

private const val TOAST_HUNT_SAVED = "Hunt saved successfully!"

@Composable
fun BaseHuntScreen(
    title: String = "Add your Hunt",
    vm: BaseHuntViewModel,
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    testMode: Boolean = false,
) {
  val uiState by vm.uiState.collectAsState()
  val context = LocalContext.current

  if (testMode) {
    LaunchedEffect(Unit) { vm.setTestMode(true) }
  }

  LaunchedEffect(uiState.saveSuccessful) {
    if (uiState.saveSuccessful) {
      Toast.makeText(context, TOAST_HUNT_SAVED, Toast.LENGTH_SHORT).show()
      onDone()
      vm.resetSaveSuccess()
    }
  }

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      vm.clearErrorMsg()
    }
  }

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
        onTitleChange = vm::setTitle,
        onDescriptionChange = vm::setDescription,
        onTimeChange = vm::setTime,
        onDistanceChange = vm::setDistance,
        onDifficultySelect = vm::setDifficulty,
        onStatusSelect = vm::setStatus,
        onSelectLocations = { vm.setIsSelectingPoints(true) },
        onSave = { vm.submit() },
        onGoBack = onGoBack,
    )
  }
}
