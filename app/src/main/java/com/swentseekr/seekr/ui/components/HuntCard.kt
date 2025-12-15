package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.theme.LocalAppColors

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
 * @param authorName the name of the author that creates the hunt
 * @param isLiked a boolean that indicates if the hunt is liked or not
 * @param onLikeClick a callback for liking the hunt
 * @param modifier Optional modifier for external styling or click handling.
 */
@Composable
fun HuntCard(
    hunt: Hunt,
    authorName: String = "",
    isLiked: Boolean = false,
    onLikeClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Card(
        modifier =
            modifier
                .padding(HuntCardUIConstants.CardPadding)
                .fillMaxWidth(HuntCardUIConstants.CardWidthFraction),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                    contentDescription = HuntCardScreenStrings.HUNT_PICTURE_DESCRIPTION,
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
                                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)))))

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
                            .testTag(HuntCardScreenStrings.LIKE_BUTTON)) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = HuntCardScreenStrings.LIKE_BUTTON,
                        tint =
                            if (isLiked) colors.liked
                            else colors.disliked,
                        modifier = Modifier.size(HuntCardUIConstants.IconSize28))
                }

                // TITLE + AUTHOR
                Column(
                    modifier =
                        Modifier.align(Alignment.BottomStart)
                            .padding(HuntCardUIConstants.Padding16)) {
                    Text(
                        text = hunt.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.height(HuntCardUIConstants.Padding4))
                    Text(
                        text = "${HuntCardScreenStrings.BY} " + authorName,
                        style = MaterialTheme.typography.labelSmall,
                        color =
                            MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = HuntCardScreenDefaults.TEXT_COLOR_FACTOR),
                    )
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
                    icon = StatIcon.Vector(Icons.Filled.LocationOn),
                    value = "${hunt.distance}",
                    unit = HuntCardScreenStrings.DISTANCE_UNIT,
                    modifier = Modifier.weight(HuntCardScreenDefaults.WEIGHT_LOCATION_MODIFIER))

                Spacer(modifier = Modifier.width(HuntCardUIConstants.Padding8))

                ModernStatChip(
                    icon = StatIcon.PainterIcon(painter = painterResource(R.drawable.clock)),
                    value = "${hunt.time}",
                    unit = HuntCardScreenStrings.TIME_UNIT,
                    modifier = Modifier.weight(HuntCardScreenDefaults.WEIGHT_TIME_MODIFIER))
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
    val colors = LocalAppColors.current
    val (bg, textColor) =
        when (difficulty) {
            Difficulty.EASY -> colors.difficultyEasy to MaterialTheme.colorScheme.onPrimary
            Difficulty.INTERMEDIATE ->
                colors.difficultyIntermediate to MaterialTheme.colorScheme.onPrimary
            Difficulty.DIFFICULT -> colors.difficultyHard to MaterialTheme.colorScheme.onPrimary
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
            style = MaterialTheme.typography.labelSmall,
            color = textColor)
    }
}

/**
 * Represents an icon that can be rendered inside a stat chip.
 *
 * `StatIcon` abstracts over two possible icon sources:
 * - [Vector] — a standard Compose [ImageVector] such as Material Icons.
 * - [PainterIcon] — a drawable-based icon using a [Painter], typically from `painterResource()`
 *   (e.g., custom SVGs imported as VectorDrawables).
 *
 * This sealed class allows `ModernStatChip` to support both built-in Material icons and custom
 * SVG-based icons without changing its public API.
 */
sealed class StatIcon {
    data class Vector(val icon: ImageVector) : StatIcon()

    data class PainterIcon(val painter: Painter) : StatIcon()
}

/**
 * Displays a small rounded chip showing a numeric value with a unit, preceded by an icon. Common
 * use cases include distance, duration, or other numerical stats in a Hunt card.
 *
 * This version accepts a [StatIcon], allowing callers to provide either:
 * - a Material Design [ImageVector] (via [StatIcon.Vector]), or
 * - a custom SVG/drawable icon using a [Painter] (via [StatIcon.PainterIcon]).
 *
 * The chip includes:
 * - a light pill-shaped background,
 * - an icon tinted with a standardized gray color,
 * - a value+unit text section.
 *
 * @param icon The icon to display in the chip, either vector-based or painter-based.
 * @param value The numeric or textual value to show (e.g., "5.0").
 * @param unit The unit displayed after the value (e.g., "km", "h").
 * @param modifier Optional modifier for size, layout, or styling.
 */
@Composable
fun ModernStatChip(icon: StatIcon, value: String, unit: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(HuntCardUIConstants.StatChipCorner),
        color = MaterialTheme.colorScheme.tertiaryContainer) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = HuntCardUIConstants.Padding12,
                    vertical = HuntCardUIConstants.Padding10),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            when (icon) {
                is StatIcon.Vector ->
                    Icon(
                        imageVector = icon.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(HuntCardUIConstants.StatIconSize))
                is StatIcon.PainterIcon ->
                    Icon(
                        painter = icon.painter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(HuntCardUIConstants.StatIconSize))
            }

            Spacer(modifier = Modifier.width(HuntCardUIConstants.Padding6))

            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}
