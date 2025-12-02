package com.swentseekr.seekr.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.ui.theme.GrayDislike
import com.swentseekr.seekr.ui.theme.Green
import com.swentseekr.seekr.ui.theme.White
import kotlinx.coroutines.launch

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

                currentCheckpointInfo?.let { (name, description) ->
                  NextCheckpointSection(
                      checkpointName = name,
                      checkpointDescription = description,
                      distanceToNext = distanceToNext)
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
  }
}

private fun calculateCurrentCheckpointInfo(
    selectedHunt: Hunt,
    validated: Int
): Pair<String, String>? {
  val ordered = buildList {
    add(selectedHunt.start)
    selectedHunt.middlePoints.forEach { add(it) }
    add(selectedHunt.end)
  }

  return ordered.getOrNull(validated)?.let { checkpoint ->
    checkpoint.name to checkpoint.description
  }
}

private fun buildDistanceSuffix(distanceToNext: Int?): String =
    distanceToNext?.let {
      " in $it${MapScreenStrings.DistanceMetersSuffix} / " +
          "${MapConfig.ValidationRadiusMeters}${MapScreenStrings.DistanceMetersSuffix}"
    } ?: ""

@Composable
private fun ProgressSection(validated: Int, totalPoints: Int) {
  ProgressHeader(validated = validated, totalPoints = totalPoints)

  Spacer(Modifier.height(MapScreenDefaults.PopupSpacing))

  SegmentedProgressBar(validated = validated, totalPoints = totalPoints)
}

@Composable
private fun ProgressHeader(validated: Int, totalPoints: Int) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Text(text = MapScreenStrings.Progress, style = MaterialTheme.typography.bodyMedium)
        Text(
            modifier = Modifier.testTag(MapScreenTestTags.PROGRESS),
            text = "$validated / $totalPoints",
            style = MaterialTheme.typography.bodyMedium)
      }
}

@Composable
private fun SegmentedProgressBar(validated: Int, totalPoints: Int) {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    repeat(totalPoints) { index ->
      val isCompleted = index < validated

      Box(
          modifier =
              Modifier.weight(1f)
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

@Composable
private fun NextCheckpointSection(
    checkpointName: String,
    checkpointDescription: String,
    distanceToNext: Int?
) {
  Spacer(Modifier.height(MapScreenDefaults.PopupSpacing))

  val distanceSuffix = buildDistanceSuffix(distanceToNext)

  Text(
      text = "${MapScreenStrings.NextStopPrefix}$checkpointName$distanceSuffix",
      style = MaterialTheme.typography.titleSmall)

  if (checkpointDescription.isNotBlank()) {
    Text(text = checkpointDescription, style = MaterialTheme.typography.bodySmall)
  }
}

@Composable
private fun ErrorMessage(errorMsg: String?) {
  val message = errorMsg?.takeIf { it.isNotBlank() } ?: return

  Spacer(Modifier.height(MapScreenDefaults.PopupSpacing))

  Text(
      text = message,
      color = MaterialTheme.colorScheme.error,
      style = MaterialTheme.typography.bodySmall)
}

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
        onFinishHunt = onFinishHunt,
    )
  }
}

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
