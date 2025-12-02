package com.swentseekr.seekr.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.swentseekr.seekr.model.hunt.Hunt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MapScreen(viewModel: MapViewModel = viewModel(), testMode: Boolean = false) {
  val uiState by viewModel.uiState.collectAsState()
  val cameraPositionState = rememberCameraPositionState()
  val scope = rememberCoroutineScope()

  val context = LocalContext.current
  val fused = remember { LocationServices.getFusedLocationProviderClient(context) }

  var mapLoaded by remember { mutableStateOf(false) }
  var previousCameraPosition by remember { mutableStateOf<CameraPosition?>(null) }
  var showStopHuntDialog by remember { mutableStateOf(false) }

  val permissionState = rememberLocationPermissionState(testMode = testMode)
  val selectedHunt = uiState.selectedHunt

  MoveCameraToUserLocationEffect(
      hasLocationPermission = permissionState.hasPermission,
      mapLoaded = mapLoaded,
      context = context,
      fused = fused,
      cameraPositionState = cameraPositionState,
      scope = scope)

  ContinuousDistanceUpdateEffect(
      hasLocationPermission = permissionState.hasPermission,
      uiState = uiState,
      context = context,
      fused = fused,
      viewModel = viewModel)

  RouteAndZoomToNextPointEffect(
      hasLocationPermission = permissionState.hasPermission,
      uiState = uiState,
      context = context,
      fused = fused,
      cameraPositionState = cameraPositionState,
      scope = scope,
      viewModel = viewModel)

  Box(modifier = Modifier.fillMaxSize().testTag(MapScreenTestTags.MAP_SCREEN)) {
    MapContent(
        uiState = uiState,
        cameraPositionState = cameraPositionState,
        mapLoaded = mapLoaded,
        onMapLoaded = { mapLoaded = true },
        selectedHunt = selectedHunt,
        onMarkerClick = viewModel::onMarkerClick)

    LocationPermissionPopup(permissionState = permissionState)

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

    if (uiState.isFocused && selectedHunt != null) {
      FocusedHuntControls(
          uiState = uiState,
          selectedHunt = selectedHunt,
          onStartHunt = { viewModel.startHunt() },
          onValidateCurrentLocation = {
            validateCurrentLocationIfPermitted(
                context = context, fused = fused, viewModel = viewModel)
          },
          onFinishHunt = { onPersist ->
            finishHuntIfLoggedIn(onPersist = onPersist, viewModel = viewModel)
          },
          onBackToAllHunts = { viewModel.onBackToAllHunts() },
          onShowStopHuntDialog = { showStopHuntDialog = true },
      )

      if (showStopHuntDialog) {
        StopHuntDialog(
            onConfirm = {
              showStopHuntDialog = false
              viewModel.onBackToAllHunts()
            },
            onDismiss = { showStopHuntDialog = false })
      }
    }
  }
}

private data class LocationPermissionState(
    val hasPermission: Boolean,
    val requestPermission: () -> Unit
)

