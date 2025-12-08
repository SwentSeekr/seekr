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

/**
 * High-level map screen composable for the Seekr app.
 *
 * This composable:
 * - Observes [MapUIState] from [MapViewModel].
 * - Manages Google Maps camera position and location-related side effects.
 * - Handles location permission requests and UI popups.
 * - Controls hunt-related UI (start, validate location, finish, stop dialogs).
 *
 * @param viewModel [MapViewModel] used as the source of state and map-related actions.
 * @param testMode [Boolean] flag indicating whether the screen is in test mode (set to `true` to
 *   skip automatic permission requests during tests).
 */
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
      uiState = uiState,
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
        hasLocationPermission = permissionState.hasPermission,
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

/**
 * Simple holder for location permission state and the permission request action.
 *
 * @property hasPermission [Boolean] indicating whether the user has granted location permissions
 *   (fine or coarse).
 * @property requestPermission function of type `() -> Unit` used to trigger the system permission
 *   dialog for location permissions.
 */
private data class LocationPermissionState(
    val hasPermission: Boolean,
    val requestPermission: () -> Unit
)

/**
 * Remembers and manages the location permission state for the map screen.
 *
 * This composable:
 * - Uses an [ActivityResultContracts.RequestMultiplePermissions] launcher.
 * - Tracks whether fine or coarse location permission has been granted.
 * - Automatically triggers a permission request on first composition (unless [testMode] is `true`).
 *
 * @param testMode [Boolean] flag used to disable automatic permission requests in tests. When
 *   `true`, no initial permission request is launched.
 * @return [LocationPermissionState] containing the current permission status and a function to
 *   request permissions.
 */
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

/**
 * Side-effect composable that moves the camera to the user's last known location **only when no
 * hunt is currently selected or active**.
 *
 * This effect:
 * - Runs whenever location permission, map load state, or hunt-selection state changes.
 * - Aborts immediately if:
 *     - The map is not yet loaded.
 *     - Location permission is not granted.
 *     - A hunt is selected or a hunt has already started (in these cases the camera should remain
 *       focused on the hunt instead of the user).
 * - When allowed, queries the last known location from [FusedLocationProviderClient].
 * - Animates the [CameraPositionState] to center on the user's location using the default
 *   user-location zoom level.
 *
 * @param hasLocationPermission whether the app currently has location permission.
 * @param mapLoaded whether the map has finished initializing.
 * @param uiState the current [MapUIState], used to detect selected or active hunts.
 * @param context the context required to verify permission state.
 * @param fused client providing access to the user's last known location.
 * @param cameraPositionState the map camera to animate.
 * @param scope coroutine scope used to perform the animation.
 */
@Composable
private fun MoveCameraToUserLocationEffect(
    hasLocationPermission: Boolean,
    mapLoaded: Boolean,
    uiState: MapUIState,
    context: Context,
    fused: FusedLocationProviderClient,
    cameraPositionState: CameraPositionState,
    scope: CoroutineScope
) {
  LaunchedEffect(hasLocationPermission, mapLoaded, uiState.selectedHunt, uiState.isHuntStarted) {
    if (!mapLoaded || !hasLocationPermission) return@LaunchedEffect
    if (!isLocationPermissionGranted(context)) return@LaunchedEffect

    val hasActiveHunt = uiState.selectedHunt != null || uiState.isHuntStarted
    if (hasActiveHunt) return@LaunchedEffect

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

/**
 * Displays a permission request popup when location permission is not granted.
 *
 * Internally shows [PermissionRequestPopup] as long as [LocationPermissionState.hasPermission] is
 * `false`.
 *
 * @param permissionState [LocationPermissionState] current location permission state with a
 *   function to request permissions.
 */
@Composable
private fun LocationPermissionPopup(permissionState: LocationPermissionState) {
  if (!permissionState.hasPermission) {
    PermissionRequestPopup(onRequestPermission = { permissionState.requestPermission() })
  }
}

/**
 * Checks whether either fine or coarse location permission is granted.
 *
 * @param context [Context] used to query permission status via [ContextCompat.checkSelfPermission].
 * @return [Boolean] `true` if fine or coarse location permission is granted, otherwise `false`.
 */
private fun isLocationPermissionGranted(context: Context): Boolean {
  val fineGranted =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED

  val coarseGranted =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED

  return fineGranted || coarseGranted
}

/**
 * Validates the user's current location if location permission is granted.
 *
 * This function:
 * - Checks for location permission via [isLocationPermissionGranted].
 * - Reads the last known location from [FusedLocationProviderClient].
 * - Forwards the location as a [LatLng] to [MapViewModel.validateCurrentPoint].
 *
 * @param context [Context] used to check location permissions.
 * @param fused [FusedLocationProviderClient] used to obtain the last known location.
 * @param viewModel [MapViewModel] that performs validation of the current point.
 */
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

/**
 * Finishes the current hunt only if the user is logged in and a persistence callback is provided.
 *
 * This function:
 * - Retrieves the current Firebase user via [FirebaseAuth].
 * - If a user ID exists and [onPersist] is non-null, calls [MapViewModel.finishHunt] with the given
 *   persistence callback.
 *
 * @param onPersist optional suspending function of type `suspend (Hunt) -> Unit` that persists the
 *   finished [Hunt] (or handles its completion).
 * @param viewModel [MapViewModel] responsible for finalizing the hunt.
 */
private fun finishHuntIfLoggedIn(onPersist: (suspend (Hunt) -> Unit)?, viewModel: MapViewModel) {
  val userId = FirebaseAuth.getInstance().currentUser?.uid
  if (userId != null && onPersist != null) {
    viewModel.finishHunt(onPersist = onPersist)
  }
}

/**
 * Side-effect composable that continuously updates the distance to the next point while a hunt is
 * focused and location permission is granted.
 *
 * This effect:
 * - Starts listening for location updates via [FusedLocationProviderClient.requestLocationUpdates]
 *   when the user has permission, the map is focused on a hunt, and a hunt is selected.
 * - On each location update, calls [MapViewModel.updateCurrentDistanceToNext] with the user's
 *   current [LatLng].
 * - Cleans up by removing location updates when the effect leaves the composition.
 *
 * @param hasLocationPermission [Boolean] whether location permission is granted.
 * @param uiState [MapUIState] current UI state, including focus and selected hunt.
 * @param context [Context] used to verify permission status.
 * @param fused [FusedLocationProviderClient] used for continuous location updates.
 * @param viewModel [MapViewModel] which receives distance updates to the next point.
 */
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

/**
 * Side-effect composable that routes and zooms the map between the user's current location and the
 * next hunt point once a hunt has started.
 *
 * This effect:
 * - Runs when permission, hunt started state, validated count, or selected hunt changes.
 * - Checks that a hunt is started and permissions are granted.
 * - Reads the current location from [FusedLocationProviderClient].
 * - Determines the next point using [nextPointFor].
 * - Calls [MapViewModel.routeFromCurrentToNext] to calculate the route.
 * - Adjusts the camera to show both the current location and the next point within a [LatLngBounds]
 *   with a configured padding.
 *
 * @param hasLocationPermission [Boolean] whether location permission is granted.
 * @param uiState [MapUIState] current map UI state, including hunt status and validated points.
 * @param context [Context] used to verify permission status.
 * @param fused [FusedLocationProviderClient] used to obtain the current location.
 * @param cameraPositionState [CameraPositionState] map camera state to be animated.
 * @param scope [CoroutineScope] used to launch the camera animation.
 * @param viewModel [MapViewModel] which performs routing logic between current and next point.
 */
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
