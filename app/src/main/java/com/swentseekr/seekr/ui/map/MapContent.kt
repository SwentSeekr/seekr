package com.swentseekr.seekr.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.theme.Blue
import kotlin.math.roundToInt

/**
 * Main map rendering composable responsible for:
 * - Displaying a GoogleMap instance.
 * - Placing markers depending on whether the map is in overview mode or focused mode.
 * - Animating the camera when a hunt is selected or focused.
 *
 * @param uiState the current state of the map screen, including hunts, focus, and route.
 * @param cameraPositionState the state object controlling the map's camera.
 * @param mapLoaded whether the GoogleMap instance has fully initialized.
 * @param onMapLoaded callback invoked once the map is ready.
 * @param selectedHunt the hunt currently selected by the user.
 * @param onMarkerClick callback invoked when a hunt marker is tapped.
 */
@Composable
fun MapContent(
    uiState: MapUIState,
    cameraPositionState: CameraPositionState,
    mapLoaded: Boolean,
    onMapLoaded: () -> Unit,
    selectedHunt: Hunt?,
    onMarkerClick: (Hunt) -> Unit,
) {
  val mapUiSettings =
      MapUiSettings(
          myLocationButtonEnabled = true,
          scrollGesturesEnabled = true,
          zoomGesturesEnabled = true,
          zoomControlsEnabled = false,
          tiltGesturesEnabled = true,
          rotationGesturesEnabled = true)

  val mapProperties = MapProperties(isMyLocationEnabled = true)

  GoogleMap(
      modifier =
          androidx.compose.ui.Modifier.fillMaxSize().testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
      cameraPositionState = cameraPositionState,
      onMapLoaded = onMapLoaded,
      properties = mapProperties,
      uiSettings = mapUiSettings) {
        // Animate camera when hunt changes or becomes focused
        LaunchedEffect(mapLoaded, selectedHunt, uiState.isFocused) {
          if (!mapLoaded) return@LaunchedEffect
          val hunt = selectedHunt ?: return@LaunchedEffect

          cameraPositionState.animateToHunt(hunt, uiState.isFocused)
        }

        // Show appropriate markers
        if (uiState.isFocused && selectedHunt != null) {
          FocusedHuntMarkers(uiState = uiState, selectedHunt = selectedHunt)
        } else {
          OverviewMarkers(hunts = uiState.hunts, onMarkerClick = onMarkerClick)
        }
      }
}

/**
 * Renders markers for all hunts in a non-focused (overview) mode.
 *
 * @param hunts list of all hunts available.
 * @param onMarkerClick callback when the user taps a hunt marker.
 */
@Composable
private fun OverviewMarkers(hunts: List<Hunt>, onMarkerClick: (Hunt) -> Unit) {
  hunts.forEach { hunt -> HuntImageMarker(hunt = hunt, onMarkerClick = onMarkerClick) }
}

/**
 * Renders a single hunt marker using a rounded image thumbnail loaded from its mainImageUrl.
 *
 * Steps:
 * - Loads the hunt’s image via Coil.
 * - Renders the image into a rounded square bitmap with a border.
 * - Uses the final bitmap as a Google Maps marker icon.
 *
 * @param hunt the hunt whose marker is being displayed.
 * @param onMarkerClick callback triggered when the marker is tapped.
 */
@Composable
private fun HuntImageMarker(hunt: Hunt, onMarkerClick: (Hunt) -> Unit) {
  val context = LocalContext.current
  val density = LocalDensity.current

  val (icon, setIcon) = remember(hunt.uid) { mutableStateOf<BitmapDescriptor?>(null) }

  // Load the hunt’s image and create a rounded icon
  LaunchedEffect(hunt.mainImageUrl) {
    val imageUrl = hunt.mainImageUrl

    val sizePx = with(density) { MapScreenDefaults.MarkerImageSize.toPx().roundToInt() }
    val cornerRadiusPx = with(density) { MapScreenDefaults.MarkerCornerRadius.toPx() }

    val loader = context.imageLoader

    val request =
        ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .size(sizePx)
            .scale(Scale.FILL)
            .build()

    val result = loader.execute(request)
    val drawable = result.drawable ?: return@LaunchedEffect

    val roundedBitmap =
        createRoundedMarkerBitmap(
            drawable = drawable, sizePx = sizePx, cornerRadiusPx = cornerRadiusPx)

    setIcon(BitmapDescriptorFactory.fromBitmap(roundedBitmap))
  }

  Marker(
      state = MarkerState(LatLng(hunt.start.latitude, hunt.start.longitude)),
      title = hunt.title,
      snippet = hunt.description,
      icon = icon,
      onClick = {
        onMarkerClick(hunt)
        true
      })
}

