package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.mockProfileData
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.profile.ModernCustomToolbar
import com.swentseekr.seekr.ui.profile.ModernEmptyHuntsState
import com.swentseekr.seekr.ui.profile.ModernProfileHeader
import com.swentseekr.seekr.ui.profile.Profile
import com.swentseekr.seekr.ui.profile.ProfileTab
import com.swentseekr.seekr.ui.theme.GrayDislike

enum class OfflineProfileTab {
  MY_HUNTS,
  DONE_HUNTS,
  LIKED_HUNTS
}

@Composable
fun OfflineCachedProfileScreen(profile: Profile?, modifier: Modifier = Modifier) {
  val offlineViewModel = remember(profile) { OfflineViewModel(profile) }
  val currentProfile = offlineViewModel.profile

  Surface(
      modifier = modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.onPrimary,
  ) {
    if (currentProfile == null) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = OfflineProfileConstants.OFFLINE_NO_PROFILE,
            color = GrayDislike,
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

      // ---------------------------------
      // 1. HEADER – reuse ModernProfileHeader
      // ---------------------------------
      // We pass:
      // - reviewCount = 0 for now (offline doesn’t seem to have a review count field)
      //   → layout is identical; label will show "- 0 reviews".
      // - isMyProfile = false, testPublic = true so top-right “close/settings” logic
      //   doesn’t depend on auth state. You can tweak this if you want a specific icon.
      ModernProfileHeader(
          profile = currentProfile,
          reviewCount = 0,
          isMyProfile = false,
          testPublic = true,
          onSettings = {},
          onGoBack = {},
          onReviewsClick = {})

      // ---------------------------------
      // 2. TABS – reuse ModernCustomToolbar
      // ---------------------------------
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

      // ---------------------------------
      // 3. HUNTS LIST / EMPTY STATE – reuse ModernEmptyHuntsState + HuntCard
      // ---------------------------------
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
                                vertical = OfflineProfileConstants.SCREEN_VERTICAL_PADDING,
                                horizontal = OfflineProfileConstants.SCREEN_HORIZONTAL_PADDING))
              }
            }
          }
    }
  }
}

@Preview
@Composable
fun OfflineCachedProfileScreenPreview() {
  OfflineCachedProfileScreen(profile = mockProfileData())
}
