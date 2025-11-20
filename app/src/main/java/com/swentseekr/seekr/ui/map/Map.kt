package com.swentseekr.seekr.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.ui.theme.Blue
import com.swentseekr.seekr.ui.theme.GrayDislike
import com.swentseekr.seekr.ui.theme.Green
import com.swentseekr.seekr.ui.theme.White
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

  var hasLocationPermission by remember { mutableStateOf(false) }

  var showStopHuntDialog by remember { mutableStateOf(false) }

  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            hasLocationPermission =
                grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
          }

  val selectedHunt = uiState.selectedHunt

  if (!testMode) {
    LaunchedEffect(Unit) {
      permissionLauncher.launch(
          arrayOf(
              Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
  }

  LaunchedEffect(hasLocationPermission, mapLoaded) {
    if (!mapLoaded) return@LaunchedEffect
    val fineGranted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    val coarseGranted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    if (!(fineGranted || coarseGranted)) {
      return@LaunchedEffect
    }

    fused.lastLocation.addOnSuccessListener { location ->
      location?.let {
        val here = LatLng(it.latitude, it.longitude)
        scope.launch {
          cameraPositionState.animate(
              CameraUpdateFactory.newLatLngZoom(here, MapScreenDefaults.UserLocationZoom))
        }
      }
    }
  }

  val mapUiSettings =
      MapUiSettings(
          myLocationButtonEnabled = true,
          scrollGesturesEnabled = hasLocationPermission,
          zoomGesturesEnabled = hasLocationPermission,
          tiltGesturesEnabled = hasLocationPermission,
          rotationGesturesEnabled = hasLocationPermission)
  val mapProperties = MapProperties(isMyLocationEnabled = hasLocationPermission)

  Box(Modifier.fillMaxSize().testTag(MapScreenTestTags.MAP_SCREEN)) {
    GoogleMap(
        modifier = Modifier.matchParentSize().testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
        cameraPositionState = cameraPositionState,
        onMapLoaded = { mapLoaded = true },
        properties = mapProperties,
        uiSettings = mapUiSettings) {
          LaunchedEffect(mapLoaded, selectedHunt, uiState.isFocused) {
            if (!mapLoaded) return@LaunchedEffect
            val hunt = selectedHunt ?: return@LaunchedEffect

            if (!uiState.isFocused) {
              if (previousCameraPosition == null) {
                previousCameraPosition = cameraPositionState.position
              }
              val target = LatLng(hunt.start.latitude, hunt.start.longitude)
              cameraPositionState.animate(
                  CameraUpdateFactory.newLatLngZoom(target, MapScreenDefaults.FocusedZoom))
            } else {
              val points = buildList {
                add(LatLng(hunt.start.latitude, hunt.start.longitude))
                hunt.middlePoints.forEach { add(LatLng(it.latitude, it.longitude)) }
                add(LatLng(hunt.end.latitude, hunt.end.longitude))
              }

              if (points.size == MapScreenDefaults.UnitPointSize) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        points.first(), MapScreenDefaults.FocusedZoom))
              } else {
                val bounds = LatLngBounds.Builder().apply { points.forEach { include(it) } }.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, MapScreenDefaults.BoundsPadding))
              }
            }
          }

          if (uiState.isFocused && selectedHunt != null) {
            Marker(
                state =
                    MarkerState(LatLng(selectedHunt.start.latitude, selectedHunt.start.longitude)),
                title = "${MapScreenStrings.StartPrefix}${selectedHunt.start.name}",
                icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.ic_start_marker))
            selectedHunt.middlePoints.forEachIndexed { idx, point ->
              Marker(
                  state = MarkerState(LatLng(point.latitude, point.longitude)), title = point.name)
            }
            Marker(
                state = MarkerState(LatLng(selectedHunt.end.latitude, selectedHunt.end.longitude)),
                title = "${MapScreenStrings.EndPrefix}${selectedHunt.end.name}",
                icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.ic_end_marker))
            if (uiState.route.isNotEmpty()) {
              Polyline(
                  points = uiState.route, width = MapScreenDefaults.RouteStrokeWidth, color = Blue)
            }
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

    if (!hasLocationPermission) {
      PermissionRequestPopup(
          onRequestPermission = {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION))
          })
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

      val isHuntStarted = uiState.isHuntStarted

      Button(
          onClick = {
            if (isHuntStarted) {
              showStopHuntDialog = true
            } else {
              viewModel.onBackToAllHunts()
            }
          },
          modifier =
              Modifier.align(Alignment.TopStart)
                  .padding(MapScreenDefaults.BackButtonPadding)
                  .testTag(MapScreenTestTags.BUTTON_BACK),
          colors = ButtonDefaults.textButtonColors(containerColor = Green, contentColor = White)) {
            Text(
                text =
                    if (isHuntStarted) MapScreenStrings.StopHunt
                    else MapScreenStrings.BackToAllHunts,
                style = MaterialTheme.typography.bodyMedium)
          }

      if (showStopHuntDialog) {
        AlertDialog(
            modifier = Modifier.testTag(MapScreenTestTags.STOP_POPUP),
            onDismissRequest = { showStopHuntDialog = false },
            title = { Text(MapScreenStrings.StopHuntTitle) },
            text = { Text(MapScreenStrings.StopHuntMessage) },
            confirmButton = {
              TextButton(
                  modifier = Modifier.testTag(MapScreenTestTags.CONFIRM),
                  onClick = {
                    showStopHuntDialog = false
                    viewModel.onBackToAllHunts()
                  }) {
                    Text(MapScreenStrings.ConfirmStopHunt)
                  }
            },
            dismissButton = {
              TextButton(onClick = { showStopHuntDialog = false }) { Text(MapScreenStrings.Cancel) }
            })
      }

      Card(
          modifier =
              Modifier.align(Alignment.BottomCenter)
                  .fillMaxWidth()
                  .padding(
                      horizontal = MapScreenDefaults.OverlayDoublePadding,
                      vertical = MapScreenDefaults.OverlayInnerPadding),
          shape =
              RoundedCornerShape(
                  topStart = MapScreenDefaults.CardPadding, topEnd = MapScreenDefaults.CardPadding),
          elevation = CardDefaults.cardElevation(MapScreenDefaults.CardElevation)) {
            Column(
                modifier = Modifier.padding(MapScreenDefaults.CardPadding).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                  val hunt = selectedHunt
                  val totalPoints = (hunt?.middlePoints?.size ?: 0) + 2
                  val validated = uiState.validatedCount

                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = MapScreenStrings.Progress,
                            style = MaterialTheme.typography.bodyMedium)
                        Text(
                            modifier = Modifier.testTag(MapScreenTestTags.PROGRESS),
                            text = "$validated / $totalPoints",
                            style = MaterialTheme.typography.bodyMedium)
                      }

                  Spacer(Modifier.height(MapScreenDefaults.BackButtonPadding))

                  if (!uiState.isHuntStarted) {
                    Button(
                        modifier = Modifier.testTag(MapScreenTestTags.START),
                        onClick = { viewModel.startHunt() },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Green, contentColor = White)) {
                          Text(MapScreenStrings.StartHunt)
                        }
                  } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically) {
                          TextButton(
                              modifier = Modifier.testTag(MapScreenTestTags.VALIDATE),
                              onClick = {
                                val fineGranted =
                                    ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                                        PackageManager.PERMISSION_GRANTED
                                val coarseGranted =
                                    ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                        PackageManager.PERMISSION_GRANTED

                                if (!(fineGranted || coarseGranted)) return@TextButton

                                fused.lastLocation.addOnSuccessListener { loc ->
                                  loc?.let {
                                    viewModel.validateCurrentPoint(
                                        LatLng(it.latitude, it.longitude))
                                  }
                                }
                              },
                              colors = ButtonDefaults.textButtonColors(contentColor = Green)) {
                                Text(MapScreenStrings.Validate)
                              }

                          val canFinish = validated >= totalPoints
                          Button(
                              modifier = Modifier.testTag(MapScreenTestTags.FINISH),
                              onClick = {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                  viewModel.finishHunt(
                                      onPersist = { finished ->
                                        scope.launch {
                                          try {
                                            ProfileRepositoryProvider.repository.addDoneHunt(
                                                userId, finished)
                                          } catch (e: Exception) {
                                            Log.e("MapScreen", "Failed to add done hunt", e)
                                          }
                                        }
                                      })
                                }
                              },
                              enabled = canFinish,
                              colors =
                                  ButtonDefaults.buttonColors(
                                      containerColor = if (canFinish) Green else GrayDislike,
                                      contentColor = White)) {
                                Text(MapScreenStrings.FinishHunt)
                              }
                        }
                  }
                }
          }
    }
  }
}

