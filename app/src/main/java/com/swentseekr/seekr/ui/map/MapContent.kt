package com.swentseekr.seekr.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.theme.Blue
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Scale
import kotlin.math.roundToInt

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
            tiltGesturesEnabled = true,
            rotationGesturesEnabled = true
        )

    val mapProperties = MapProperties(isMyLocationEnabled = true)

    GoogleMap(
        modifier =
            androidx.compose.ui.Modifier
                .fillMaxSize()
                .testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
        cameraPositionState = cameraPositionState,
        onMapLoaded = onMapLoaded,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {
        LaunchedEffect(mapLoaded, selectedHunt, uiState.isFocused) {
            if (!mapLoaded) return@LaunchedEffect
            val hunt = selectedHunt ?: return@LaunchedEffect

            cameraPositionState.animateToHunt(hunt, uiState.isFocused)
        }

        if (uiState.isFocused && selectedHunt != null) {
            FocusedHuntMarkers(uiState = uiState, selectedHunt = selectedHunt)
        } else {
            OverviewMarkers(hunts = uiState.hunts, onMarkerClick = onMarkerClick)
        }
    }
}

@Composable
private fun OverviewMarkers(
    hunts: List<Hunt>,
    onMarkerClick: (Hunt) -> Unit
) {
    hunts.forEach { hunt ->
        HuntImageMarker(
            hunt = hunt,
            onMarkerClick = onMarkerClick
        )
    }
}

/**
 * Marker that uses the hunt main image as a square rounded icon.
 */
@Composable
private fun HuntImageMarker(
    hunt: Hunt,
    onMarkerClick: (Hunt) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val (icon, setIcon) = remember(hunt.uid) { mutableStateOf<BitmapDescriptor?>(null) }

    // Load and build the rounded bitmap once per hunt
    LaunchedEffect(hunt.mainImageUrl) {
        val imageUrl = hunt.mainImageUrl ?: return@LaunchedEffect

        val sizePx =
            with(density) { MapScreenDefaults.MarkerImageSize.toPx().roundToInt() }
        val cornerRadiusPx =
            with(density) { MapScreenDefaults.MarkerCornerRadius.toPx() }

        val loader = context.imageLoader

        val request =
            ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .size(sizePx) // let Coil scale appropriately
                .scale(Scale.FILL)
                .build()

        val result = loader.execute(request)
        val drawable = result.drawable ?: return@LaunchedEffect

        val roundedBitmap =
            createRoundedMarkerBitmap(
                drawable = drawable,
                sizePx = sizePx,
                cornerRadiusPx = cornerRadiusPx
            )

        setIcon(BitmapDescriptorFactory.fromBitmap(roundedBitmap))
    }

    Marker(
        state = MarkerState(LatLng(hunt.start.latitude, hunt.start.longitude)),
        title = hunt.title,
        snippet = hunt.description,
        icon = icon, // null initially -> Google default, then replaced when loaded
        onClick = {
            onMarkerClick(hunt)
            true
        }
    )
}

@Composable
private fun FocusedHuntMarkers(
    uiState: MapUIState,
    selectedHunt: Hunt
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Marker(
        state = MarkerState(LatLng(selectedHunt.start.latitude, selectedHunt.start.longitude)),
        title = "${MapScreenStrings.StartPrefix}${selectedHunt.start.name}",
        snippet = selectedHunt.start.description.ifBlank { null },
        icon = bitmapDescriptorFromVector(context, R.drawable.ic_start_marker)
    )

    selectedHunt.middlePoints.forEach { point ->
        Marker(
            state = MarkerState(LatLng(point.latitude, point.longitude)),
            title = point.name,
            snippet = point.description.ifBlank { null }
        )
    }

    Marker(
        state = MarkerState(LatLng(selectedHunt.end.latitude, selectedHunt.end.longitude)),
        title = "${MapScreenStrings.EndPrefix}${selectedHunt.end.name}",
        snippet = selectedHunt.end.description.ifBlank { null },
        icon = bitmapDescriptorFromVector(context, R.drawable.ic_end_marker)
    )

    if (uiState.route.isNotEmpty()) {
        Polyline(
            points = uiState.route,
            width = MapScreenDefaults.RouteStrokeWidth,
            color = Blue
        )
    }
}

private suspend fun CameraPositionState.animateToHunt(hunt: Hunt, isFocused: Boolean) {
    if (!isFocused) {
        val target = LatLng(hunt.start.latitude, hunt.start.longitude)
        animate(
            CameraUpdateFactory.newLatLngZoom(
                target,
                MapScreenDefaults.FocusedZoom
            )
        )
        return
    }

    val points =
        buildList {
            add(LatLng(hunt.start.latitude, hunt.start.longitude))
            hunt.middlePoints.forEach { add(LatLng(it.latitude, it.longitude)) }
            add(LatLng(hunt.end.latitude, hunt.end.longitude))
        }

    if (points.size == MapScreenDefaults.UnitPointSize) {
        animate(
            CameraUpdateFactory.newLatLngZoom(
                points.first(),
                MapScreenDefaults.FocusedZoom
            )
        )
    } else {
        val bounds =
            LatLngBounds.Builder()
                .apply { points.forEach { include(it) } }
                .build()

        animate(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                MapScreenDefaults.BoundsPadding
            )
        )
    }
}

/**
 * Creates a square bitmap with rounded corners from a [Drawable].
 */
private fun createRoundedMarkerBitmap(
    drawable: Drawable,
    sizePx: Int,
    cornerRadiusPx: Float
): Bitmap {
    val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Rounded clip path
    val path = android.graphics.Path().apply {
        addRoundRect(
            0f,
            0f,
            sizePx.toFloat(),
            sizePx.toFloat(),
            cornerRadiusPx,
            cornerRadiusPx,
            android.graphics.Path.Direction.CW
        )
    }

    // Clip canvas to rounded shape
    canvas.clipPath(path)

    // Draw image
    drawable.setBounds(0, 0, sizePx, sizePx)
    drawable.draw(canvas)

    // Draw green border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f  // Adjust thickness here
        color = com.swentseekr.seekr.ui.theme.Green.toArgb()
    }

    // Draw the rounded border AFTER clipping
    canvas.drawRoundRect(
        0f,
        0f,
        sizePx.toFloat(),
        sizePx.toFloat(),
        cornerRadiusPx,
        cornerRadiusPx,
        borderPaint
    )

    return output
}


