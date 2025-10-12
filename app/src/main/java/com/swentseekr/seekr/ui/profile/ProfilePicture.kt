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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swentseekr.seekr.R

/**
 * Displays a user's profile picture in a circular shape.
 *
 * @param profilePicture The resource ID of the profile picture.
 */
@Composable
fun ProfilePicture(profilePicture: Int) {
    val isFallback = profilePicture == 0
  val painter =
      if (isFallback) {
          painterResource(R.drawable.empty_user)
      } else {
          painterResource(profilePicture)
      }
  Image(
      painter = painter,
      contentDescription = "Profile Picture",
      modifier = Modifier.padding(horizontal = 4.dp).size(100.dp).clip(CircleShape).testTag(
          if (isFallback) "EMPTY_PROFILE_PICTURE"
          else ProfileTestTags.PROFILE_PICTURE)
      )
}
