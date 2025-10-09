package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swentseekr.seekr.R

/**
 * Displays a rating as a row of icons. Supports both star ratings and sport ratings.
 *
 * @param rating The numeric rating (0.0 - 5.0).
 * @param type The type of rating to display (STAR or SPORT).
 */
@Composable
fun Rating(rating: Double, type: RatingType) {

  val image =
      if (type == RatingType.SPORT) {
        R.drawable.run_emoji
      } else {
        R.drawable.full_star
      }
  Row {
    val fullStars = rating.toInt()
    val hasHalfStar = (rating - fullStars) >= 0.5
    val emptyStars = 5 - fullStars - if (hasHalfStar) 1 else 0

    repeat(fullStars) {
      Image(
          painter = painterResource(image),
          contentDescription = "Full Rating",
          modifier = Modifier.size(20.dp))
    }
    if (hasHalfStar) {
      Image(
          painter = painterResource(image), // I should change the image.
          //I will do this as soon as I have a half image in my resources
          contentDescription = "Half Rating",
          modifier = Modifier.size(20.dp))
    }
    repeat(emptyStars) {
      Image(
          painter = painterResource(image),
          // I should change the image.
          //I will do this as soon as I have a half image in my resources
          contentDescription = "Empty Rating",
          modifier = Modifier.size(20.dp))
    }
  }
}

/** Enum representing the type of rating displayed in [Rating]. */
enum class RatingType {
  STAR,
  SPORT
}
