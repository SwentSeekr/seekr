package com.swentseekr.seekr.model.hunt

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.theme.LocalAppColors
import kotlinx.serialization.Serializable

/**
 * Represents a hunt within the application.
 *
 * @property uid Unique identifier for the hunt.
 * @property start Starting location of the hunt.
 * @property end Ending location of the hunt.
 * @property middlePoints List of intermediate locations along the hunt.
 * @property status Current category/status of the hunt ([HuntStatus]).
 * @property title Display title of the hunt.
 * @property description Textual description of the hunt.
 * @property time Estimated time to complete the hunt (in minutes or hours).
 * @property distance Total distance of the hunt.
 * @property difficulty Difficulty level of the hunt ([Difficulty]).
 * @property authorId User ID of the author who created the hunt.
 * @property otherImagesUrls Optional list of URLs for additional hunt images.
 * @property mainImageUrl URL of the main image for the hunt.
 * @property reviewRate Average review rating of the hunt.
 */
@Serializable
data class Hunt(
    val uid: String,
    val start: Location,
    val end: Location,
    val middlePoints: List<Location>,
    val status: HuntStatus,
    val title: String,
    val description: String,
    val time: Double,
    val distance: Double,
    val difficulty: Difficulty,
    val authorId: String,
    val otherImagesUrls: List<String> = emptyList(),
    val mainImageUrl: String,
    val reviewRate: Double
)

/** Enum representing the category or purpose of a hunt. */
@Serializable
enum class HuntStatus {
  /** Hunt meant for fun and casual enjoyment. */
  FUN,

  /** Hunt designed for exploration or discovery. */
  DISCOVER,

  /** Hunt designed for sports or fitness purposes. */
  SPORT
}

/** Enum representing the difficulty level of a hunt. */
@Serializable
enum class Difficulty {
  /** Easy level suitable for beginners. */
  EASY,

  /** Intermediate level requiring some experience or effort. */
  INTERMEDIATE,

  /** Difficult level suitable for advanced users. */
  DIFFICULT
}

/**
 * Returns a Compose [Color] corresponding to a [Difficulty] level.
 *
 * @param difficulty The difficulty level.
 * @return Green for EASY, Yellow for INTERMEDIATE, Red for DIFFICULT.
 */
@Composable
fun difficultyColor(difficulty: Difficulty): Color {
  val colors = LocalAppColors.current
  return when (difficulty) {
    Difficulty.EASY -> colors.difficultyEasy
    Difficulty.INTERMEDIATE -> colors.difficultyIntermediate
    Difficulty.DIFFICULT -> colors.difficultyHard
  }
}

/**
 * Returns a hexadecimal color integer corresponding to a [HuntStatus].
 *
 * @param status The hunt status.
 * @return Green for FUN, Yellow for DISCOVER, Red for SPORT.
 */
@Composable
fun statusColor(status: HuntStatus): Color {
  val colors = LocalAppColors.current
  return when (status) {
    HuntStatus.FUN -> colors.statusFun
    HuntStatus.DISCOVER -> colors.statusDiscover
    HuntStatus.SPORT -> colors.statusSport
  }
}
