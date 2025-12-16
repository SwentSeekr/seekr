package com.swentseekr.seekr.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.difficultyColor
import com.swentseekr.seekr.model.hunt.statusColor
import com.swentseekr.seekr.ui.map.MapScreenDefaults.ONE_FLOAT
import com.swentseekr.seekr.ui.map.MapScreenDefaults.ZERO_FLOAT

/**
 * Displays a popup card presenting a summary of a hunt.
 *
 * This popup appears when a hunt marker is selected in the map overview. It includes:
 * - Hunt image
 * - Title and short description
 * - Status and difficulty chips
 * - "Cancel" and "View Hunt" actions
 *
 * @param hunt the hunt being displayed in the popup.
 * @param onViewClick invoked when the user selects the “View Hunt” button.
 * @param onDismiss invoked when the user selects the “Cancel” button.
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
        Row(
            modifier = Modifier.padding(MapScreenDefaults.CardPadding),
            verticalAlignment = Alignment.CenterVertically) {
              AsyncImage(
                  model = hunt.mainImageUrl,
                  contentDescription = hunt.title + MapScreenStrings.HUNT_IMAGE_DESCRIPTION_SUFFIX,
                  modifier =
                      Modifier.size(MapScreenDefaults.PopupImageSize)
                          .clip(RoundedCornerShape(MapScreenDefaults.PopupImageCornerRadius))
                          .testTag(MapScreenTestTags.POPUP_IMAGE),
                  contentScale = ContentScale.Crop,
                  error = painterResource(R.drawable.empty_image),
              )

              Spacer(Modifier.width(MapScreenDefaults.PopupSpacing))

              Column(modifier = Modifier.weight(ONE_FLOAT)) {
                Text(
                    hunt.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag(MapScreenTestTags.POPUP_TITLE))

                Text(
                    hunt.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = MapScreenDefaults.MAX_LINES,
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
                        Text(MapScreenStrings.CANCEL)
                      }

                  Button(
                      onClick = onViewClick,
                      modifier = Modifier.testTag(MapScreenTestTags.BUTTON_VIEW)) {
                        Text(MapScreenStrings.VIEW_HUNT)
                      }
                }
              }
            }
      }
}

/**
 * Global chip used for both status and difficulty.
 *
 * @param label text to display inside the chip.
 * @param baseColor base color from which background and content colors are derived.
 */
@Composable
private fun HuntMetaChip(label: String, baseColor: Color) {
  val readableText = baseColor.darken(MapScreenDefaults.CHIP_CONTENT_DARKEN_FACTOR)

  Surface(
      color = baseColor.copy(alpha = MapScreenDefaults.CHIP_BACKGROUND_ALPHA),
      contentColor = readableText,
      shape = RoundedCornerShape(MapScreenDefaults.ChipCornerRadius)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier =
                Modifier.padding(
                    horizontal = MapScreenDefaults.ChipHorizontalPadding,
                    vertical = MapScreenDefaults.ChipVerticalPadding))
      }
}

/**
 * Displays a chip representing the current status of a hunt.
 *
 * The chip uses a background color derived from the hunt's status and adjusts text color for
 * readability by darkening the base color.
 *
 * @param status the current status of the hunt (e.g., AVAILABLE, IN_PROGRESS, COMPLETED).
 */
@Composable
private fun StatusChip(status: HuntStatus) {
  val baseColor = statusColor(status)
  val label = status.name.lowercase().replaceFirstChar { it.uppercase() }

  HuntMetaChip(label = label, baseColor = baseColor)
}

/**
 * Displays a chip representing a hunt’s difficulty.
 *
 * The chip color is derived from the difficulty level using [difficultyColor]. Text color is
 * adjusted by darkening the base color to maintain contrast.
 *
 * @param difficulty the difficulty level of the hunt.
 */
@Composable
private fun DifficultyChip(difficulty: Difficulty) {
  val baseColor = difficultyColor(difficulty)
  val label = difficulty.name.lowercase().replaceFirstChar { it.uppercase() }

  HuntMetaChip(label = label, baseColor = baseColor)
}

/**
 * Creates a darker version of the color by multiplying each RGB component by the provided factor.
 *
 * @param factor a value between 0f and 1f; lower values result in a darker color.
 * @return a new darkened [Color] instance.
 */
@Composable
private fun Color.darken(factor: Float): Color {
  val f = factor.coerceIn(ZERO_FLOAT, ONE_FLOAT)
  return lerp(this, MaterialTheme.colorScheme.onBackground, ONE_FLOAT - f)
}
