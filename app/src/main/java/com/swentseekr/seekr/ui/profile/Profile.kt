package com.swentseekr.seekr.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.mockProfileData
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.components.MAX_RATING
import com.swentseekr.seekr.ui.components.Rating
import com.swentseekr.seekr.ui.components.RatingType
import com.swentseekr.seekr.ui.theme.*

val BackgroundColorKey = SemanticsPropertyKey<Color>("BackgroundColor")
var SemanticsPropertyReceiver.backgroundColor by BackgroundColorKey

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

        LaunchedEffect(userId) { viewModel.loadProfile(userId) }
        uiState.profile

        AnimatedVisibility(visible = uiState.isLoading, enter = fadeIn(), exit = fadeOut()) {
          Box(
              modifier = Modifier.fillMaxSize().testTag(ProfileTestTags.PROFILE_LOADING),
              contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                      CircularProgressIndicator(color = Green)
                      Text(
                          text = ProfileConstants.LOADING_PROFILE,
                          color = GrayDislike,
                          fontSize = ProfileConstants.TEXT_SIZE_LOADING,
                          modifier = Modifier.padding(top = ProfileConstants.SIZE_MEDIUM_DP))
                    }
              }
        }

        if (uiState.errorMsg != null) {
          Text(
              "${ProfileScreenStrings.ErrorPrefix}${uiState.errorMsg}",
              color = ProfileScreenDefaults.ErrorTextColor)
          return
        }
        uiState.profile
      }

  if (profile == null) {
    Text(ProfileScreenStrings.NoProfileFound, color = ProfileScreenDefaults.EmptyStateTextColor)
    return
  }

  val isMyProfile = testMode || uiState.isMyProfile

  var selectedTab by remember { mutableStateOf(ProfileTab.MY_HUNTS) }
  val reviewCount by viewModel.totalReviews.collectAsState()
  LaunchedEffect(profile.myHunts) { viewModel.loadTotalReviewsForProfile(profile) }

  Scaffold(
      floatingActionButton = {
        if (isMyProfile) {
          FloatingActionButton(
              onClick = onAddHunt, modifier = Modifier.testTag(ProfileTestTags.ADD_HUNT)) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = ProfileScreenStrings.AddHuntContentDescription)
              }
        }
      },
      modifier = Modifier.testTag(ProfileTestTags.PROFILE_SCREEN)) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
          Row(
              modifier = Modifier.fillMaxWidth().padding(ProfileScreenDefaults.HeaderPadding),
              verticalAlignment = Alignment.CenterVertically) {
                ProfilePicture(profilePictureRes = profile.author.profilePicture)
                Column(Modifier.weight(1f)) {
                  Text(
                      text = profile.author.pseudonym,
                      fontSize = ProfileScreenDefaults.NameFontSize,
                      fontWeight = FontWeight.Bold,
                      modifier =
                          Modifier.padding(ProfileScreenDefaults.InfoPadding)
                              .testTag(ProfileTestTags.PROFILE_PSEUDONYM))
                  Row {
                    Text(
                        text = "${profile.author.reviewRate}/${MAX_RATING}",
                        modifier =
                            Modifier.padding(ProfileScreenDefaults.InfoPadding)
                                .testTag(ProfileTestTags.PROFILE_REVIEW_RATING))
                    Rating(rating = profile.author.reviewRate, RatingType.STAR)
                  }
                  Row {
                    Text(
                        text = "${profile.author.sportRate}/${MAX_RATING}",
                        modifier =
                            Modifier.padding(ProfileScreenDefaults.InfoPadding)
                                .testTag(ProfileTestTags.PROFILE_SPORT_RATING))
                    Rating(rating = profile.author.sportRate, RatingType.SPORT)
                  }
                }

                IconButton(
                    onClick = onSettings, modifier = Modifier.testTag(ProfileTestTags.SETTINGS)) {
                      Icon(
                          imageVector = Icons.Default.Settings,
                          contentDescription = ProfileScreenStrings.SettingsContentDescription)
                    }
              }

          Text(
              text = profile.author.bio,
              fontSize = ProfileScreenDefaults.BodyFontSize,
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(
                          horizontal = ProfileScreenDefaults.BioHorizontalPadding,
                          vertical = ProfileScreenDefaults.BioVerticalPadding)
                      .testTag(ProfileTestTags.PROFILE_BIO))

          LazyColumn(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(horizontal = ProfileScreenDefaults.HuntsListHorizontalPadding)
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
                        text = ProfileScreenStrings.NoHuntsYet,
                        color = ProfileScreenDefaults.EmptyStateTextColor,
                        fontSize = ProfileScreenDefaults.BodyFontSize,
                        modifier =
                            Modifier.padding(top = ProfileScreenDefaults.EmptyStateTopPadding)
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
      val color =
          if (selectedTab == item.tab) ProfileScreenDefaults.SelectedTabColor
          else ProfileScreenDefaults.UnselectedTabColor
      Icon(
          imageVector = item.icon,
          contentDescription = item.tab.name,
          modifier =
              Modifier.background(color)
                  .padding(
                      horizontal = ProfileScreenDefaults.TabHorizontalPadding,
                      vertical = ProfileScreenDefaults.TabVerticalPadding)
                  .clickable { onTabSelected(item.tab) }
                  .semantics { backgroundColor = color }
                  .testTag(item.testTag))
    }
  }
}
