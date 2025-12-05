package com.swentseekr.seekr.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.ui.theme.GrayDislike
import com.swentseekr.seekr.ui.theme.Green
import com.swentseekr.seekr.ui.theme.White
import kotlinx.coroutines.launch

/**
 * Displays all overlay controls when a hunt is in "focused" mode.
 *
 * Includes:
 * - A top-left back/stop button
 * - A bottom card showing progress, next checkpoint, and actions (start, validate, finish)
 *
 * This composable is scoped to a [BoxScope] for easy positioning on top of the map.
 *
 * @param uiState current UI state of the map including progress and distance.
 * @param selectedHunt the hunt currently being viewed.
 * @param onStartHunt callback when user chooses to begin the hunt.
 * @param onValidateCurrentLocation callback for validating proximity to the next checkpoint.
 * @param onFinishHunt callback that receives an optional persistence lambda used to store hunt
 *   completion.
 * @param onBackToAllHunts invoked when the user exits the focused hunt view.
 * @param onShowStopHuntDialog invoked to show the dialog that confirms stopping the hunt.
 */
@Composable
fun BoxScope.FocusedHuntControls(
    uiState: MapUIState,
    selectedHunt: Hunt,
    onStartHunt: () -> Unit,
    onValidateCurrentLocation: () -> Unit,
    onFinishHunt: ((suspend (Hunt) -> Unit)?) -> Unit,
    onBackToAllHunts: () -> Unit,
    onShowStopHuntDialog: () -> Unit,
) {
  val isHuntStarted = uiState.isHuntStarted

  Button(
      onClick = { if (isHuntStarted) onShowStopHuntDialog() else onBackToAllHunts() },
      modifier =
          Modifier.align(Alignment.TopStart)
              .padding(MapScreenDefaults.BackButtonPadding)
              .testTag(MapScreenTestTags.BUTTON_BACK),
      colors = ButtonDefaults.textButtonColors(containerColor = Green, contentColor = White)) {
        Text(
            text =
                if (isHuntStarted) MapScreenStrings.StopHunt else MapScreenStrings.BackToAllHunts,
            style = MaterialTheme.typography.bodyMedium)
      }

  FocusedHuntBottomCard(
      uiState = uiState,
      selectedHunt = selectedHunt,
      onStartHunt = onStartHunt,
      onValidateCurrentLocation = onValidateCurrentLocation,
      onFinishHunt = onFinishHunt)
}

/**
 * Bottom overlay card shown during a focused hunt.
 *
 * Displays:
 * - Progress section
 * - Next checkpoint information
 * - Error messages
 * - Hunt action buttons (start, validate, finish)
 *
 * @param uiState the current UI state for the map.
 * @param selectedHunt the hunt being performed.
 * @param onStartHunt callback when the user begins the hunt.
 * @param onValidateCurrentLocation callback for validating proximity to next point.
 * @param onFinishHunt callback to finish the hunt with optional persistence logic.
 */
