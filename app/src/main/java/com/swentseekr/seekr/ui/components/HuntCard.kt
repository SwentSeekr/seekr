package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt

/**
 * Displays a modern, image-focused card preview of a Hunt.
 *
 * The card shows:
 * - the main hunt image (with gradient overlay),
 * - a difficulty badge,
 * - a like button (UI only),
 * - the hunt title and author,
 * - distance and duration stats.
 *
 * All layout values (sizes, paddings, colors) are centralized in `HuntCardUIConstants`.
 *
 * @param hunt The Hunt to display (title, authorId, distance, time, difficulty, mainImageUrl).
 * @param modifier Optional modifier for external styling or click handling.
 */
@Composable
fun HuntCard(
    hunt: Hunt,
    isLiked: Boolean = false,
    onLikeClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
  Card(
      modifier =
          modifier
              .padding(HuntCardUIConstants.CardPadding)
              .fillMaxWidth(HuntCardUIConstants.CardWidthFraction),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = HuntCardUIConstants.CardElevation),
      shape = RoundedCornerShape(HuntCardUIConstants.CornerRadius)) {
        Column(modifier = Modifier.fillMaxWidth()) {

          // IMAGE WITH GRADIENT
          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .height(HuntCardUIConstants.ImageHeight)
                      .clip(
                          RoundedCornerShape(
                              topStart = HuntCardUIConstants.CornerRadius,
                              topEnd = HuntCardUIConstants.CornerRadius))) {
                AsyncImage(
                    model = hunt.mainImageUrl.takeIf { it.isNotBlank() },
                    contentDescription = HuntCardScreenStrings.HuntPictureDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.empty_image),
                    error = painterResource(R.drawable.empty_image))

                // Gradient overlay
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            HuntCardUIConstants.Black30,
                                            HuntCardUIConstants.Black70))))

                // DIFFICULTY BADGE (top-left)
                ModernDifficultyBadge(
                    difficulty = hunt.difficulty,
                    modifier =
                        Modifier.align(Alignment.TopStart).padding(HuntCardUIConstants.Padding12))

                // LIKE BUTTON (top-right)
                IconButton(
                    onClick = { onLikeClick(hunt.uid) },
                    modifier =
                        Modifier.align(Alignment.TopEnd)
                            .padding(HuntCardUIConstants.Padding8)
                            .testTag(HuntCardScreenStrings.LikeButton)) {
                      Icon(
                          imageVector = Icons.Filled.Favorite,
                          contentDescription = HuntCardScreenStrings.LikeButton,
                          tint =
                              if (isLiked) HuntCardUIConstants.LikeRed
                              else HuntCardScreenDefaults.LightGray,
                          modifier = Modifier.size(HuntCardUIConstants.IconSize28))
                    }

                // TITLE + AUTHOR
                Column(
                    modifier =
                        Modifier.align(Alignment.BottomStart)
                            .padding(HuntCardUIConstants.Padding16)) {
                      Text(
                          text = hunt.title,
                          fontSize = HuntCardUIConstants.TitleFont24,
                          fontWeight = FontWeight.Bold,
                          color = HuntCardUIConstants.White,
                          lineHeight = 28.sp)
                      Spacer(modifier = Modifier.height(HuntCardUIConstants.Padding4))
                      Text(
                          text = "${HuntCardScreenStrings.By} ${hunt.authorId}",
                          fontSize = HuntCardUIConstants.AuthorFont14,
                          color = HuntCardUIConstants.White.copy(alpha = 0.9f),
                          fontWeight = FontWeight.Medium)
                    }
              }

          // STATS ROW
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(
                          horizontal = HuntCardUIConstants.Padding16,
                          vertical = HuntCardUIConstants.Padding16),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically) {
                ModernStatChip(
                    icon = Icons.Filled.LocationOn,
                    value = "${hunt.distance}",
                    unit = HuntCardScreenStrings.DistanceUnit,
                    modifier = Modifier.weight(1f))

                Spacer(modifier = Modifier.width(HuntCardUIConstants.Padding8))

                // We need to change the icon later
                ModernStatChip(
                    icon = Icons.Filled.Favorite,
                    value = "${hunt.time}",
                    unit = HuntCardScreenStrings.TimeUnit,
                    modifier = Modifier.weight(1f))
              }
        }
      }
}

/**
 * Displays a rounded difficulty badge (Easy, Intermediate, Difficult).
 *
 * The badge uses:
 * - a colored pill background depending on the difficulty level,
 * - bold white text,
 * - consistent spacing and elevation from `HuntCardUIConstants`.
 *
 * @param difficulty Difficulty level of the hunt (EASY / INTERMEDIATE / DIFFICULT)
 * @param modifier Optional modifier for positioning (e.g. inside an image overlay)
 */
@Composable
fun ModernDifficultyBadge(difficulty: Difficulty, modifier: Modifier = Modifier) {
  val (bg, textColor) =
      when (difficulty) {
        Difficulty.EASY -> HuntCardUIConstants.DifficultyEasy to HuntCardUIConstants.White
        Difficulty.INTERMEDIATE ->
            HuntCardUIConstants.DifficultyIntermediate to HuntCardUIConstants.White
        Difficulty.DIFFICULT -> HuntCardUIConstants.DifficultyHard to HuntCardUIConstants.White
      }

  Surface(
      modifier = modifier,
      shape = RoundedCornerShape(HuntCardUIConstants.DifficultyBadgeCorner),
      color = bg,
      shadowElevation = HuntCardUIConstants.BadgeElevation) {
        Text(
            text = difficulty.toString(),
            modifier =
                Modifier.padding(
                    horizontal = HuntCardUIConstants.Padding12,
                    vertical = HuntCardUIConstants.Padding6),
            fontSize = HuntCardUIConstants.DifficultyFont12,
            fontWeight = FontWeight.Bold,
            color = textColor)
      }
}

/**
 * Displays a small rounded chip showing a numeric value with a unit, preceded by an icon (e.g.
 * distance or duration).
 *
 * The chip uses:
 * - a light background,
 * - a gray icon,
 * - semi-bold text,
 * - internal paddings and radius from `HuntCardUIConstants`.
 *
 * @param icon The icon displayed before the text (ImageVector such as Material Icons)
 * @param value The main numeric value to show (e.g. "5.0")
 * @param unit The unit to display after the value (e.g. "km", "h")
 * @param modifier Optional modifier, usually used with `.weight()` to align chips in a row
 */
@Composable
fun ModernStatChip(icon: ImageVector, value: String, unit: String, modifier: Modifier = Modifier) {
  Surface(
      modifier = modifier,
      shape = RoundedCornerShape(HuntCardUIConstants.StatChipCorner),
      color = HuntCardUIConstants.StatBackground) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = HuntCardUIConstants.Padding12,
                    vertical = HuntCardUIConstants.Padding10),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
              Icon(
                  imageVector = icon,
                  contentDescription = null,
                  modifier = Modifier.size(HuntCardUIConstants.StatIconSize),
                  tint = HuntCardUIConstants.StatIconGray)

              Spacer(modifier = Modifier.width(HuntCardUIConstants.Padding6))

              Text(
                  text = "$value $unit",
                  fontSize = HuntCardUIConstants.StatFont14,
                  fontWeight = FontWeight.SemiBold,
                  color = HuntCardUIConstants.StatTextDark)
            }
      }
}
