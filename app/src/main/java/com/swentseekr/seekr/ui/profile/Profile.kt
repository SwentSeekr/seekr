package com.swentseekr.seekr.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.mockProfileData
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.components.MAX_RATING
import com.swentseekr.seekr.ui.profile.ProfileScreenConstants.HUNTS_DONE_LABEL
import com.swentseekr.seekr.ui.profile.ProfileScreenConstants.MULTIPLE_REVIEWS_LABEL
import com.swentseekr.seekr.ui.profile.ProfileScreenConstants.ONE_DECIMAL_FORMAT
import com.swentseekr.seekr.ui.profile.ProfileScreenConstants.SINGLE_REVIEW
import com.swentseekr.seekr.ui.profile.ProfileScreenConstants.SINGLE_REVIEW_LABEL
import com.swentseekr.seekr.ui.profile.ProfileUIConstantsDefaults.ALPHA_LIGHT
import com.swentseekr.seekr.ui.theme.ProfileTypography
import kotlinx.serialization.Serializable

// -------------------------
// TEST TAGS
// -------------------------
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
  const val LIKE_BUTTON = "_LIKE_BUTTON"

  fun getTestTagForHuntCard(hunt: Hunt, index: Int): String = "HUNT_CARD_${hunt.uid}"
}

// -------------------------
// ORIGINAL CONSTANTS
// -------------------------
object ProfileConstants {
  val SIZE_MEDIUM_DP = 16.dp
  val SIZE_ICON = 40.dp
  const val ALPHA = 0.6f

  const val LOADING_PROFILE = "Loading profile..."
  const val NO_PROFILE_FOUND = "No profile found"
  const val NO_HUNTS_YET = "No hunts yet"
  const val ADD_DESCRIPTION = "Add"
  const val SETTINGS_DESCRIPTION = "Settings Icon"

  const val ERROR = "Error:"
  const val PROFILE_PICTURE_DESCRIPTION = "Profile Picture"
}

// -------------------------
// SEMANTICS
// -------------------------
val BackgroundColorKey = SemanticsPropertyKey<Color>("BackgroundColor")
var SemanticsPropertyReceiver.backgroundColor by BackgroundColorKey

// -------------------------
// DATA CLASSES
// -------------------------
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