@Composable
private fun BoxScope.FocusedHuntBottomCard(
    uiState: MapUIState,
    selectedHunt: Hunt,
    onStartHunt: () -> Unit,
    onValidateCurrentLocation: () -> Unit,
    onFinishHunt: ((suspend (Hunt) -> Unit)?) -> Unit,
) {
  val totalPoints = selectedHunt.middlePoints.size + MapScreenDefaults.MinScore
  val validated = uiState.validatedCount
  val distanceToNext = uiState.currentDistanceToNextMeters
  val currentCheckpointInfo = calculateCurrentCheckpointInfo(selectedHunt, validated)
  var isCheckpointImageFullscreen by remember { mutableStateOf(false) }

  Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                topStart = MapScreenDefaults.CardCornerRadius,
                topEnd = MapScreenDefaults.CardCornerRadius),
        elevation = CardDefaults.cardElevation(MapScreenDefaults.CardElevation)) {
          Column(
              modifier = Modifier.fillMaxWidth().padding(MapScreenDefaults.OverlayInnerPadding),
              horizontalAlignment = Alignment.Start) {
                ProgressSection(validated = validated, totalPoints = totalPoints)

                currentCheckpointInfo?.let { (name, description, imageUrl) ->
                  NextCheckpointSection(
                      checkpointName = name,
                      checkpointDescription = description,
                      distanceToNext = distanceToNext,
                      imageUrl = imageUrl,
                      onImageClick = {
                        if (!imageUrl.isNullOrBlank()) {
                          isCheckpointImageFullscreen = true
                        }
                      })
                }

                ErrorMessage(uiState.errorMsg)

                Spacer(Modifier.height(MapScreenDefaults.BackButtonPadding))

                HuntPrimaryAction(
                    isHuntStarted = uiState.isHuntStarted,
                    validated = validated,
                    totalPoints = totalPoints,
                    onStartHunt = onStartHunt,
                    onValidateCurrentLocation = onValidateCurrentLocation,
                    onFinishHunt = onFinishHunt)
              }
        }

    if (isCheckpointImageFullscreen) {
      val (_, description, imageUrl) = currentCheckpointInfo ?: Triple("", "", null)

      if (!imageUrl.isNullOrBlank()) {
        FullscreenCheckpointImage(
            imageUrl = imageUrl,
            contentDescription = description,
            onClose = { isCheckpointImageFullscreen = false })
      } else {
        isCheckpointImageFullscreen = false
      }
    }
  }
}

@Composable
internal fun FullscreenCheckpointImage(
    imageUrl: String,
    contentDescription: String,
    onClose: () -> Unit
) {
  Dialog(onDismissRequest = onClose) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f))) {
      AsyncImage(
          model = imageUrl,
          contentDescription = contentDescription,
          modifier = Modifier.align(Alignment.Center).fillMaxWidth().fillMaxHeight(0.9f),
          contentScale = ContentScale.Fit,
          error = painterResource(R.drawable.empty_image),
      )

      IconButton(
          onClick = onClose,
          modifier =
              Modifier.align(Alignment.TopEnd)
                  .padding(16.dp)
                  .size(36.dp)
                  .background(color = Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                  .testTag(MapScreenTestTags.CLOSE_CHECKPOINT_IMAGE)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = MapScreenStrings.Cancel,
                tint = Color.White)
          }
    }
  }
}

/**
 * Determines the next checkpoint based on validated progress.
 *
 * @param selectedHunt the hunt in progress.
 * @param validated number of checkpoints already validated.
 * @return Pair of (checkpointName, checkpointDescription) or null if index is out of range.
 */
private fun calculateCurrentCheckpointInfo(
    selectedHunt: Hunt,
    validated: Int
): Triple<String, String, String?>? {
  val ordered = buildList {
    add(selectedHunt.start)
    selectedHunt.middlePoints.forEach { add(it) }
    add(selectedHunt.end)
  }
  val checkpoint = ordered.getOrNull(validated) ?: return null
  val imageUrl =
      checkpoint.imageIndex?.let { index -> selectedHunt.otherImagesUrls.getOrNull(index) }

  return Triple(checkpoint.name, checkpoint.description, imageUrl)
}

/**
 * Builds a formatted distance string showing distance to the next checkpoint and the validation
 * radius.
 *
 * @param distanceToNext distance in meters to the next checkpoint.
 * @return a formatted subtitle or an empty string if distance is null.
 */
private fun buildDistanceSuffix(distanceToNext: Int?): String =
    distanceToNext?.let {
      "${MapScreenStrings.IN}$it${MapScreenStrings.DistanceMetersSuffix}${MapScreenStrings.SLASH}" +
          "${MapConfig.ValidationRadiusMeters}${MapScreenStrings.DistanceMetersSuffix}"
    } ?: ""

/**
 * Combined progress header + segmented bar for hunt progression.
 *
 * @param validated checkpoints completed.
 * @param totalPoints total number of checkpoints in the hunt.
 */
