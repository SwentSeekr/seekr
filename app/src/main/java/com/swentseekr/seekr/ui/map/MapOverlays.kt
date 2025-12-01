package com.swentseekr.seekr.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.ui.theme.GrayDislike
import com.swentseekr.seekr.ui.theme.Green
import com.swentseekr.seekr.ui.theme.White
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.rememberCoroutineScope

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

    // Top-left button: Back / Stop
    Button(
        onClick = { if (isHuntStarted) onShowStopHuntDialog() else onBackToAllHunts() },
        modifier =
            Modifier.align(Alignment.TopStart)
                .padding(MapScreenDefaults.BackButtonPadding)
                .testTag(MapScreenTestTags.BUTTON_BACK),
        colors = ButtonDefaults.textButtonColors(containerColor = Green, contentColor = White)
    ) {
        Text(
            text =
                if (isHuntStarted) MapScreenStrings.StopHunt
                else MapScreenStrings.BackToAllHunts,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    // Bottom card
    FocusedHuntBottomCard(
        uiState = uiState,
        selectedHunt = selectedHunt,
        onStartHunt = onStartHunt,
        onValidateCurrentLocation = onValidateCurrentLocation,
        onFinishHunt = onFinishHunt
    )
}

@Composable
private fun BoxScope.FocusedHuntBottomCard(
    uiState: MapUIState,
    selectedHunt: Hunt,
    onStartHunt: () -> Unit,
    onValidateCurrentLocation: () -> Unit,
    onFinishHunt: ((suspend (Hunt) -> Unit)?) -> Unit,
) {
    val totalPoints =
        selectedHunt.middlePoints.size + MapScreenDefaults.MinScore
    val validated = uiState.validatedCount

    val currentCheckpointInfo: Pair<String, String>? =
        run {
            val ordered = buildList {
                add(selectedHunt.start)
                selectedHunt.middlePoints.forEach { add(it) }
                add(selectedHunt.end)
            }
            ordered.getOrNull(validated)?.let { checkpoint ->
                checkpoint.name to checkpoint.description
            }
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
                topStart = MapScreenDefaults.CardPadding,
                topEnd = MapScreenDefaults.CardPadding),
        elevation = CardDefaults.cardElevation(MapScreenDefaults.CardElevation)
    ) {
        Column(
            modifier = Modifier.padding(MapScreenDefaults.CardPadding).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = MapScreenStrings.Progress,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    modifier = Modifier.testTag(MapScreenTestTags.PROGRESS),
                    text = "$validated / $totalPoints",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            currentCheckpointInfo?.let { (name, description) ->
                Text(
                    text = "${MapScreenStrings.NextStopPrefix}$name",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(MapScreenDefaults.BackButtonPadding))

            if (!uiState.isHuntStarted) {
                Button(
                    modifier = Modifier.testTag(MapScreenTestTags.START),
                    onClick = onStartHunt,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Green,
                            contentColor = White
                        )
                ) {
                    Text(MapScreenStrings.StartHunt)
                }
            } else {
                HuntActionsRow(
                    canFinish = validated >= totalPoints,
                    onValidateCurrentLocation = onValidateCurrentLocation,
                    onFinishHunt = onFinishHunt,
                )
            }
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            modifier = Modifier.testTag(MapScreenTestTags.VALIDATE),
            onClick = onValidateCurrentLocation,
            colors = ButtonDefaults.textButtonColors(contentColor = Green)
        ) {
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
                            android.util.Log.e(
                                MapScreenTestTags.MAP_SCREEN,
                                MapScreenStrings.Fail,
                                e
                            )
                        }
                    }
                }
            },
            enabled = canFinish,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = if (canFinish) Green else GrayDislike,
                    contentColor = White
                )
        ) {
            Text(MapScreenStrings.FinishHunt)
        }
    }
}

@Composable
fun StopHuntDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.testTag(MapScreenTestTags.STOP_POPUP),
        onDismissRequest = onDismiss,
        title = { Text(MapScreenStrings.StopHuntTitle) },
        text = { Text(MapScreenStrings.StopHuntMessage) },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag(MapScreenTestTags.CONFIRM),
                onClick = onConfirm
            ) {
                Text(MapScreenStrings.ConfirmStopHunt)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(MapScreenStrings.Cancel)
            }
        }
    )
}
