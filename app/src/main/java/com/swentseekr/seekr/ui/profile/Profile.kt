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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.mockProfileData
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.components.MAX_RATING
import com.swentseekr.seekr.ui.theme.*
import kotlinx.serialization.Serializable

object ProfileTestTags {
  const val PROFILE_SCREEN = "PROFILE_SCREEN"
  const val PROFILE_LOADING = "PROFILE_LOADING"
  const val PROFILE_PICTURE = "PROFILE_PICTURE"
  const val EMPTY_PROFILE_PICTURE = "EMPTY_PROFILE_PICTURE"
  const val PROFILE_PSEUDONYM = "PROFILE_PSEUDONYM"
  const val PROFILE_BIO = "PROFILE_BIO"
  const val PROFILE_REVIEW_RATING = "PROFILE_REVIEW_RATING"
  const val PROFILE_REVIEWS_COUNT = "PROFILE_REVIEWS_COUNT"
  const val PROFILE_SPORT_RATING = "PROFILE_SPORT_RATING"
  const val PROFILE_HUNTS_DONE_COUNT = "PROFILE_HUNTS_DONE_COUNT"
  const val PROFILE_HUNTS_LIST = "PROFILE_HUNTS_LIST"
  const val EMPTY_HUNTS_MESSAGE = "PROFILE_EMPTY_HUNTS_MESSAGE"
  const val SETTINGS = "SETTINGS"
  const val GO_BACK = "GO_BACK"
  const val ADD_HUNT = "ADD_HUNT"
  const val TAB_MY_HUNTS = "TAB_MY_HUNTS"
  const val TAB_DONE_HUNTS = "TAB_DONE_HUNTS"
  const val TAB_LIKED_HUNTS = "TAB_LIKED_HUNTS"

  fun getTestTagForHuntCard(hunt: Hunt, index: Int): String = "HUNT_CARD_$index"
}

object ProfileConstants {
  val SIZE_SMALL = 4.dp
  val SIZE_MEDIUM_SP = 16.sp
  val TEXT_SIZE_PSEUDONYM = 20.sp
  val TEXT_SIZE_REVIEWS = 14.sp
  val TEXT_SIZE_LOADING = 18.sp
  val PADDING_VERTICAL = 8.dp
  val PADDING_TOP = 32.dp
  val PADDING_ICON_INTERNAL = 2.dp
  val PADDING_ROW = 24.dp
  val SIZE_MEDIUM_DP = 16.dp
  val SIZE_ICON = 40.dp

  const val LOADING_PROFILE = "Loading profile..."
  const val NO_PROFILE_FOUND = "No profile found"
  const val NO_HUNTS_YET = "No hunts yet"
  const val ADD_DESCRIPTION = "Add"
  const val SETTINGS_DESCRIPTION = "Settings Icon"
}

val BackgroundColorKey = SemanticsPropertyKey<Color>("BackgroundColor")
var SemanticsPropertyReceiver.backgroundColor by BackgroundColorKey

data class TabItem(val tab: ProfileTab, val testTag: String, val icon: ImageVector)

@Serializable
data class Profile(
    val uid: String = "",
    val author: Author = Author(),
    val myHunts: MutableList<Hunt> = mutableListOf(),
    val doneHunts: MutableList<Hunt> = mutableListOf(),
    val likedHunts: MutableList<Hunt> = mutableListOf(),
)

