package com.swentseekr.seekr.ui.map

import android.content.Context
import android.graphics.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.ui.theme.Green
import kotlinx.coroutines.launch

/**
 * Test tags used by instrumented tests to target key UI elements on the Map screen.
 *
 * These tags are applied to:
 * - [GOOGLE_MAP_SCREEN]: the `GoogleMap` composable root.
 * - [POPUP_CARD], [POPUP_TITLE], [POPUP_DESC]: the bottom popup and its content shown after a
 *   marker tap.
 * - [BUTTON_VIEW], [BUTTON_CANCEL]: actions in the popup.
 * - [BUTTON_BACK]: "Back to all hunts" shown in focused mode.
 */
object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "mapScreen"
  const val POPUP_CARD = "huntPopupCard"
  const val POPUP_TITLE = "huntPopupTitle"
  const val POPUP_DESC = "huntPopupDesc"
  const val BUTTON_CANCEL = "huntPopupCancel"
  const val BUTTON_VIEW = "huntPopupView"
  const val BUTTON_BACK = "backToAllHunts"
}

/**
 * Top-level composable for the Map screen.
 *
 * Responsibilities:
 * - Renders a Google Map and reacts to [MapViewModel.uiState] changes to display markers.
 * - In overview mode: shows one marker per hunt (start).
 * - In focused mode: shows the selected hunt’s start, middle checkpoints, and end.
 * - Animates the camera when a hunt is selected (zoom in) and when a hunt is viewed (fit bounds).
 * - Exposes a bottom popup to “View Hunt” or “Cancel”; “Cancel” restores the previous camera.
 *
 * Side effects and animation are driven by [LaunchedEffect] keyed to `selectedHunt`, `isFocused`,
 * and the map’s loaded state.
 *
 * @param viewModel the screen view model providing [MapUIState] and user intents.
 */
@Composable
fun MapScreen(viewModel: MapViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsState()
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 2f)
  }
  val scope = rememberCoroutineScope()
  var mapLoaded by remember { mutableStateOf(false) }
  var previousCameraPosition by remember { mutableStateOf<CameraPosition?>(null) }

  val selectedHunt = uiState.selectedHunt

  Box(Modifier.fillMaxSize()) {
    GoogleMap(
        modifier = Modifier.matchParentSize().testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
        cameraPositionState = cameraPositionState,
        onMapLoaded = { mapLoaded = true }) {
        LaunchedEffect(mapLoaded, selectedHunt, uiState.isFocused) {
            if (!mapLoaded) return@LaunchedEffect
            val hunt = selectedHunt ?: return@LaunchedEffect

            if (!uiState.isFocused) {
                if (previousCameraPosition == null) {
                    previousCameraPosition = cameraPositionState.position
                }
                val target = LatLng(hunt.start.latitude, hunt.start.longitude)
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, 15f))
            } else {
                val points = buildList {
                    add(LatLng(hunt.start.latitude, hunt.start.longitude))
                    hunt.middlePoints.forEach { add(LatLng(it.latitude, it.longitude)) }
                    add(LatLng(hunt.end.latitude, hunt.end.longitude))
                }

                if (points.size == 1) {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(points.first(), 15f)
                    )
                } else {
                    val bounds = LatLngBounds.Builder().apply { points.forEach { include(it) } }.build()
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(bounds, 100)
                    )
                }
            }
        }

          if (uiState.isFocused && selectedHunt != null) {
            Marker(
                state = MarkerState(LatLng(selectedHunt.start.latitude, selectedHunt.start.longitude)),
                title = "Start: ${selectedHunt.title}",
                icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.ic_start_marker))
              selectedHunt.middlePoints.forEachIndexed { idx, point ->
              Marker(
                  state = MarkerState(LatLng(point.latitude, point.longitude)), title = point.name)
            }
            Marker(
                state = MarkerState(LatLng(selectedHunt.end.latitude, selectedHunt.end.longitude)),
                title = "End: ${selectedHunt.title}",
                icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.ic_end_marker))
          } else {
            uiState.hunts.forEach { hunt ->
              Marker(
                  state = MarkerState(LatLng(hunt.start.latitude, hunt.start.longitude)),
                  title = hunt.title,
                  snippet = hunt.description,
                  onClick = {
                    viewModel.onMarkerClick(hunt)
                    true
                  })
            }
          }
        }

    if (selectedHunt != null && !uiState.isFocused) {
      HuntPopup(
          hunt = selectedHunt,
          onViewClick = { viewModel.onViewHuntClick() },
          onDismiss = {
            scope.launch {
              previousCameraPosition?.let { prev ->
                cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(prev))
              }
              previousCameraPosition = null
              viewModel.onBackToAllHunts()
            }
          })
    }

    if (uiState.isFocused) {

      Button(
          onClick = { viewModel.onBackToAllHunts() },
          colors =
              ButtonDefaults.textButtonColors(
                  containerColor = Green, contentColor = Color.White),
          modifier =
              Modifier.align(Alignment.TopStart)
                  .padding(12.dp)
                  .testTag(MapScreenTestTags.BUTTON_BACK)) {
            Text("Back to all hunts")
          }
    }
  }
}

/**
 * Bottom popup presented after tapping a hunt marker in overview mode.
 *
 * Shows the hunt title and description with two actions:
 * - **View Hunt**: switches to focused mode (start, checkpoints, end) and triggers camera fit.
 * - **Cancel**: closes the popup and restores the previous camera position (no focus).
 *
 * @param hunt the selected hunt to preview.
 * @param onViewClick callback invoked when the user chooses to view the full hunt.
 * @param onDismiss callback invoked when the user cancels the preview.
 */
@Composable
fun HuntPopup(hunt: Hunt, onViewClick: () -> Unit, onDismiss: () -> Unit) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag(MapScreenTestTags.POPUP_CARD),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(8.dp)) {
        Column(Modifier.padding(16.dp)) {
          Text(
              hunt.title,
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.testTag(MapScreenTestTags.POPUP_TITLE))
          Text(
              hunt.description,
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 2,
              modifier = Modifier.testTag(MapScreenTestTags.POPUP_DESC))
          Spacer(Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Green),
                modifier = Modifier.testTag(MapScreenTestTags.BUTTON_CANCEL)) {
                  Text("Cancel")
                }
            Button(
                onClick = onViewClick,
                colors =
                    ButtonDefaults.textButtonColors(
                        containerColor = Green, contentColor = Color.White),
                modifier = Modifier.testTag(MapScreenTestTags.BUTTON_VIEW)) {
                  Text("View Hunt")
                }
          }
        }
      }
}

/**
 * Converts a vector drawable resource into a [BitmapDescriptor] usable by Google Maps markers.
 *
 * This helper reads the drawable, renders it to a bitmap using a temporary [Canvas], and wraps it
 * as a [BitmapDescriptor]. Prefer calling this from a composable with a cached [Context] (e.g., via
 * `LocalContext.current`) and cache the result with `remember { ... }` to avoid recomposition
 * overhead.
 *
 * @param context an Android context used to resolve the drawable resource.
 * @param vectorResId the vector drawable resource ID (e.g., `R.drawable.ic_start_marker`).
 * @return a [BitmapDescriptor] suitable for the `icon` parameter of [Marker].
 */
fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
  val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
  vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
  val bitmap = createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
  val canvas = Canvas(bitmap)
  vectorDrawable.draw(canvas)
  return BitmapDescriptorFactory.fromBitmap(bitmap)
}
