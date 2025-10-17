package com.swentseekr.seekr.ui.addhunt

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

// ----------------------
// Constants
// ----------------------
private const val TOAST_MIN_POINTS = "Please select at least a start and end point."

@Composable
fun AddHuntScreen(
    addHuntViewModel: AddHuntViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    testMode: Boolean = false
) {
  val uiState by addHuntViewModel.uiState.collectAsState()
  val context = LocalContext.current

  if (testMode) {
    // Active le mode test sur le ViewModel si possible
    LaunchedEffect(Unit) { addHuntViewModel.setTestMode(true) }
  }

  // Handle successful addition
  LaunchedEffect(uiState.saveSuccessful) {
    if (uiState.saveSuccessful) {
      Toast.makeText(context, "Hunt added successfully!", Toast.LENGTH_SHORT).show()
      onDone()
    }
  }

  // Handle error toast
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      addHuntViewModel.clearErrorMsg()
    }
  }

  if (uiState.isSelectingPoints) {
    AddPointsMapScreen(
        initPoints = uiState.points,
        onDone = { locations ->
          val points = locations.toMutableList()
          if (!addHuntViewModel.setPoints(points)) {
            Toast.makeText(context, "Failed to set points. Please try again.", Toast.LENGTH_SHORT)
                .show()
            return@AddPointsMapScreen
          }
          addHuntViewModel.setIsSelectingPoints(false)
        },
        onCancel = { addHuntViewModel.setIsSelectingPoints(false) },
        testMode = testMode)
  } else {
    AddHuntFieldsScreen(
        uiState = uiState,
        onTitleChange = addHuntViewModel::setTitle,
        onDescriptionChange = addHuntViewModel::setDescription,
        onTimeChange = addHuntViewModel::setTime,
        onDistanceChange = addHuntViewModel::setDistance,
        onDifficultySelect = addHuntViewModel::setDifficulty,
        onStatusSelect = addHuntViewModel::setStatus,
        onSelectLocations = { addHuntViewModel.setIsSelectingPoints(true) },
        onSave = { addHuntViewModel.addHunt() },
        onGoBack = onGoBack)
  }
}
