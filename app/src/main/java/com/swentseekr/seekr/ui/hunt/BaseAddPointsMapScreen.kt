package com.swentseekr.seekr.ui.hunt

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.map.MapScreenDefaults
import com.swentseekr.seekr.ui.map.PermissionRequestPopup

/**
 * Base screen used to add checkpoints on a map.
 *
 * This composable displays a Google Map allowing the user to place checkpoints by tapping on the
 * map, name them, optionally attach an image, and preview the result before final confirmation.
 *
 * Responsibilities:
 * - Requests and reacts to location permission state.
 * - Centers the map on the user’s current location when permission is granted.
 * - Allows manual placement of checkpoints via map taps.
 * - Opens the system image picker to attach an image to a checkpoint.
 * - Forwards the selected checkpoint location and image URI to the caller.
 *
 * Behavior details:
 * - When location permission is granted, the map re-centers on the user's last known location.
 * - When a checkpoint is added, the image picker is launched and the result is delivered through
 *   [onCheckpointImagePicked].
 * - The map stops auto-centering on the user once the user manually interacts with it.
 *
 * This composable is intentionally stateless with respect to persistence; all checkpoint handling
 * and final validation are delegated to the caller via callbacks.
 *
 * @param onDone Called when the user finishes adding checkpoints and confirms the action.
 * @param initPoints Optional initial list of points to display on the map. Useful when editing an
 *   existing hunt or restoring UI state.
 * @param onCancel Called when the user cancels checkpoint creation.
 * @param testMode When true, skips certain behaviors (e.g., location re-centering) to ensure
 *   reproducible results during automated tests.
 * @param onCheckpointImagePicked Called when an image is selected for a checkpoint, providing the
 *   checkpoint [Location] and the selected image [Uri].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseAddPointsMapScreen(
    onDone: (List<Location>) -> Unit,
    initPoints: List<Location> = emptyList(),
    onCancel: () -> Unit,
    testMode: Boolean = false,
    onCheckpointImagePicked: (Location, Uri?) -> Unit = { _, _ -> }
) {
  var points by remember { mutableStateOf(initPoints) }
  val cameraPositionState = rememberCameraPositionState()

  val context = LocalContext.current
  val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
  var hasLocationPermission by remember { mutableStateOf(false) }
  val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

  var mapLoaded by remember { mutableStateOf(false) }

  val permissionLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          granted ->
        hasLocationPermission = granted
      }

  LaunchedEffect(Unit) {
    hasLocationPermission =
        ContextCompat.checkSelfPermission(context, locationPermission) ==
            PackageManager.PERMISSION_GRANTED
  }

  LaunchedEffect(hasLocationPermission, mapLoaded) {
    if (!hasLocationPermission || !mapLoaded) return@LaunchedEffect

    try {
      fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
          val target = LatLng(location.latitude, location.longitude)
          cameraPositionState.move(
              CameraUpdateFactory.newLatLngZoom(target, MapScreenDefaults.UserLocationZoom))
        }
      }
    } catch (_: SecurityException) {
      return@LaunchedEffect
    }
  }

  var showNameDialog by remember { mutableStateOf(false) }
  var tempLatLng by remember { mutableStateOf<LatLng?>(null) }

  var pendingLocation by rememberSaveable { mutableStateOf<Location?>(null) }

  val imagePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
          onResult = { uri: Uri? ->
            val location = pendingLocation
            if (location != null) {
              onCheckpointImagePicked(location, uri)
            }
            pendingLocation = null
          })

  if (testMode) {
    LaunchedEffect(Unit) { points = listOf(Location(BaseHuntConstantsDefault.DEFAULT_LATITUDE_1,
        BaseHuntConstantsDefault.DEFAULT_LONGITUDE_1, BaseHuntFieldsStrings.NAME_1),
        Location(BaseHuntConstantsDefault.DEFAULT_LATITUDE_2,BaseHuntConstantsDefault.DEFAULT_LONGITUDE_2,
        BaseHuntFieldsStrings.NAME_2)) }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(AddPointsMapScreenDefaults.TITLE_TEXT) },
            navigationIcon = {
              IconButton(
                  onClick = onCancel,
                  modifier = Modifier.testTag(AddPointsMapScreenTestTags.CANCEL_BUTTON)) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = AddPointsMapScreenDefaults.BACK_CONTENT_DESCRIPTION)
                  }
            })
      },
      bottomBar = {
        Column(Modifier.padding(AddPointsMapScreenDefaults.BottomPadding)) {
          Button(
              onClick = { onDone(points) },
              modifier = Modifier.fillMaxWidth().testTag(AddPointsMapScreenTestTags.CONFIRM_BUTTON),
              enabled = points.size >= MapScreenDefaults.MinScore,
          ) {
            Text("${AddPointsMapScreenDefaults.CONFIRM_BUTTON_LABEL} (${points.size})")
          }
        }
      }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
          GoogleMap(
              modifier = Modifier.fillMaxSize().testTag(AddPointsMapScreenTestTags.MAP_VIEW),
              cameraPositionState = cameraPositionState,
              onMapLoaded = { mapLoaded = true },
              properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
              onMapClick = { latLng ->
                tempLatLng = latLng
                showNameDialog = true
              }) {
                points.forEach { point ->
                  Marker(
                      state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                      title = point.name,
                      snippet = point.description.ifBlank { null })
                }

                if (points.size >= BaseHuntConstantsDefault.POLYLINE) {
                  Polyline(
                      points = points.map { LatLng(it.latitude, it.longitude) },
                      color = MaterialTheme.colorScheme.primary)
                }
              }

          if (!hasLocationPermission) {
            PermissionRequestPopup(
                onRequestPermission = { permissionLauncher.launch(locationPermission) })
          }
        }
      }

  PointNameDialog(
      show = showNameDialog && tempLatLng != null,
      onDismiss = { showNameDialog = false },
      onConfirm = { name, description ->
        tempLatLng?.let { latLng ->
          val newLocation = Location(latLng.latitude, latLng.longitude, name, description)
          points = points + newLocation
          pendingLocation = newLocation
          imagePickerLauncher.launch(BaseHuntFieldsStrings.IMAGE_LAUNCH)
        }
        showNameDialog = false
      })
}
