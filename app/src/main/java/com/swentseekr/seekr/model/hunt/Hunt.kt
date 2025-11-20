package com.swentseekr.seekr.model.hunt

import androidx.compose.ui.graphics.Color
import com.swentseekr.seekr.model.map.Location
import kotlinx.serialization.Serializable

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

@Serializable
enum class HuntStatus {
  FUN,
  DISCOVER,
  SPORT
}

@Serializable
enum class Difficulty {
  EASY,
  INTERMEDIATE,
  DIFFICULT
}

fun DifficultyColor(difficulty: Difficulty): Color {
  return when (difficulty) {
    Difficulty.EASY -> Color.Green // Green
    Difficulty.INTERMEDIATE -> Color.Yellow // Yellow
    Difficulty.DIFFICULT -> Color.Red // Red
  }
}

fun StatusColor(status: HuntStatus): Int {
  return when (status) {
    HuntStatus.FUN -> 0x60BA37 // Green
    HuntStatus.DISCOVER -> 0xFFDFAD // Yellow
    HuntStatus.SPORT -> 0xFFC1C1 // Red
  }
}
