package com.swentseekr.seekr.ui.hunt

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.swentseekr.seekr.model.map.Location

private const val TITLE_TEXT = "Select Hunt Points"
private const val BACK_CONTENT_DESC = "Back"
private const val BUTTON_CONFIRM_LABEL = "Confirm Points"
private const val BOTTOM_PADDING = 16

object AddPointsMapScreenTestTags {
  const val CONFIRM_BUTTON = "ConfirmButton"
  const val MAP_VIEW = "MapView"
  const val CANCEL_BUTTON = "CancelButton"
  const val POINT_NAME_FIELD = "PointNameField"
}

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
            title = { Text(TITLE_TEXT) },
            navigationIcon = {
              IconButton(
                  onClick = onCancel,
                  modifier = Modifier.testTag(AddPointsMapScreenTestTags.CANCEL_BUTTON)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = BACK_CONTENT_DESC)
                  }
            })
      },
      bottomBar = {
        Column(Modifier.padding(BOTTOM_PADDING.dp)) {
          Button(
              onClick = { onDone(points) },
              modifier = Modifier.fillMaxWidth().testTag(AddPointsMapScreenTestTags.CONFIRM_BUTTON),
              enabled = points.size >= 2,
          ) {
            Text("$BUTTON_CONFIRM_LABEL (${points.size})")
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

/** Dialog composable extracted for easier testing and cleaner code. */
@Composable
fun PointNameDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
  if (!show) return

  var pointName by remember { mutableStateOf("") }
  var hasTypedBefore by remember { mutableStateOf(false) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Nom du point") },
      text = {
        Column {
          OutlinedTextField(
              value = pointName,
              onValueChange = {
                if (it.isNotBlank()) hasTypedBefore = true
                pointName = it
              },
              placeholder = { Text("e.g. Musée de l’automobile") },
              singleLine = true,
              isError = hasTypedBefore && pointName.isBlank(),
              label = { Text("Nom du point") },
              supportingText = {
                if (hasTypedBefore && pointName.isBlank()) {
                  Text("Le nom ne peut pas être vide", color = MaterialTheme.colorScheme.error)
                }
              },
              modifier =
                  Modifier.fillMaxWidth().testTag(AddPointsMapScreenTestTags.POINT_NAME_FIELD))
        }
      },
      confirmButton = {
        TextButton(onClick = { onConfirm(pointName.trim()) }, enabled = pointName.isNotBlank()) {
          Text("Ajouter")
        }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } })
}