// -------------------------
// MAIN SCREEN COMPOSABLE
// -------------------------
/**
 * Main screen displaying a user's profile.
 *
 * This screen shows:
 * - A profile header with avatar, pseudonym, bio, and statistics
 * - Tabs to switch between the user's hunts, completed hunts, and liked hunts
 * - A list of hunts corresponding to the selected tab
 * - A floating action button to add a hunt when viewing own profile
 * - Loading and error states
 *
 * Behavior:
 * - Loads the profile from the ViewModel unless running in test mode
 * - Adapts UI depending on whether the profile belongs to the current user
 * - Supports navigation to hunt details, settings, reviews, and add-hunt flow
 *
 * Testing:
 * - Allows injection of a mock profile
 * - Exposes extensive test tags for UI testing
 * - Can simulate public or private profile states
 *
 * @param userId Optional ID of the user whose profile is displayed.
 * @param viewModel ViewModel responsible for loading and managing profile data.
 * @param onAddHunt Callback invoked when the add-hunt button is clicked.
 * @param onSettings Callback invoked when the settings button is clicked.
 * @param onMyHuntClick Callback invoked when a hunt card is clicked, passing the hunt ID.
 * @param onGoBack Callback invoked when the back button is clicked.
 * @param testMode Enables test mode, bypassing ViewModel loading.
 * @param testPublic Forces the profile to behave as a public profile (used for tests).
 * @param testProfile Optional profile used when test mode is enabled.
 * @param onReviewsClick Callback invoked when the reviews statistic card is clicked.
 */
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
    onReviewsClick: () -> Unit = {},
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(userId) { if (!testMode) viewModel.loadProfile(userId, context) }

  val profile =
      if (testMode) {
        testProfile ?: mockProfileData()
      } else {
        uiState.profile
      }

  // LOADING UI
  AnimatedVisibility(visible = uiState.isLoading, enter = fadeIn(), exit = fadeOut()) {
    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .testTag(ProfileTestTags.PROFILE_LOADING),
        contentAlignment = Alignment.Center) {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = ProfileConstants.LOADING_PROFILE,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = ProfileTypography.bodyMedium,
                    modifier = Modifier.padding(top = ProfileConstants.SIZE_MEDIUM_DP))
              }
        }
  }

  if (uiState.errorMsg != null) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center) {
          Text(
              "${ProfileConstants.ERROR} ${uiState.errorMsg}",
              color = MaterialTheme.colorScheme.error,
              style = ProfileTypography.bodyLarge)
        }
    return
  }

  uiState.profile

  if (profile == null) {
    AnimatedVisibility(visible = !uiState.isLoading, enter = fadeIn(), exit = fadeOut()) {
      Box(
          modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center) {
            Text(
                ProfileConstants.NO_PROFILE_FOUND,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = ProfileTypography.bodyLarge)
          }
    }
    return
  }

  val isMyProfile = testMode || uiState.isMyProfile
  var selectedTab by remember { mutableStateOf(ProfileTab.MY_HUNTS) }

  val reviewCount by viewModel.totalReviews.collectAsState()
  LaunchedEffect(profile.myHunts) { viewModel.loadTotalReviewsForProfile(profile) }
  LaunchedEffect(profile) { profile.let { viewModel.loadAllReviewsForProfile(it) } }
  Scaffold(
      floatingActionButton = {
        if (isMyProfile) {
          FloatingActionButton(
              onClick = onAddHunt,
              modifier = Modifier.testTag(ProfileTestTags.ADD_HUNT),
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              shape = CircleShape) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = ProfileConstants.ADD_DESCRIPTION,
                    modifier = Modifier.size(ProfileUIConstantsDefaults.Size28)) // AA
          }
        }
      },
      containerColor = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier.testTag(ProfileTestTags.PROFILE_SCREEN)) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

          // ---- HEADER SECTION ----
          ModernProfileHeader(
              profile = profile,
              reviewCount = reviewCount,
              isMyProfile = isMyProfile,
              testPublic = testPublic,
              onSettings = onSettings,
              onGoBack = onGoBack,
              onReviewsClick = onReviewsClick)

          // ---- TABS SECTION ----
          ModernCustomToolbar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

          // ---- HUNTS LIST ----

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
                  items(huntsToDisplay.size, key = { huntsToDisplay[it].uid }) { index ->
                    val hunt = huntsToDisplay[index]

                    val base = Modifier.testTag(ProfileTestTags.getTestTagForHuntCard(hunt, index))

                    val clickable = base.clickable { onMyHuntClick(hunt.uid) }
                    HuntCard(
                        hunt = hunt,
                        isLiked = profile.likedHunts.any { it.uid == hunt.uid },
                        onLikeClick = { _ -> viewModel.toggleLikedHunt(hunt, context) },
                        modifier =
                            clickable.testTag(
                                "${ProfileTestTags.getTestTagForHuntCard(hunt, index)}${ProfileTestTags.LIKE_BUTTON}"))
                  }
                }
              }
        }
      }
}

/**
 * Displays the top section of the profile screen, including:
 * - Background gradient
 * - Settings or back button (depending on profile ownership)
 * - Profile picture, pseudonym, and bio
 * - Statistics cards for review rating and hunts done
 *
 * @param profile The Profile object containing all user information.
 * @param reviewCount The total number of reviews the user has received.
 * @param isMyProfile Whether the displayed profile belongs to the current user.
 * @param testPublic If true, simulates a public profile for testing purposes.
 * @param onSettings Callback invoked when the settings button is clicked.
 * @param onGoBack Callback invoked when the back button is clicked.
 * @param onReviewsClick Callback invoked when the reviews stat card is clicked.
 */