/**
 * Renders detailed markers when a hunt is in focused mode:
 * - Start marker (custom icon)
 * - Middle point markers
 * - End marker (custom icon)
 * - Route polyline connecting visited points
 *
 * @param uiState current UI state containing route data.
 * @param selectedHunt the hunt currently being displayed in focused mode.
 */
@Composable
private fun FocusedHuntMarkers(uiState: MapUIState, selectedHunt: Hunt) {
  val context = LocalContext.current

  // Start marker
  Marker(
      state = MarkerState(LatLng(selectedHunt.start.latitude, selectedHunt.start.longitude)),
      title = "${MapScreenStrings.StartPrefix}${selectedHunt.start.name}",
      snippet = selectedHunt.start.description.ifBlank { null },
      icon = bitmapDescriptorFromVector(context, R.drawable.ic_start_marker))

  // Middle points
  selectedHunt.middlePoints.forEach { point ->
    Marker(
        state = MarkerState(LatLng(point.latitude, point.longitude)),
        title = point.name,
        snippet = point.description.ifBlank { null })
  }

  // End marker
  Marker(
      state = MarkerState(LatLng(selectedHunt.end.latitude, selectedHunt.end.longitude)),
      title = "${MapScreenStrings.EndPrefix}${selectedHunt.end.name}",
      snippet = selectedHunt.end.description.ifBlank { null },
      icon = bitmapDescriptorFromVector(context, R.drawable.ic_end_marker))

  // Route polyline
  if (uiState.route.isNotEmpty()) {
    Polyline(points = uiState.route, width = MapScreenDefaults.RouteStrokeWidth, color = Blue)
  }
}

/**
 * Animates the camera to properly frame the selected hunt.
 *
 * Behavior:
 * - If `isFocused` is false, zooms to the start point only.
 * - If focused, checks whether the hunt has multiple points:
 *     - For a single point, zoom directly.
 *     - For multiple points, compute a LatLngBounds to frame all points.
 *
 * @param hunt the hunt to center on.
 * @param isFocused whether the hunt is currently in detailed mode.
 */
private suspend fun CameraPositionState.animateToHunt(hunt: Hunt, isFocused: Boolean) {
  if (!isFocused) {
    val target = LatLng(hunt.start.latitude, hunt.start.longitude)
    animate(CameraUpdateFactory.newLatLngZoom(target, MapScreenDefaults.FocusedZoom))
    return
  }

  val points = buildList {
    add(LatLng(hunt.start.latitude, hunt.start.longitude))
    hunt.middlePoints.forEach { add(LatLng(it.latitude, it.longitude)) }
    add(LatLng(hunt.end.latitude, hunt.end.longitude))
  }

  if (points.size == MapScreenDefaults.UnitPointSize) {
    animate(CameraUpdateFactory.newLatLngZoom(points.first(), MapScreenDefaults.FocusedZoom))
  } else {
    val bounds = LatLngBounds.Builder().apply { points.forEach { include(it) } }.build()

    animate(CameraUpdateFactory.newLatLngBounds(bounds, MapScreenDefaults.BoundsPadding))
  }
}

/**
 * Creates a rounded-square bitmap to use as a custom map marker.
 *
 * Steps:
 * - Creates an empty ARGB bitmap.
 * - Clips the canvas to a rounded rectangle.
 * - Draws the provided drawable onto the canvas.
 * - Draws a colored border around the rounded rectangle.
 *
 * @param drawable the drawable to render inside the rounded marker.
 * @param sizePx the final width & height of the bitmap.
 * @param cornerRadiusPx the corner radius applied to the marker.
 * @return a bitmap suitable for use with [BitmapDescriptorFactory.fromBitmap].
 */
private fun createRoundedMarkerBitmap(
    drawable: Drawable,
    sizePx: Int,
    cornerRadiusPx: Float
): Bitmap {
  val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(output)

  val path =
      android.graphics.Path().apply {
        addRoundRect(
            0f,
            0f,
            sizePx.toFloat(),
            sizePx.toFloat(),
            cornerRadiusPx,
            cornerRadiusPx,
            android.graphics.Path.Direction.CW)
      }

  canvas.clipPath(path)

  drawable.setBounds(0, 0, sizePx, sizePx)
  drawable.draw(canvas)

  // Add border
  val borderPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = MapScreenDefaults.CustomMarkerBorderWidth
        color = com.swentseekr.seekr.ui.theme.Green.toArgb()
      }

  canvas.drawRoundRect(
      0f, 0f, sizePx.toFloat(), sizePx.toFloat(), cornerRadiusPx, cornerRadiusPx, borderPaint)

  return output
}
