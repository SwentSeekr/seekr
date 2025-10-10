package com.swentseekr.seekr.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Displays a user's profile picture in a circular shape.
 *
 * @param profilePicture The resource ID of the profile picture.
 */
@Composable
fun ProfilePicture(profilePicture: Int) {
  val painter =
      if (profilePicture != 0) {
        painterResource(profilePicture)
      } else {
        ColorPainter(Color.Gray)
      }
  Image(
      painter = painter,
      contentDescription = "Profile Picture",
      modifier = Modifier.padding(horizontal = 4.dp).size(100.dp).clip(CircleShape))
}
