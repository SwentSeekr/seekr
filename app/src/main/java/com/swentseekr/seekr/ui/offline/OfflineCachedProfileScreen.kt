package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.mockProfileData
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.components.MAX_RATING
import com.swentseekr.seekr.ui.components.Rating
import com.swentseekr.seekr.ui.components.RatingType
import com.swentseekr.seekr.ui.profile.Profile
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

  Surface(modifier = modifier.fillMaxSize()) {
    if (currentProfile == null) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = OfflineProfileConstants.OFFLINE_NO_PROFILE,
            color = GrayDislike,
            style = MaterialTheme.typography.bodyMedium)
      }
      return@Surface
    }

    val selectedTab = offlineViewModel.selectedTab
    val doneHuntsCount = offlineViewModel.doneHuntsCount
    val reviewRate = offlineViewModel.reviewRate
    val sportRate = offlineViewModel.sportRate
    val huntsToDisplay: List<Hunt> = offlineViewModel.huntsToDisplay

    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(
                    horizontal = OfflineProfileConstants.SCREEN_HORIZONTAL_PADDING,
                    vertical = OfflineProfileConstants.SCREEN_VERTICAL_PADDING)) {

          // Header / author section
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(vertical = OfflineProfileConstants.MEDIUM_PADDING),
              verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(OfflineProfileConstants.COLUMN_WEIGHT)) {
                  Text(
                      text = currentProfile.author.pseudonym,
                      fontSize = OfflineProfileConstants.TEXT_SIZE_PSEUDONYM,
                      fontWeight = FontWeight.Bold)

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${reviewRate}/${MAX_RATING}",
                        modifier = Modifier.padding(OfflineProfileConstants.SMALL_PADDING))
                    Rating(rating = reviewRate, type = RatingType.STAR)
                    Text(
                        text = " - ${OfflineProfileConstants.REVIEWS_SUFFIX.trimStart()}",
                        fontSize = OfflineProfileConstants.TEXT_SIZE_SECONDARY,
                        color = GrayDislike)
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${sportRate}/${MAX_RATING}",
                        modifier = Modifier.padding(OfflineProfileConstants.SMALL_PADDING))
                    Rating(rating = sportRate, type = RatingType.SPORT)
                    Text(
                        text = " - $doneHuntsCount${OfflineProfileConstants.HUNTS_DONE_SUFFIX}",
                        fontSize = OfflineProfileConstants.TEXT_SIZE_SECONDARY,
                        color = GrayDislike)
                  }
                }
              }

          if (currentProfile.author.bio.isNotBlank()) {
            Text(
                text = currentProfile.author.bio,
                fontSize = OfflineProfileConstants.TEXT_SIZE_BODY,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(vertical = OfflineProfileConstants.SMALL_PADDING))
          }

          Spacer(modifier = Modifier.height(OfflineProfileConstants.SECTION_TOP_PADDING))

          OfflineProfileTabs(
              selectedTab = selectedTab, onTabSelected = { offlineViewModel.selectTab(it) })

          Spacer(modifier = Modifier.height(OfflineProfileConstants.SMALL_PADDING))

          if (huntsToDisplay.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
              Text(
                  text = OfflineProfileConstants.NO_HUNTS_YET,
                  color = GrayDislike,
                  fontSize = OfflineProfileConstants.TEXT_SIZE_BODY)
            }
          } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                  itemsIndexed(huntsToDisplay) { _, hunt ->
                    HuntCard(
                        hunt = hunt,
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(
                                    vertical = OfflineProfileConstants.SCREEN_VERTICAL_PADDING))
                  }
                }
          }
        }
  }
}

@Composable
private fun OfflineProfileTabs(
    selectedTab: OfflineProfileTab,
    onTabSelected: (OfflineProfileTab) -> Unit
) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    val tabs =
        listOf(
            Triple(
                OfflineProfileTab.MY_HUNTS,
                OfflineProfileConstants.TAB_MY_HUNTS,
                Icons.Filled.Menu),
            Triple(
                OfflineProfileTab.DONE_HUNTS,
                OfflineProfileConstants.TAB_DONE_HUNTS,
                Icons.Filled.Check),
            Triple(
                OfflineProfileTab.LIKED_HUNTS,
                OfflineProfileConstants.TAB_LIKED_HUNTS,
                Icons.Filled.Favorite))

    tabs.forEach { (tab, description, icon) ->
      val bgColor =
          if (selectedTab == tab) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.onPrimary
      Icon(
          imageVector = icon,
          contentDescription = description,
          modifier =
              Modifier.background(bgColor)
                  .padding(
                      horizontal = OfflineProfileConstants.ICON_HORIZONTAL_PADDING,
                      vertical = OfflineProfileConstants.ICON_VERTICAL_PADDING)
                  .clickable { onTabSelected(tab) })
    }
  }
}

@Preview
@Composable
fun OfflineCachedProfileScreenPreview() {
  OfflineCachedProfileScreen(profile = mockProfileData())
}