@Composable
fun ModernProfileHeader(
    profile: Profile,
    reviewCount: Int,
    isMyProfile: Boolean,
    testPublic: Boolean,
    onSettings: () -> Unit,
    onGoBack: () -> Unit,
    onReviewsClick: () -> Unit = {}
) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .background(
                  Brush.verticalGradient(
                      colors =
                          listOf(
                              MaterialTheme.colorScheme.primary,
                              MaterialTheme.colorScheme.secondary)))) {
        Column(modifier = Modifier.fillMaxWidth().padding(ProfileUIConstantsDefaults.Padding20)) {

          // TOP RIGHT BUTTON : SETTINGS or BACK
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (isMyProfile && !testPublic) {
              Surface(
                  onClick = onSettings,
                  modifier = Modifier.testTag(ProfileTestTags.SETTINGS),
                  shape = CircleShape,
                  color =
                      MaterialTheme.colorScheme.onPrimary.copy(
                          alpha = ProfileUIConstantsDefaults.ALPHA_LIGHT)) {
                    IconButton(onClick = onSettings) {
                      Icon(
                          imageVector = Icons.Default.Settings,
                          contentDescription = ProfileConstants.SETTINGS_DESCRIPTION,
                          tint = MaterialTheme.colorScheme.onPrimary,
                          modifier = Modifier.size(ProfileUIConstantsDefaults.Size24))
                    }
                  }
            } else {
              Surface(
                  onClick = onGoBack,
                  modifier = Modifier.testTag(ProfileTestTags.GO_BACK),
                  shape = CircleShape,
                  color =
                      MaterialTheme.colorScheme.onPrimary.copy(
                          alpha = ProfileUIConstantsDefaults.ALPHA_LIGHT)) {
                    IconButton(onClick = onGoBack) {
                      Icon(
                          imageVector = Icons.Default.Close,
                          contentDescription = ProfileScreenConstants.ICON_BUTTON_GOBACK,
                          tint = MaterialTheme.colorScheme.onPrimary,
                          modifier = Modifier.size(ProfileUIConstantsDefaults.Size24))
                    }
                  }
            }
          }

          Spacer(modifier = Modifier.height(ProfileUIConstantsDefaults.Padding12))

          // PROFILE PICTURE + NAME
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ProfilePicture(
                profilePictureRes = profile.author.profilePicture,
                profilePictureUrl = profile.author.profilePictureUrl,
                modifier = Modifier.size(ProfileUIConstantsDefaults.Size70).clip(CircleShape))

            Spacer(modifier = Modifier.width(ProfileUIConstantsDefaults.Padding16))

            Column(modifier = Modifier.weight(ProfileUIConstantsDefaults.WEIGHT)) {
              Text(
                  text = profile.author.pseudonym,
                  style = ProfileTypography.titleLarge,
                  color = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.testTag(ProfileTestTags.PROFILE_PSEUDONYM))

              Spacer(modifier = Modifier.height(ProfileUIConstantsDefaults.Padding4))

              Text(
                  text = profile.author.bio,
                  style = ProfileTypography.bodyMedium,
                  color =
                      MaterialTheme.colorScheme.onPrimary.copy(
                          alpha = ProfileUIConstantsDefaults.ALPHA_MEDIUM),
                  maxLines = ProfileScreenConstants.MAX_LINE,
                  modifier = Modifier.testTag(ProfileTestTags.PROFILE_BIO))
            }
          }

          Spacer(modifier = Modifier.height(ProfileUIConstantsDefaults.Padding16))

          // STATS CARDS
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ModernStatCard(
                onReviewsClick = onReviewsClick,
                icon = painterResource(R.drawable.full_star),
                value = String.format(ONE_DECIMAL_FORMAT, profile.author.reviewRate),
                label =
                    if (reviewCount == SINGLE_REVIEW)
                        String.format(SINGLE_REVIEW_LABEL, reviewCount)
                    else String.format(MULTIPLE_REVIEWS_LABEL, reviewCount),
                modifier = Modifier.weight(ProfileUIConstantsDefaults.WEIGHT),
                testTagValue = ProfileTestTags.PROFILE_REVIEW_RATING,
                testTagLabel = ProfileTestTags.PROFILE_REVIEWS_COUNT)

            Spacer(modifier = Modifier.width(ProfileUIConstantsDefaults.Padding12))

            ModernStatCard(
                icon = painterResource(R.drawable.full_sport),
                value = String.format(ONE_DECIMAL_FORMAT, profile.author.sportRate),
                label = String.format(HUNTS_DONE_LABEL, profile.doneHunts.size),
                modifier = Modifier.weight(ProfileUIConstantsDefaults.WEIGHT),
                testTagValue = ProfileTestTags.PROFILE_SPORT_RATING,
                testTagLabel = ProfileTestTags.PROFILE_HUNTS_DONE_COUNT)
          }
        }
      }
}