/**
 * Displays a popup requesting location permission from the user.
 *
 * @param onRequestPermission callback invoked when the user opts to grant location permission.
 */
@Composable
fun PermissionRequestPopup(onRequestPermission: () -> Unit) {
  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(MapScreenDefaults.OverlayScrimColor)
              .padding(MapScreenDefaults.OverlayPadding)
              .testTag(MapScreenTestTags.PERMISSION_POPUP),
      contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(MapScreenDefaults.CardPadding),
            shape = RoundedCornerShape(MapScreenDefaults.CardCornerRadius),
            elevation = CardDefaults.cardElevation(MapScreenDefaults.CardElevation)) {
              Column(
                  modifier = Modifier.padding(MapScreenDefaults.OverlayInnerPadding).fillMaxWidth(),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = MapScreenStrings.PermissionExplanation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                        modifier =
                            Modifier.padding(bottom = MapScreenDefaults.CardPadding)
                                .testTag(MapScreenTestTags.EXPLAIN))
                    TextButton(
                        onClick = { onRequestPermission() },
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(top = MapScreenDefaults.PopupSpacing)
                                .testTag(MapScreenTestTags.GRANT_LOCATION_PERMISSION)) {
                          Text(MapScreenStrings.GrantPermission, color = Color.White)
                        }
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
      modifier =
          Modifier.fillMaxWidth()
              .padding(MapScreenDefaults.CardPadding)
              .testTag(MapScreenTestTags.POPUP_CARD),
      shape = RoundedCornerShape(MapScreenDefaults.CardCornerRadius),
      elevation = CardDefaults.cardElevation(MapScreenDefaults.CardElevation)) {
        Column(Modifier.padding(MapScreenDefaults.CardPadding)) {
          Text(
              hunt.title,
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.testTag(MapScreenTestTags.POPUP_TITLE))
          Text(
              hunt.description,
              style = MaterialTheme.typography.bodyMedium,
              maxLines = MapScreenDefaults.MaxLines,
              modifier = Modifier.testTag(MapScreenTestTags.POPUP_DESC))
          Spacer(Modifier.height(MapScreenDefaults.PopupSpacing))
          Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Green),
                modifier = Modifier.testTag(MapScreenTestTags.BUTTON_CANCEL)) {
                  Text(MapScreenStrings.Cancel)
                }
            Button(
                onClick = onViewClick,
                colors =
                    ButtonDefaults.textButtonColors(
                        containerColor = Green, contentColor = Color.White),
                modifier = Modifier.testTag(MapScreenTestTags.BUTTON_VIEW)) {
                  Text(MapScreenStrings.ViewHunt)
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
