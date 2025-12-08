package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.profile.ModernCustomToolbar
import com.swentseekr.seekr.ui.profile.ModernEmptyHuntsState
import com.swentseekr.seekr.ui.profile.ModernProfileHeader
import com.swentseekr.seekr.ui.profile.Profile
import com.swentseekr.seekr.ui.profile.ProfileTab

/**
 * Tabs available in the offline profile view.
 *
 * These values mirror the online [ProfileTab] entries and represent the three logical sections of
 * the profile:
 * - [MY_HUNTS] – hunts created by the user.
 * - [DONE_HUNTS] – hunts that the user has completed.
 * - [LIKED_HUNTS] – hunts that the user has liked.
 *
 * The offline UI maps these values to [ProfileTab] so that existing composables (e.g.
 * [ModernCustomToolbar], [ModernEmptyHuntsState]) can be reused without introducing separate
 * offline-only variants.
 */
enum class OfflineProfileTab {
  MY_HUNTS,
  DONE_HUNTS,
  LIKED_HUNTS
}

/**
 * Offline profile screen based on locally cached profile data.
 *
 * This composable:
 * - Displays a profile-style layout even when the device is offline.
 * - Reuses the same building blocks as the online profile:
 *     - [ModernProfileHeader] for the profile header.
 *     - [ModernCustomToolbar] for tab selection.
 *     - [ModernEmptyHuntsState] and [HuntCard] for the hunts section.
 * - Relies on [OfflineViewModel] to:
 *     - Hold the current [Profile] snapshot.
 *     - Track the selected [OfflineProfileTab].
 *     - Expose the filtered list of [Hunt]s to display.
 *
 * Behavior:
 * - If [profile] is `null`, a simple centered offline message is shown and no profile content is
 *   rendered. This state typically indicates that the app has no cached profile yet.
 * - If [profile] is non-null, the screen shows:
 *     - A header with basic profile information.
 *     - Tabs for "My Hunts", "Done Hunts", and "Liked Hunts".
 *     - A list of hunts for the currently selected tab or a dedicated empty state when no hunts are
 *       available.
 *
 * The screen is intended for use from [com.swentseekr.seekr.ui.navigation.SeekrOfflineNavHost] as
 * part of the offline navigation graph.
 *
 * @param profile Cached [Profile] to display in offline mode. If `null`, the screen renders a
 *   generic "no offline profile" message.
 * @param modifier Optional [Modifier] applied to the root [Surface].
 */
@Composable
fun OfflineCachedProfileScreen(profile: Profile?, modifier: Modifier = Modifier) {
  // Offline-specific view model driven by the cached profile snapshot.
  val offlineViewModel = remember(profile) { OfflineViewModel(profile) }
  val currentProfile = offlineViewModel.profile

  Surface(
      modifier = modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.onPrimary, // from theme (White)
  ) {
    // If there is no cached profile, show a centered offline message and bail out early.
    if (currentProfile == null) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = OfflineConstants.OFFLINE_NO_PROFILE,
            color = MaterialTheme.colorScheme.onSecondary, // GrayDislike via theme
            style = MaterialTheme.typography.bodyMedium)
      }
      return@Surface
    }

    val selectedOfflineTab = offlineViewModel.selectedTab
    val huntsToDisplay: List<Hunt> = offlineViewModel.huntsToDisplay

    // Map OfflineProfileTab -> ProfileTab so we can reuse ModernCustomToolbar & empty state.
    val selectedProfileTab: ProfileTab =
        when (selectedOfflineTab) {
          OfflineProfileTab.MY_HUNTS -> ProfileTab.MY_HUNTS
          OfflineProfileTab.DONE_HUNTS -> ProfileTab.DONE_HUNTS
          OfflineProfileTab.LIKED_HUNTS -> ProfileTab.LIKED_HUNTS
        }

    Column(modifier = Modifier.fillMaxSize()) {

      // 1. HEADER – reuse ModernProfileHeader to keep visual parity with online profile.
      ModernProfileHeader(
          profile = currentProfile,
          reviewCount = 0,
          isMyProfile = false,
          testPublic = true,
          onSettings = {},
          onGoBack = {},
          onReviewsClick = {})

      // 2. TABS – reuse ModernCustomToolbar, mapping back to OfflineProfileTab internally.
      ModernCustomToolbar(
          selectedTab = selectedProfileTab,
          onTabSelected = { newTab ->
            val mappedOfflineTab =
                when (newTab) {
                  ProfileTab.MY_HUNTS -> OfflineProfileTab.MY_HUNTS
                  ProfileTab.DONE_HUNTS -> OfflineProfileTab.DONE_HUNTS
                  ProfileTab.LIKED_HUNTS -> OfflineProfileTab.LIKED_HUNTS
                }
            offlineViewModel.selectTab(mappedOfflineTab)
          })

      // 3. HUNTS LIST / EMPTY STATE – reuse ModernEmptyHuntsState + HuntCard for consistency.
      LazyColumn(
          modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (huntsToDisplay.isEmpty()) {
              item { ModernEmptyHuntsState(selectedProfileTab) }
            } else {
              itemsIndexed(huntsToDisplay) { _, hunt ->
                HuntCard(
                    hunt = hunt,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(
                                vertical = OfflineConstants.PROFILE_SCREEN_VERTICAL_PADDING,
                                horizontal = OfflineConstants.PROFILE_SCREEN_HORIZONTAL_PADDING))
              }
            }
          }
    }
  }
}
