package com.swentseekr.seekr.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.theme.Green

@Composable
fun HuntPopup(
    hunt: Hunt,
    onViewClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth()
                .padding(MapScreenDefaults.CardPadding)
                .testTag(MapScreenTestTags.POPUP_CARD),
        shape = RoundedCornerShape(MapScreenDefaults.CardCornerRadius),
        elevation = CardDefaults.cardElevation(MapScreenDefaults.CardElevation)
    ) {
        Column(Modifier.padding(MapScreenDefaults.CardPadding)) {
            Text(
                hunt.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.testTag(MapScreenTestTags.POPUP_TITLE)
            )
            Text(
                hunt.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = MapScreenDefaults.MaxLines,
                modifier = Modifier.testTag(MapScreenTestTags.POPUP_DESC)
            )
            Spacer(Modifier.height(MapScreenDefaults.PopupSpacing))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = Green),
                    modifier = Modifier.testTag(MapScreenTestTags.BUTTON_CANCEL)
                ) {
                    Text(MapScreenStrings.Cancel)
                }
                Button(
                    onClick = onViewClick,
                    colors =
                        ButtonDefaults.textButtonColors(
                            containerColor = Green,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                    modifier = Modifier.testTag(MapScreenTestTags.BUTTON_VIEW)
                ) {
                    Text(MapScreenStrings.ViewHunt)
                }
            }
        }
    }
}