@Composable
private fun ProgressSection(validated: Int, totalPoints: Int) {
  ProgressHeader(validated = validated, totalPoints = totalPoints)

  Spacer(Modifier.height(MapScreenDefaults.PopupSpacing))

  SegmentedProgressBar(validated = validated, totalPoints = totalPoints)
}

/**
 * Displays a header row showing "Progress" and a fraction like "2 / 5".
 *
 * @param validated completed checkpoints.
 * @param totalPoints total checkpoints.
 */
@Composable
private fun ProgressHeader(validated: Int, totalPoints: Int) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Text(text = MapScreenStrings.Progress, style = MaterialTheme.typography.bodyMedium)
        Text(
            modifier = Modifier.testTag(MapScreenTestTags.PROGRESS),
            text = "$validated${MapScreenStrings.SLASH}$totalPoints",
            style = MaterialTheme.typography.bodyMedium)
      }
}

/**
 * Displays a segmented progress bar where each segment represents a checkpoint.
 *
 * @param validated number of completed checkpoints.
 * @param totalPoints total checkpoints.
 */
@Composable
private fun SegmentedProgressBar(validated: Int, totalPoints: Int) {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    repeat(totalPoints) { index ->
      val isCompleted = index < validated

      Box(
          modifier =
              Modifier.weight(MapScreenDefaults.ONE_FLOAT)
                  .height(MapScreenDefaults.ProgressBarHeight)
                  .clip(RoundedCornerShape(MapScreenDefaults.ProgressSegmentCornerRadius))
                  .background(
                      if (isCompleted) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSecondary))

      if (index != totalPoints - 1) {
        Spacer(Modifier.width(MapScreenDefaults.ProgressTickSpacing))
      }
    }
  }
}

/**
 * Displays the name and optional description of the next checkpoint.
 *
 * @param checkpointName name of upcoming checkpoint.
 * @param checkpointDescription optional description text.
 * @param distanceToNext distance to next checkpoint in meters.
 */
@Composable
fun NextCheckpointSection(
    checkpointName: String,
    checkpointDescription: String,
    distanceToNext: Int?,
    imageUrl: String?,
    onImageClick: () -> Unit = {}
) {
  Spacer(Modifier.height(MapScreenDefaults.PopupSpacing))

  val distanceSuffix = buildDistanceSuffix(distanceToNext)

  if (!imageUrl.isNullOrBlank()) {
    AsyncImage(
        model = imageUrl,
        contentDescription = checkpointName + MapScreenStrings.HuntImageDescriptionSuffix,
        modifier =
            Modifier.fillMaxWidth()
                .height(MapScreenDefaults.PopupImageSize)
                .clip(RoundedCornerShape(MapScreenDefaults.PopupImageCornerRadius))
                .clickable(onClick = onImageClick)
                .testTag(MapScreenTestTags.NEXT_CHECKPOINT_IMAGE),
        contentScale = ContentScale.Crop,
        error = painterResource(R.drawable.empty_image),
    )

    Spacer(Modifier.height(MapScreenDefaults.PopupSpacing))
  }

  Text(
      text = "${MapScreenStrings.NextStopPrefix}$checkpointName$distanceSuffix",
      style = MaterialTheme.typography.titleSmall)

  if (checkpointDescription.isNotBlank()) {
    Text(text = checkpointDescription, style = MaterialTheme.typography.bodySmall)
  }
}

/**
 * Displays an error message if the hunt produces one (e.g., validation errors).
 *
 * @param errorMsg nullable message string.
 */
@Composable
private fun ErrorMessage(errorMsg: String?) {
  val message = errorMsg?.takeIf { it.isNotBlank() } ?: return

  Spacer(Modifier.height(MapScreenDefaults.PopupSpacing))

  Text(
      text = message,
      color = MaterialTheme.colorScheme.error,
      style = MaterialTheme.typography.bodySmall)
}

