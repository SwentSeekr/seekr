package com.swentseekr.seekr.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.DifficultyColor
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.StatusColor

@Composable
fun HuntPopup(hunt: Hunt, onViewClick: () -> Unit, onDismiss: () -> Unit) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(MapScreenDefaults.CardPadding)
              .testTag(MapScreenTestTags.POPUP_CARD),
      shape = RoundedCornerShape(MapScreenDefaults.CardCornerRadius),
      elevation = CardDefaults.cardElevation(MapScreenDefaults.CardElevation)) {
        Row(
            modifier = Modifier.padding(MapScreenDefaults.CardPadding),
            verticalAlignment = Alignment.CenterVertically) {
              AsyncImage(
                  model = hunt.mainImageUrl,
                  contentDescription = hunt.title + MapScreenStrings.HuntImageDescriptionSuffix,
                  modifier =
                      Modifier.size(MapScreenDefaults.PopupImageSize)
                          .clip(RoundedCornerShape(MapScreenDefaults.PopupImageCornerRadius))
                          .testTag(MapScreenTestTags.POPUP_IMAGE),
                  contentScale = ContentScale.Crop,
                  error = painterResource(R.drawable.empty_image),
              )

              Spacer(Modifier.width(MapScreenDefaults.PopupSpacing))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                    hunt.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag(MapScreenTestTags.POPUP_TITLE))

                Text(
                    hunt.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = MapScreenDefaults.MaxLines,
                    modifier = Modifier.testTag(MapScreenTestTags.POPUP_DESC))

                Spacer(Modifier.height(MapScreenDefaults.PopupSpacing))

                Row(
                    modifier = Modifier.fillMaxWidth().testTag(MapScreenTestTags.POPUP_META_ROW),
                    horizontalArrangement = Arrangement.spacedBy(MapScreenDefaults.PopupSpacing)) {
                      StatusChip(hunt.status)
                      DifficultyChip(hunt.difficulty)
                    }

                Spacer(Modifier.height(MapScreenDefaults.PopupSpacing))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                  TextButton(
                      onClick = onDismiss,
                      modifier = Modifier.testTag(MapScreenTestTags.BUTTON_CANCEL)) {
                        Text(MapScreenStrings.Cancel)
                      }

                  Button(
                      onClick = onViewClick,
                      modifier = Modifier.testTag(MapScreenTestTags.BUTTON_VIEW)) {
                        Text(MapScreenStrings.ViewHunt)
                      }
                }
              }
            }
      }
}

@Composable
private fun StatusChip(status: HuntStatus) {
  val baseColor = Color(StatusColor(status))
  val readableText = baseColor.darken(MapScreenDefaults.ChipContentDarkenFactor)

  Surface(
      color = baseColor.copy(alpha = MapScreenDefaults.ChipBackgroundAlpha),
      contentColor = readableText,
      shape = RoundedCornerShape(MapScreenDefaults.ChipCornerRadius)) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            modifier =
                Modifier.padding(
                    horizontal = MapScreenDefaults.ChipHorizontalPadding,
                    vertical = MapScreenDefaults.ChipVerticalPadding))
      }
}

@Composable
private fun DifficultyChip(difficulty: Difficulty) {
  val baseColor = DifficultyColor(difficulty)
  val readableText = baseColor.darken(MapScreenDefaults.ChipContentDarkenFactor)

  Surface(
      color = baseColor.copy(alpha = MapScreenDefaults.ChipBackgroundAlpha),
      contentColor = readableText,
      shape = RoundedCornerShape(MapScreenDefaults.ChipCornerRadius)) {
        Text(
            text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            modifier =
                Modifier.padding(
                    horizontal = MapScreenDefaults.ChipHorizontalPadding,
                    vertical = MapScreenDefaults.ChipVerticalPadding))
      }
}

private fun Color.darken(factor: Float): Color =
    Color(
        red = (this.red * factor).coerceIn(0f, 1f),
        green = (this.green * factor).coerceIn(0f, 1f),
        blue = (this.blue * factor).coerceIn(0f, 1f),
        alpha = 1f)
