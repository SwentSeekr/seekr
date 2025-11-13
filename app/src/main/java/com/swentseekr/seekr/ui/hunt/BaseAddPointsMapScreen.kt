package com.swentseekr.seekr.ui.hunt

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.swentseekr.seekr.model.map.Location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseAddPointsMapScreen(
    onDone: (List<Location>) -> Unit,
    initPoints: List<Location> = emptyList(),
    onCancel: () -> Unit,
    testMode: Boolean = false,
) {
  var points by remember { mutableStateOf(initPoints) }
  val cameraPositionState = rememberCameraPositionState()

  var showNameDialog by remember { mutableStateOf(false) }
  var tempLatLng by remember { mutableStateOf<LatLng?>(null) }

  if (testMode) {
    LaunchedEffect(Unit) { points = listOf(Location(0.0, 0.0, "P1"), Location(1.0, 1.0, "P2")) }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(AddPointsMapScreenDefaults.TitleText) },
            navigationIcon = {
              IconButton(
                  onClick = onCancel,
                  modifier = Modifier.testTag(AddPointsMapScreenTestTags.CANCEL_BUTTON)) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = AddPointsMapScreenDefaults.BackContentDescription)
                  }
            })
      },
      bottomBar = {
        Column(Modifier.padding(AddPointsMapScreenDefaults.BottomPadding)) {
          Button(
              onClick = { onDone(points) },
              modifier = Modifier.fillMaxWidth().testTag(AddPointsMapScreenTestTags.CONFIRM_BUTTON),
              enabled = points.size >= 2,
          ) {
            Text("${AddPointsMapScreenDefaults.ConfirmButtonLabel} (${points.size})")
          }
        }
      }) { padding ->
        GoogleMap(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .testTag(AddPointsMapScreenTestTags.MAP_VIEW),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
              tempLatLng = latLng
              showNameDialog = true
            }) {
              points.forEach { point ->
                Marker(
                    state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                    title = point.name)
              }

              if (points.size >= 2) {
                Polyline(
                    points = points.map { LatLng(it.latitude, it.longitude) },
                    color = MaterialTheme.colorScheme.primary)
              }
            }
      }

  PointNameDialog(
      show = showNameDialog && tempLatLng != null,
      onDismiss = { showNameDialog = false },
      onConfirm = { name ->
        tempLatLng?.let { points = points + Location(it.latitude, it.longitude, name) }
        showNameDialog = false
      })
}
