package com.swentseekr.seekr.ui.addhunt

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

object AddPointsMapScreenTestTags {
    const val CONFIRM_BUTTON = "ConfirmButton"
    const val MAP_VIEW = "MapView"

    const val CANCEL_BUTTON = "CancelButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPointsMapScreen(
    onDone: (List<Location>) -> Unit,
    initPoints: List<Location> = emptyList(),
    onCancel: () -> Unit
) {
    var points by remember { mutableStateOf(initPoints) }
    val cameraPositionState = rememberCameraPositionState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Hunt Points") },
                navigationIcon = {
                    IconButton(onClick = onCancel, modifier = Modifier.testTag(AddPointsMapScreenTestTags.CANCEL_BUTTON)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column(Modifier.padding(16.dp)) {
                Button(
                    onClick = { onDone(points) },
                    modifier = Modifier.fillMaxWidth().testTag(AddPointsMapScreenTestTags.CONFIRM_BUTTON),
                    enabled = points.size >= 2,

                ) {
                    Text("Confirm Points (${points.size})")
                }
            }
        }
    ) { padding ->
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag(AddPointsMapScreenTestTags.MAP_VIEW),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                points = points + Location(latLng.latitude, latLng.longitude, "Point ${points.size + 1}")
            }
        ) {
            points.forEach { point ->
                Marker(state = MarkerState(position = LatLng(point.latitude, point.longitude)), title = point.name)
            }
            if (points.size >= 2) {
                Polyline(points = points.map { LatLng(it.latitude, it.longitude) }, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
