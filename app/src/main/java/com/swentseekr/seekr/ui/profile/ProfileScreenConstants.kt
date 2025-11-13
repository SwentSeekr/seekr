package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swentseekr.seekr.model.hunt.Hunt

object ProfileScreenDefaults {
  val HeaderPadding: Dp = 16.dp
  val InfoPadding: Dp = 4.dp
  val BioHorizontalPadding: Dp = 16.dp
  val BioVerticalPadding: Dp = 8.dp
  val HuntsListHorizontalPadding: Dp = 16.dp
  val EmptyStateTopPadding: Dp = 32.dp
  val TabHorizontalPadding: Dp = 40.dp
  val TabVerticalPadding: Dp = 10.dp
  val ProfilePictureSize: Dp = 100.dp
  val NameFontSize: TextUnit = 20.sp
  val BodyFontSize: TextUnit = 16.sp
  val SelectedTabColor: Color = Color.Green
  val UnselectedTabColor: Color = Color.White
  val ErrorTextColor: Color = Color.Red
  val EmptyStateTextColor: Color = Color.Gray
}

object ProfileScreenStrings {
  const val AddHuntContentDescription = "Add"
  const val SettingsContentDescription = "Settings"
  const val ErrorPrefix = "Error: "
  const val NoProfileFound = "No profile found"
  const val NoHuntsYet = "No hunts yet"
  const val ProfilePictureContentDescription = "Profile Picture"
}

object ProfileTestTags {
  const val PROFILE_HUNTS_LIST = "PROFILE_HUNTS_LIST"
  const val TAB_MY_HUNTS = "TAB_MY_HUNTS"
  const val TAB_DONE_HUNTS = "TAB_DONE_HUNTS"
  const val TAB_LIKED_HUNTS = "TAB_LIKED_HUNTS"
  const val ADD_HUNT = "ADD_HUNT"
  const val PROFILE_PICTURE = "PROFILE_PICTURE"
  const val EMPTY_PROFILE_PICTURE = "EMPTY PROFILE_PICTURE"
  const val PROFILE_PSEUDONYM = "PROFILE_PSEUDONYM"
  const val PROFILE_BIO = "PROFILE_BIO"
  const val PROFILE_REVIEW_RATING = "PROFILE_REVIEW_RATING"
  const val PROFILE_SPORT_RATING = "PROFILE_SPORT_RATING"
  const val EMPTY_HUNTS_MESSAGE = "EMPTY_HUNTS_MESSAGE"
  const val PROFILE_SCREEN = "PROFILE_SCREEN"
  const val SETTINGS = "SETTINGS"

  fun getTestTagForHuntCard(hunt: Hunt, index: Int): String = "HUNT_CARD_$index"
}