/**
 * Chooses and displays the correct primary action button(s) based on hunt progress:
 * - If not started → "Start Hunt"
 * - If started → "Validate" and "Finish" (when all checkpoints are validated)
 *
 * @param isHuntStarted whether hunt is active.
 * @param validated checkpoints completed.
 * @param totalPoints total checkpoints.
 * @param onStartHunt begins the hunt.
 * @param onValidateCurrentLocation validates next checkpoint.
 * @param onFinishHunt finalizes hunt on completion.
 */
@Composable
private fun HuntPrimaryAction(
    isHuntStarted: Boolean,
    validated: Int,
    totalPoints: Int,
    onStartHunt: () -> Unit,
    onValidateCurrentLocation: () -> Unit,
    onFinishHunt: ((suspend (Hunt) -> Unit)?) -> Unit,
) {
  if (!isHuntStarted) {
    StartHuntButton(onStartHunt = onStartHunt)
  } else {
    HuntActionsRow(
        canFinish = validated >= totalPoints,
        onValidateCurrentLocation = onValidateCurrentLocation,
        onFinishHunt = onFinishHunt)
  }
}

/**
 * Button displayed before the hunt is started.
 *
 * @param onStartHunt callback to start the hunt.
 */
@Composable
private fun StartHuntButton(onStartHunt: () -> Unit) {
  Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Button(
        modifier = Modifier.testTag(MapScreenTestTags.START),
        onClick = onStartHunt,
        colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = White)) {
          Text(MapScreenStrings.StartHunt)
        }
  }
}

/**
 * Row containing "Validate" and "Finish" actions shown during active hunts.
 * - Validate: checks current location proximity.
 * - Finish: completes the hunt once all checkpoints validated.
 *
 * @param canFinish whether all checkpoints have been completed.
 * @param onValidateCurrentLocation callback to validate the next point.
 * @param onFinishHunt callback that receives a suspend lambda for persisting the finished hunt.
 */
@Composable
private fun HuntActionsRow(
    canFinish: Boolean,
    onValidateCurrentLocation: () -> Unit,
    onFinishHunt: ((suspend (Hunt) -> Unit)?) -> Unit,
) {
  val scope = rememberCoroutineScope()

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically) {
        TextButton(
            modifier = Modifier.testTag(MapScreenTestTags.VALIDATE),
            onClick = onValidateCurrentLocation,
            colors = ButtonDefaults.textButtonColors(contentColor = Green)) {
              Text(MapScreenStrings.Validate)
            }

        Button(
            modifier = Modifier.testTag(MapScreenTestTags.FINISH),
            onClick = {
              onFinishHunt { finished ->
                scope.launch {
                  try {
                    val userId =
                        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                      ProfileRepositoryProvider.repository.addDoneHunt(userId, finished)
                    }
                  } catch (e: Exception) {
                    android.util.Log.e(MapScreenTestTags.MAP_SCREEN, MapScreenStrings.Fail, e)
                  }
                }
              }
            },
            enabled = canFinish,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = if (canFinish) Green else GrayDislike, contentColor = White)) {
              Text(MapScreenStrings.FinishHunt)
            }
      }
}

/**
 * Confirmation dialog for stopping an active hunt prematurely.
 *
 * @param onConfirm invoked when user confirms stopping the hunt.
 * @param onDismiss invoked when user cancels the dialog.
 */
@Composable
fun StopHuntDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
      modifier = Modifier.testTag(MapScreenTestTags.STOP_POPUP),
      onDismissRequest = onDismiss,
      title = { Text(MapScreenStrings.StopHuntTitle) },
      text = { Text(MapScreenStrings.StopHuntMessage) },
      confirmButton = {
        TextButton(modifier = Modifier.testTag(MapScreenTestTags.CONFIRM), onClick = onConfirm) {
          Text(MapScreenStrings.ConfirmStopHunt)
        }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text(MapScreenStrings.Cancel) } })
}
