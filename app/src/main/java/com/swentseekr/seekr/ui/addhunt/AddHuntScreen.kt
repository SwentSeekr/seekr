package com.swentseekr.seekr.ui.addhunt

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun AddHuntScreen(
    addHuntViewModel: AddHuntViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {}
) {
    var isSelectingPoints by remember { mutableStateOf(false) }
    val uiState by addHuntViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle error toast
    LaunchedEffect(uiState.errorMsg) {
        uiState.errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            addHuntViewModel.clearErrorMsg()
        }
    }

    if (isSelectingPoints) {
        AddPointsMapScreen(
            initPoints = uiState.points,
            onDone = { locations ->
                val points = locations.toMutableList()
                if (points.size < 2) {
                    Toast.makeText(context, "Please select at least a start and end point.", Toast.LENGTH_SHORT).show()
                    return@AddPointsMapScreen
                }
                addHuntViewModel.setPoints(points)
                isSelectingPoints = false
            },
            onCancel = { isSelectingPoints = false }
        )
    } else {
        AddHuntFieldsScreen(
            uiState = uiState,
            onTitleChange = addHuntViewModel::setTitle,
            onDescriptionChange = addHuntViewModel::setDescription,
            onTimeChange = addHuntViewModel::setTime,
            onDistanceChange = addHuntViewModel::setDistance,
            onDifficultySelect = addHuntViewModel::setDifficulty,
            onStatusSelect = addHuntViewModel::setStatus,
            onSelectLocations = { isSelectingPoints = true },
            onSave = {
                if (addHuntViewModel.addHunt()) onDone()
            },
            onGoBack = onGoBack
        )
    }
}