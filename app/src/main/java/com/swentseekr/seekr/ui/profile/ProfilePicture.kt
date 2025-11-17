package com.swentseekr.seekr.ui.profile

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.swentseekr.seekr.R

const val PROFILE_PICTURE_SIZE_DP = 100
const val DEFAULT_PROFILE_PICTURE = 0
/**
 * Displays a user's profile picture in a circular shape.
 *
 * @param profilePictureRes The drawable resource ID of the profile picture (fallback if Uri/Url is
 *   null).
 * @param profilePictureUri The URI of a picked image from gallery or camera (optional).
 * @param profilePictureUrl The Firebase Storage URL string (optional).
 */
@Composable
fun ProfilePicture(
    profilePictureRes: Int = DEFAULT_PROFILE_PICTURE,
    profilePictureUri: Uri? = null,
    profilePictureUrl: String? = null,
    modifier: Modifier = Modifier
) {
  val isFallback =
      profilePictureUri == null &&
          profilePictureRes == DEFAULT_PROFILE_PICTURE &&
          profilePictureUrl.isNullOrEmpty()

  val painter =
      when {
        profilePictureUri != null -> rememberAsyncImagePainter(profilePictureUri)
        !profilePictureUrl.isNullOrEmpty() -> rememberAsyncImagePainter(profilePictureUrl)
        profilePictureRes != DEFAULT_PROFILE_PICTURE -> painterResource(profilePictureRes)
        else -> painterResource(R.drawable.empty_user)
      }

  Image(
      painter = painter,
      contentDescription = "Profile Picture",
      modifier =
          modifier
              .size(PROFILE_PICTURE_SIZE_DP.dp)
              .clip(CircleShape)
              .testTag(
                  if (isFallback) ProfileTestTags.EMPTY_PROFILE_PICTURE
                  else ProfileTestTags.PROFILE_PICTURE))
}
