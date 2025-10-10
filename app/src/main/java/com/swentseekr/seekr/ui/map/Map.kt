package com.swentseekr.seekr.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "mapScreen"
}

/**
 * The main composable function for displaying the map screen.
 *
 * This screen displays a Google Map with markers for each available treasure hunt. It observes the
 * UI state from the [MapViewModel] to get the list of hunts and the target location for the map's
 * camera.
 *
 * @param viewModel The instance of [MapViewModel] used to manage the state of this screen. It
 *   defaults to a new instance provided by `viewModel()`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
      content = { pd ->
        val cameraPositionState = rememberCameraPositionState {
          position = CameraPosition.fromLatLngZoom(uiState.target, 13f)
        }
        GoogleMap(
            modifier =
                Modifier.fillMaxSize().padding(pd).testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
            cameraPositionState = cameraPositionState) {
              uiState.hunts.forEach { hunt ->
                Marker(
                    state =
                        MarkerState(position = LatLng(hunt.start.latitude, hunt.start.longitude)),
                    title = hunt.title,
                    snippet = hunt.description)
              }
            }
      })
}