enum class ProfileTab {
  MY_HUNTS,
  DONE_HUNTS,
  LIKED_HUNTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String? = null,
    viewModel: ProfileViewModel = viewModel(),
    onAddHunt: () -> Unit = {},
    onSettings: () -> Unit = {},
    onMyHuntClick: (String) -> Unit = {},
    onGoBack: () -> Unit = {},
    testMode: Boolean = false,
    testPublic: Boolean = false,
    testProfile: Profile? = null,
) {

  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  val profile =
      if (testMode) {
        testProfile ?: mockProfileData()
      } else {

        LaunchedEffect(userId) { viewModel.loadProfile(userId, context) }
        uiState.profile

        AnimatedVisibility(visible = uiState.isLoading, enter = fadeIn(), exit = fadeOut()) {
          Box(
              modifier =
                  Modifier.fillMaxSize()
                      .background(Color(0xFFF8F9FA))
                      .testTag(ProfileTestTags.PROFILE_LOADING),
              contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                      CircularProgressIndicator(color = Color(0xFF00C853))
                      Text(
                          text = ProfileConstants.LOADING_PROFILE,
                          color = Color(0xFF666666),
                          fontSize = ProfileConstants.TEXT_SIZE_LOADING,
                          modifier = Modifier.padding(top = ProfileConstants.SIZE_MEDIUM_DP))
                    }
              }
        }

        if (uiState.errorMsg != null) {
          Box(
              modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)),
              contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.errorMsg}", color = Color(0xFFEF5350))
              }
          return
        }
        uiState.profile
      }

  if (profile == null) {
    AnimatedVisibility(visible = !uiState.isLoading, enter = fadeIn(), exit = fadeOut()) {
      Box(
          modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)),
          contentAlignment = Alignment.Center) {
            Text(ProfileConstants.NO_PROFILE_FOUND, color = Color(0xFF666666))
          }
    }
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
              onClick = onAddHunt,
              modifier = Modifier.testTag(ProfileTestTags.ADD_HUNT),
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = Color.White,
              shape = CircleShape) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = ProfileConstants.ADD_DESCRIPTION,
                    modifier = Modifier.size(28.dp))
              }
        }
      },
      containerColor = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier.testTag(ProfileTestTags.PROFILE_SCREEN)) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
          // MODERN HERO HEADER
          ModernProfileHeader(
              profile = profile,
              reviewCount = reviewCount,
              isMyProfile = isMyProfile,
              testPublic = testPublic,
              onSettings = onSettings,
              onGoBack = onGoBack)

          // MODERN TABS
          ModernCustomToolbar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

          // HUNTS LIST
          val huntsToDisplay =
              when (selectedTab) {
                ProfileTab.MY_HUNTS -> profile.myHunts
                ProfileTab.DONE_HUNTS -> profile.doneHunts
                ProfileTab.LIKED_HUNTS -> profile.likedHunts
              }

          LazyColumn(
              modifier = Modifier.fillMaxSize().testTag(ProfileTestTags.PROFILE_HUNTS_LIST),
              horizontalAlignment = Alignment.CenterHorizontally) {
                if (huntsToDisplay.isEmpty()) {
                  item { ModernEmptyHuntsState(selectedTab) }
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

@Composable
fun ModernProfileHeader(
    profile: Profile,
    reviewCount: Int,
    isMyProfile: Boolean,
    testPublic: Boolean,
    onSettings: () -> Unit,
    onGoBack: () -> Unit
) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .background(
                  Brush.verticalGradient(
                      colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFFE8847C))))) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
          // Settings or Go Back button
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (isMyProfile && !testPublic) {
              Surface(
                  onClick = onSettings,
                  modifier = Modifier.testTag(ProfileTestTags.SETTINGS),
                  shape = CircleShape,
                  color = Color.White.copy(alpha = 0.2f)) {
                    IconButton(onClick = onSettings) {
                      Icon(
                          imageVector = Icons.Default.Settings,
                          contentDescription = ProfileConstants.SETTINGS_DESCRIPTION,
                          tint = Color.White,
                          modifier = Modifier.size(24.dp))
                    }
                  }
            } else {
              Surface(
                  onClick = onGoBack,
                  modifier = Modifier.testTag(ProfileTestTags.GO_BACK),
                  shape = CircleShape,
                  color = Color.White.copy(alpha = 0.2f)) {
                    IconButton(onClick = onGoBack) {
                      Icon(
                          imageVector = Icons.Default.Close,
                          contentDescription = ProfileScreenConstants.ICON_BUTTON_GOBACK,
                          tint = Color.White,
                          modifier = Modifier.size(24.dp))
                    }
                  }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Profile Picture & Name - Horizontal Layout
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ProfilePicture(
                profilePictureRes = profile.author.profilePicture,
                profilePictureUrl = profile.author.profilePictureUrl,
                modifier = Modifier.size(70.dp).clip(CircleShape))

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                  text = profile.author.pseudonym,
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.testTag(ProfileTestTags.PROFILE_PSEUDONYM))

              Spacer(modifier = Modifier.height(4.dp))

              // Bio
              Text(
                  text = profile.author.bio,
                  fontSize = 14.sp,
                  color = Color.White.copy(alpha = 0.85f),
                  maxLines = 2,
                  modifier = Modifier.testTag(ProfileTestTags.PROFILE_BIO))
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Stats Cards
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ModernStatCard(
                icon = painterResource(R.drawable.full_star),
                value = "${profile.author.reviewRate}",
                label = "$reviewCount reviews",
                modifier = Modifier.weight(1f),
                testTagValue = ProfileTestTags.PROFILE_REVIEW_RATING,
                testTagLabel = ProfileTestTags.PROFILE_REVIEWS_COUNT)

            Spacer(modifier = Modifier.width(12.dp))

            ModernStatCard(
                icon = painterResource(R.drawable.full_sport),
                value = "${profile.author.sportRate}",
                label = "${profile.doneHunts.size} Hunts done",
                modifier = Modifier.weight(1f),
                testTagValue = ProfileTestTags.PROFILE_SPORT_RATING,
                testTagLabel = ProfileTestTags.PROFILE_HUNTS_DONE_COUNT)
          }
        }
      }
}