/**
 * Displays a single statistic card with an icon, value, and label.
 *
 * This composable is used in the profile header to show review rating and hunts done.
 *
 * @param icon The icon to display (Painter resource).
 * @param value The main numeric value to display (e.g., rating or count).
 * @param label The label describing the value (e.g., "Reviews" or "Hunts done").
 * @param onReviewsClick Optional callback when the card is clicked (used for review rating).
 * @param modifier Modifier for layout and styling.
 * @param testTagValue Test tag for the Text showing the value (for UI testing).
 * @param testTagLabel Test tag for the Text showing the label (for UI testing).
 */
@Composable
fun ModernStatCard(
    icon: Painter,
    value: String,
    label: String,
    onReviewsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    testTagValue: String,
    testTagLabel: String
) {
  Card(
      onClick = onReviewsClick,
      modifier = modifier.semantics(mergeDescendants = true) {},
      colors =
          CardDefaults.cardColors(
              containerColor =
                  MaterialTheme.colorScheme.onPrimary.copy(
                      alpha = ProfileUIConstantsDefaults.ALPHA_LIGHT)),
      shape = RoundedCornerShape(ProfileUIConstantsDefaults.Padding12)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(ProfileUIConstantsDefaults.Padding12),
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  painter = icon,
                  contentDescription = null,
                  tint = Color.Unspecified,
                  modifier = Modifier.size(ProfileUIConstantsDefaults.Size20))

              Spacer(modifier = Modifier.width(ProfileUIConstantsDefaults.Padding8))

              Column {
                Text(
                    text = "$value/${MAX_RATING}",
                    style = ProfileTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag(testTagValue))

                Text(
                    text = label,
                    style = ProfileTypography.labelSmall,
                    color =
                        MaterialTheme.colorScheme.onPrimary.copy(
                            alpha = ProfileUIConstantsDefaults.ALPHA_MEDIUM),
                    modifier = Modifier.testTag(testTagLabel))
              }
            }
      }
}

/**
 * Displays the tab bar for switching between My Hunts, Done Hunts, and Liked Hunts.
 *
 * Each tab is represented with an icon and label. Selected tab is highlighted.
 *
 * @param selectedTab The currently selected tab.
 * @param onTabSelected Callback invoked when a tab is selected, passing the chosen tab.
 */
