package com.swentseekr.seekr.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Constants extracted from HuntCard composable to eliminate hardcoded values. UI remains strictly
 * identical.
 */
object HuntCardUIConstants {

  // --- Paddings ---
  val Padding4 = 4.dp
  val Padding6 = 6.dp
  val Padding8 = 8.dp
  val Padding10 = 10.dp
  val Padding12 = 12.dp
  val Padding16 = 16.dp
  val Padding20 = 20.dp

  // --- Margins & Layout ---
  val CardPadding = 12.dp
  val CardWidthFraction = 0.92f
  val ImageHeight = 200.dp
  val CornerRadius = 20.dp
  val StatChipCorner = 12.dp
  val DifficultyBadgeCorner = 20.dp

  // --- Sizes ---
  val IconSize18 = 18.dp
  val IconSize20 = 20.dp
  val IconSize24 = 24.dp
  val IconSize28 = 28.dp
  val StatIconSize = 18.dp

  // --- Font sizes ---
  val TitleFont24 = 24.sp
  val AuthorFont14 = 14.sp
  val StatFont14 = 14.sp
  val DifficultyFont12 = 12.sp

  // --- Colors (identical to original) ---
  val White = Color.White
  val Black30 = Color.Black.copy(alpha = 0.3f)
  val Black70 = Color.Black.copy(alpha = 0.7f)
  val LikeRed = Color(0xFFFF5252)
  val DifficultyEasy = Color(0xFF4CAF50)
  val DifficultyIntermediate = Color(0xFFFFA726)
  val DifficultyHard = Color(0xFFEF5350)
  val StatIconGray = Color(0xFF666666)
  val StatTextDark = Color(0xFF333333)
  val StatBackground = Color(0xFFF5F5F5)

  // --- Elevations ---
  val CardElevation = 8.dp
  val BadgeElevation = 4.dp
}
