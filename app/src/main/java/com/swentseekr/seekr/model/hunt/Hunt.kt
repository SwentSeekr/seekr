package com.android.sample.model.hunt

import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.map.Location


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
    val author: Author,
    val image: Int,
    val reviewRate: Double
)

enum class HuntStatus {
    FUN,
    DISCOVER,
    SPORT
}

enum class Difficulty {
    EASY,
    INTERMEDIATE,
    DIFFICULT
}
fun DifficultyColor(difficulty: Difficulty): Int {
    return when (difficulty) {
        Difficulty.EASY -> 0x60BA37 // Green
        Difficulty.INTERMEDIATE ->   0xFFDFAD // Yellow
        Difficulty.DIFFICULT ->   0xFFC1C1// Red
    }

}
fun StatusColor(status: HuntStatus): Int {
    return when (status) {
        HuntStatus.FUN -> 0x60BA37 // Green
        HuntStatus.DISCOVER -> 0xFFDFAD // Yellow
        HuntStatus.SPORT -> 0xFFC1C1 // Red
    }

}