@Composable
fun ModernStatCard(
    icon: Painter,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    testTagValue: String,
    testTagLabel: String
) {
  Card(
      modifier = modifier,
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
      shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  painter = icon,
                  contentDescription = null,
                  tint = Color.Unspecified,
                  modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                    text = "$value/${MAX_RATING}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.testTag(testTagValue))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.testTag(testTagLabel))
              }
            }
      }
}

@Composable
fun ModernCustomToolbar(selectedTab: ProfileTab, onTabSelected: (ProfileTab) -> Unit = {}) {
  val tabs =
      listOf(
          TabItem(ProfileTab.MY_HUNTS, ProfileTestTags.TAB_MY_HUNTS, Icons.Filled.Menu),
          TabItem(ProfileTab.DONE_HUNTS, ProfileTestTags.TAB_DONE_HUNTS, Icons.Filled.Check),
          TabItem(ProfileTab.LIKED_HUNTS, ProfileTestTags.TAB_LIKED_HUNTS, Icons.Filled.Favorite))

  Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly) {
          tabs.forEach { item ->
            val isSelected = selectedTab == item.tab
            val color = if (isSelected) Color(0xFF00C853) else Color(0xFF999999)
            val backgroundColor =
                if (isSelected) Color(0xFF00C853).copy(alpha = 0.1f) else Color.Transparent

            Surface(
                modifier =
                    Modifier.weight(1f)
                        .padding(horizontal = 8.dp)
                        .clickable { onTabSelected(item.tab) }
                        .semantics { this.backgroundColor = color }
                        .testTag(item.testTag),
                shape = RoundedCornerShape(12.dp),
                color = backgroundColor) {
                  Column(
                      modifier = Modifier.padding(vertical = 12.dp),
                      horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.tab.name,
                            tint = color,
                            modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text =
                                when (item.tab) {
                                  ProfileTab.MY_HUNTS -> "My Hunts"
                                  ProfileTab.DONE_HUNTS -> "Done"
                                  ProfileTab.LIKED_HUNTS -> "Liked"
                                },
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = color)
                      }
                }
          }
        }
  }
}

@Composable
fun ModernEmptyHuntsState(selectedTab: ProfileTab) {
  Box(
      modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
      contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          val icon =
              when (selectedTab) {
                ProfileTab.MY_HUNTS -> Icons.Filled.Menu
                ProfileTab.DONE_HUNTS -> Icons.Filled.Check
                ProfileTab.LIKED_HUNTS -> Icons.Filled.Favorite
              }

          Icon(
              imageVector = icon,
              contentDescription = null,
              modifier = Modifier.size(64.dp),
              tint = Color(0xFFCCCCCC))
          Spacer(modifier = Modifier.height(16.dp))
          Text(
              text = ProfileConstants.NO_HUNTS_YET,
              color = Color(0xFF999999),
              fontSize = 16.sp,
              modifier = Modifier.testTag(ProfileTestTags.EMPTY_HUNTS_MESSAGE))
        }
      }
}

// Keep the old CustomToolbar for compatibility if needed
@Composable
fun CustomToolbar(selectedTab: ProfileTab, onTabSelected: (ProfileTab) -> Unit = {}) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    val tabs =
        listOf(
            TabItem(ProfileTab.MY_HUNTS, ProfileTestTags.TAB_MY_HUNTS, Icons.Filled.Menu),
            TabItem(ProfileTab.DONE_HUNTS, ProfileTestTags.TAB_DONE_HUNTS, Icons.Filled.Check),
            TabItem(ProfileTab.LIKED_HUNTS, ProfileTestTags.TAB_LIKED_HUNTS, Icons.Filled.Favorite))

    tabs.forEach { item ->
      val color = if (selectedTab == item.tab) Green else White
      Icon(
          imageVector = item.icon,
          contentDescription = item.tab.name,
          modifier =
              Modifier.background(color)
                  .padding(horizontal = ProfileConstants.SIZE_ICON, vertical = 10.dp)
                  .clickable { onTabSelected(item.tab) }
                  .semantics { backgroundColor = color }
                  .testTag(item.testTag))
    }
  }
}