@Composable
fun ModernCustomToolbar(selectedTab: ProfileTab, onTabSelected: (ProfileTab) -> Unit = {}) {
  val tabs =
      listOf(
          TabItem(ProfileTab.MY_HUNTS, ProfileTestTags.TAB_MY_HUNTS, Icons.Filled.Menu),
          TabItem(ProfileTab.DONE_HUNTS, ProfileTestTags.TAB_DONE_HUNTS, Icons.Filled.Check),
          TabItem(ProfileTab.LIKED_HUNTS, ProfileTestTags.TAB_LIKED_HUNTS, Icons.Filled.Favorite))

  Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.onPrimary,
      shadowElevation = ProfileUIConstantsDefaults.Padding4) {
        Row(
            modifier =
                Modifier.fillMaxWidth().padding(vertical = ProfileUIConstantsDefaults.Padding8),
            horizontalArrangement = Arrangement.SpaceEvenly) {
              val selectedColor = MaterialTheme.colorScheme.primary
              val unselectedColor =
                  MaterialTheme.colorScheme.onSurface.copy(alpha = ProfileConstants.ALPHA)
              tabs.forEach { item ->
                val isSelected = selectedTab == item.tab
                val color = if (isSelected) selectedColor else unselectedColor
                val backgroundColor =
                    if (isSelected) selectedColor.copy(alpha = ALPHA_LIGHT) else Color.Transparent
                Surface(
                    modifier =
                        Modifier.weight(ProfileUIConstantsDefaults.WEIGHT)
                            .padding(horizontal = ProfileUIConstantsDefaults.Padding8)
                            .clickable { onTabSelected(item.tab) }
                            .semantics { this.backgroundColor = color }
                            .testTag(item.testTag),
                    shape = RoundedCornerShape(ProfileUIConstantsDefaults.Padding12),
                    color = backgroundColor) {
                      Column(
                          modifier =
                              Modifier.padding(vertical = ProfileUIConstantsDefaults.Padding12),
                          horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.tab.name,
                                tint = if (isSelected) selectedColor else unselectedColor,
                                modifier = Modifier.size(ProfileUIConstantsDefaults.Size24))

                            Spacer(modifier = Modifier.height(ProfileUIConstantsDefaults.Padding4))

                            Text(
                                text =
                                    when (item.tab) {
                                      ProfileTab.MY_HUNTS ->
                                          ProfileUIConstantsDefaults.TAB_MY_HUNTS_LABEL
                                      ProfileTab.DONE_HUNTS ->
                                          ProfileUIConstantsDefaults.TAB_DONE_LABEL
                                      ProfileTab.LIKED_HUNTS ->
                                          ProfileUIConstantsDefaults.TAB_LIKED_LABEL
                                    },
                                style =
                                    if (isSelected)
                                        ProfileTypography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold)
                                    else ProfileTypography.labelSmall,
                                color = color)
                          }
                    }
              }
            }
      }
}

/**
 * Displays an empty state when there are no hunts to show in the selected tab.
 *
 * Shows a large icon representing the tab type and a message "No hunts yet".
 *
 * @param selectedTab The currently selected tab, which determines the icon to display.
 */
@Composable
fun ModernEmptyHuntsState(selectedTab: ProfileTab) {
  Box(
      modifier = Modifier.fillMaxWidth().padding(vertical = ProfileUIConstantsDefaults.Padding60),
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
              modifier = Modifier.size(ProfileUIConstantsDefaults.EmptyIconSize),
              tint = MaterialTheme.colorScheme.onSurfaceVariant)

          Spacer(modifier = Modifier.height(ProfileUIConstantsDefaults.Padding16))

          Text(
              text = ProfileConstants.NO_HUNTS_YET,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              style = ProfileTypography.bodyLarge,
              modifier = Modifier.testTag(ProfileTestTags.EMPTY_HUNTS_MESSAGE))
        }
      }
}

// --------------------------------------------------------
// LEGACY TOOLBAR (kept for compatibility if still referenced)
// --------------------------------------------------------
/**
 * Legacy toolbar for profile tabs. Kept for backward compatibility.
 *
 * Displays a row of tabs (My Hunts, Done Hunts, Liked Hunts) using icons. Highlights the selected
 * tab with a background color.
 *
 * @param selectedTab The currently selected tab.
 * @param onTabSelected Callback invoked when a tab is selected.
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
          if (selectedTab == item.tab) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.onPrimary

      Icon(
          imageVector = item.icon,
          contentDescription = item.tab.name,
          modifier =
              Modifier.background(color)
                  .padding(
                      horizontal = ProfileConstants.SIZE_ICON,
                      vertical = ProfileUIConstantsDefaults.Padding16)
                  .clickable { onTabSelected(item.tab) }
                  .semantics { backgroundColor = color }
                  .testTag(item.testTag))
    }
  }
}