@Composable
private fun rememberLocationPermissionState(testMode: Boolean): LocationPermissionState {
  var hasLocationPermission by remember { mutableStateOf(false) }

  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            hasLocationPermission =
                grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
          }

  LaunchedEffect(testMode) {
    if (!testMode) {
      permissionLauncher.launch(
          arrayOf(
              Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
  }

  return LocationPermissionState(
      hasPermission = hasLocationPermission,
      requestPermission = {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
      })
}

@Composable
private fun MoveCameraToUserLocationEffect(
    hasLocationPermission: Boolean,
    mapLoaded: Boolean,
    context: Context,
    fused: FusedLocationProviderClient,
    cameraPositionState: CameraPositionState,
    scope: CoroutineScope
) {
  LaunchedEffect(hasLocationPermission, mapLoaded) {
    if (!mapLoaded || !hasLocationPermission) return@LaunchedEffect
    if (!isLocationPermissionGranted(context)) return@LaunchedEffect

    try {
      fused.lastLocation.addOnSuccessListener { location ->
        location?.let {
          val here = LatLng(it.latitude, it.longitude)
          scope.launch {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(here, MapScreenDefaults.UserLocationZoom))
          }
        }
      }
    } catch (_: SecurityException) {
      return@LaunchedEffect
    }
  }
}

@Composable
private fun LocationPermissionPopup(permissionState: LocationPermissionState) {
  if (!permissionState.hasPermission) {
    PermissionRequestPopup(onRequestPermission = { permissionState.requestPermission() })
  }
}

private fun isLocationPermissionGranted(context: Context): Boolean {
  val fineGranted =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED

  val coarseGranted =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED

  return fineGranted || coarseGranted
}

private fun validateCurrentLocationIfPermitted(
    context: Context,
    fused: FusedLocationProviderClient,
    viewModel: MapViewModel
) {
  if (!isLocationPermissionGranted(context)) return

  try {
    fused.lastLocation.addOnSuccessListener { loc ->
      loc?.let { viewModel.validateCurrentPoint(LatLng(it.latitude, it.longitude)) }
    }
  } catch (_: SecurityException) {
    return
  }
}

private fun finishHuntIfLoggedIn(onPersist: (suspend (Hunt) -> Unit)?, viewModel: MapViewModel) {
  val userId = FirebaseAuth.getInstance().currentUser?.uid
  if (userId != null && onPersist != null) {
    viewModel.finishHunt(onPersist = onPersist)
  }
}

@Composable
private fun ContinuousDistanceUpdateEffect(
    hasLocationPermission: Boolean,
    uiState: MapUIState,
    context: Context,
    fused: FusedLocationProviderClient,
    viewModel: MapViewModel
) {
  DisposableEffect(hasLocationPermission, uiState.isFocused, uiState.selectedHunt?.uid) {
    val shouldTrack =
        hasLocationPermission &&
            uiState.isFocused &&
            uiState.selectedHunt != null &&
            isLocationPermissionGranted(context)

    if (!shouldTrack) {
      return@DisposableEffect onDispose {}
    }

    val callback =
        object : LocationCallback() {
          override fun onLocationResult(result: LocationResult) {

            val loc = result.lastLocation ?: return
            viewModel.updateCurrentDistanceToNext(LatLng(loc.latitude, loc.longitude))
          }
        }

    try {
      val request =
          LocationRequest.Builder(
                  Priority.PRIORITY_HIGH_ACCURACY, MapConfig.LOCATION_UPDATE_INTERVAL_MS)
              .setMinUpdateIntervalMillis(MapConfig.LOCATION_FASTEST_INTERVAL_MS)
              .build()

      fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
    } catch (_: SecurityException) {
      return@DisposableEffect onDispose { fused.removeLocationUpdates(callback) }
    }

    onDispose { fused.removeLocationUpdates(callback) }
  }
}

@Composable
private fun RouteAndZoomToNextPointEffect(
    hasLocationPermission: Boolean,
    uiState: MapUIState,
    context: Context,
    fused: FusedLocationProviderClient,
    cameraPositionState: CameraPositionState,
    scope: CoroutineScope,
    viewModel: MapViewModel
) {
  LaunchedEffect(
      hasLocationPermission,
      uiState.isHuntStarted,
      uiState.validatedCount,
      uiState.selectedHunt?.uid) {
        val hunt = uiState.selectedHunt
        val shouldAdjust =
            hasLocationPermission &&
                uiState.isHuntStarted &&
                hunt != null &&
                isLocationPermissionGranted(context)

        if (!shouldAdjust) return@LaunchedEffect

        try {
          fused.lastLocation.addOnSuccessListener { location ->
            val loc = location ?: return@addOnSuccessListener

            val currentLatLng = LatLng(loc.latitude, loc.longitude)

            val next = nextPointFor(hunt, uiState.validatedCount) ?: return@addOnSuccessListener

            val nextLatLng = LatLng(next.latitude, next.longitude)

            viewModel.routeFromCurrentToNext(currentLatLng)

            val bounds = LatLngBounds.Builder().include(currentLatLng).include(nextLatLng).build()

            scope.launch {
              cameraPositionState.animate(
                  CameraUpdateFactory.newLatLngBounds(bounds, MapScreenDefaults.BoundsPadding))
            }
          }
        } catch (_: SecurityException) {
          return@LaunchedEffect
        }
      }
}
