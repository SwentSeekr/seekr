package com.swentseekr.seekr.model.hunt

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.theme.LocalAppColors
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

@Composable
fun difficultyColor(difficulty: Difficulty): Color {
  val colors = LocalAppColors.current
  return when (difficulty) {
    Difficulty.EASY -> colors.difficultyEasy
    Difficulty.INTERMEDIATE -> colors.difficultyIntermediate
    Difficulty.DIFFICULT -> colors.difficultyHard
  }
}

@Composable
fun statusColor(status: HuntStatus): Color {
  val colors = LocalAppColors.current
  return when (status) {
    HuntStatus.FUN -> colors.statusFun
    HuntStatus.DISCOVER -> colors.statusDiscover
    HuntStatus.SPORT -> colors.statusSport
  }
}
