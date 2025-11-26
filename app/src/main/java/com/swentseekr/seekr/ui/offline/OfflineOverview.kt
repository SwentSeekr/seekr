package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.profile.mockProfileData
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.profile.Profile

@Composable
fun OfflineOverviewScreen(profile: Profile?, modifier: Modifier = Modifier) {
  var showHunts by remember { mutableStateOf(false) }

  // Only liked hunts offline
  val downloadedHunts: List<Hunt> = profile?.likedHunts ?: emptyList()

  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = OfflineConstants.SCREEN_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom) {
          Spacer(modifier = Modifier.height(64.dp))

          // Light green card
          Box(
              modifier =
                  Modifier.fillMaxWidth(OfflineConstants.OFFLINE_CARD_WIDTH_RATIO)
                      .height(OfflineConstants.OFFLINE_CARD_HEIGHT)
                      .background(
                          color = OfflineConstants.LIGHT_GREEN_BACKGROUND,
                          shape = OfflineConstants.CARD_SHAPE),
              contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                      Icon(
                          imageVector = Icons.Default.Warning,
                          contentDescription = null,
                          modifier = Modifier.size(OfflineConstants.OFFLINE_ICON_SIZE))
                      Spacer(modifier = Modifier.height(OfflineConstants.ICON_SPACING))
                      Text(
                          text = OfflineConstants.OFFLINE_OVERVIEW_MESSAGE,
                          style = MaterialTheme.typography.titleMedium)
                    }
              }

          // Bigger gap to push the button further down
          Spacer(modifier = Modifier.height(100.dp))

          Button(
              onClick = { showHunts = true },
              shape = RoundedCornerShape(50),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60BA37)),
              modifier = Modifier.fillMaxWidth(0.7f)) {
                Text(text = OfflineConstants.SHOW_DOWNLOADED_HUNTS_BUTTON)
              }
          Spacer(modifier = Modifier.height(50.dp))

          // Downloaded hunts list below, once user continues
          if (showHunts && downloadedHunts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  items(downloadedHunts) { hunt ->
                    HuntCard(hunt = hunt, modifier = Modifier.fillMaxWidth())
                  }
                }
          } else if (showHunts && downloadedHunts.isEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = OfflineProfileConstants.NO_HUNTS_YET,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
          }
        }
  }
}

@Preview
@Composable
fun OfflineOverviewScreenPreview() {
  OfflineOverviewScreen(profile = mockProfileData())
}
