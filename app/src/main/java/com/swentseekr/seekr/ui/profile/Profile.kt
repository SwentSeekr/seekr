package com.swentseekr.seekr.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.mockProfileData
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.components.MAX_RATING
import com.swentseekr.seekr.ui.components.Rating
import com.swentseekr.seekr.ui.components.RatingType

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

val TEXT_SIZE = 4.dp
val BackgroundColorKey = SemanticsPropertyKey<Color>("BackgroundColor")

data class TabItem(val tab: ProfileTab, val testTag: String, val icon: ImageVector)

/**
 * Data class representing a user's profile.
 *
 * @property uid Unique identifier of the user.
 * @property author Author details of the user.
 * @property myHunts Hunts created by the user.
 * @property doneHunts Hunts completed by the user.
 * @property likedHunts Hunts liked by the user.
 */
data class Profile(
    val uid: String = "",
    val author: Author = Author(),
    val myHunts: MutableList<Hunt> = mutableListOf(),
    val doneHunts: MutableList<Hunt> = mutableListOf(),
    val likedHunts: MutableList<Hunt> = mutableListOf(),
)

/** Enum representing the different tabs in the profile screen. */
enum class ProfileTab {
  MY_HUNTS,
  DONE_HUNTS,
  LIKED_HUNTS
}

/**
 * Displays the profile screen of a user with their info, ratings, bio, and hunts.
 *
 * @param profile The profile data to display.
 * @param userId The ID of the user's profile visited.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String? = null,
    viewModel: ProfileViewModel = viewModel(),
    onAddHunt: () -> Unit = {},
    onSettings: () -> Unit = {},
    onMyHuntClick: (String) -> Unit = {},
    testMode: Boolean = false,
    testProfile: Profile? = null,
) {
  val uiState by viewModel.uiState.collectAsState()

  val profile =
      if (testMode) {
        testProfile ?: mockProfileData()
      } else {
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(userId) { viewModel.loadProfile(userId) }
        if (uiState.errorMsg != null) {
          Text("Error: ${uiState.errorMsg}", color = Color.Red)
          return
        }
        uiState.profile
      }

  if (profile == null) {
    Text("No profile found", color = Color.Gray)
    return
  }

  val isMyProfile = testMode || uiState.isMyProfile

  var selectedTab by remember { mutableStateOf(ProfileTab.MY_HUNTS) }
  Scaffold(
      floatingActionButton = {
        if (isMyProfile) {
          FloatingActionButton(
              onClick = onAddHunt, modifier = Modifier.testTag(ProfileTestTags.ADD_HUNT)) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
              }
        }
      },
      modifier = Modifier.testTag(ProfileTestTags.PROFILE_SCREEN)

      // TODO settings

      ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
          Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalAlignment = Alignment.CenterVertically) {
                ProfilePicture(profilePictureRes = profile.author.profilePicture)
                Column(Modifier.weight(1f)) {
                  Text(
                      text = profile.author.pseudonym,
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold,
                      modifier =
                          Modifier.padding(TEXT_SIZE).testTag(ProfileTestTags.PROFILE_PSEUDONYM))
                  Row {
                    Text(
                        text = "${profile.author.reviewRate}/${MAX_RATING}",
                        modifier =
                            Modifier.padding(TEXT_SIZE)
                                .testTag(ProfileTestTags.PROFILE_REVIEW_RATING))
                    Rating(rating = profile.author.reviewRate, RatingType.STAR)
                  }
                  Row {
                    Text(
                        text = "${profile.author.sportRate}/${MAX_RATING}",
                        modifier =
                            Modifier.padding(TEXT_SIZE)
                                .testTag(ProfileTestTags.PROFILE_SPORT_RATING))
                    Rating(rating = profile.author.sportRate, RatingType.SPORT)
                  }
                }

                IconButton(
                    onClick = onSettings, modifier = Modifier.testTag(ProfileTestTags.SETTINGS)) {
                      Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
              }

          Text(
              text = profile.author.bio,
              fontSize = 16.sp,
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 16.dp, vertical = 8.dp)
                      .testTag(ProfileTestTags.PROFILE_BIO))

          LazyColumn(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(horizontal = 16.dp)
                      .testTag(ProfileTestTags.PROFILE_HUNTS_LIST),
              horizontalAlignment = Alignment.CenterHorizontally) {
                item { CustomToolbar(selectedTab, onTabSelected = { selectedTab = it }) }
                val huntsToDisplay =
                    when (selectedTab) {
                      ProfileTab.MY_HUNTS -> profile.myHunts
                      ProfileTab.DONE_HUNTS -> profile.doneHunts
                      ProfileTab.LIKED_HUNTS -> profile.likedHunts
                    }
                if (huntsToDisplay.isEmpty()) {
                  item {
                    Text(
                        text = "No hunts yet",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier =
                            Modifier.padding(top = 32.dp)
                                .align(Alignment.CenterHorizontally)
                                .testTag(ProfileTestTags.EMPTY_HUNTS_MESSAGE))
                  }
                } else {
                  items(huntsToDisplay.size) { index ->
                    val hunt = huntsToDisplay[index]
                    val base = Modifier.testTag(ProfileTestTags.getTestTagForHuntCard(hunt, index))
                    val clickable =
                        if (selectedTab == ProfileTab.MY_HUNTS) {
                          base.clickable { onMyHuntClick(hunt.uid) }
                        } else base

                    HuntCard(hunt, modifier = clickable)
                  }
                }
              }
        }
      }
}

/**
 * Displays a toolbar with tabs for switching between "My Hunts", "Done Hunts", and "Liked Hunts".
 *
 * @param selectedTab The currently selected tab.
 * @param onTabSelected Callback when a tab is selected.
 */
@Composable
fun CustomToolbar(selectedTab: ProfileTab, onTabSelected: (ProfileTab) -> Unit = {}) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    val tabs =
        listOf(
            TabItem(ProfileTab.MY_HUNTS, ProfileTestTags.TAB_MY_HUNTS, Icons.Filled.Menu),
            TabItem(ProfileTab.DONE_HUNTS, ProfileTestTags.TAB_DONE_HUNTS, Icons.Filled.Check),
            TabItem(ProfileTab.LIKED_HUNTS, ProfileTestTags.TAB_LIKED_HUNTS, Icons.Filled.Favorite))

    tabs.forEach { item ->
      val color = if (selectedTab == item.tab) Color.Green else Color.White
      Icon(
          imageVector = item.icon,
          contentDescription = item.tab.name,
          modifier =
              Modifier.background(color)
                  .padding(horizontal = 40.dp, vertical = 10.dp)
                  .clickable { onTabSelected(item.tab) }
                  .semantics { this[BackgroundColorKey] = color }
                  .testTag(item.testTag))
    }
  }
}
