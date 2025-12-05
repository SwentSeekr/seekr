package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

/** Overview screen displayed when the user is offline. */
@Composable
fun OfflineOverviewScreen(
    modifier: Modifier = Modifier,
    onShowDownloadedHunts: () -> Unit = {},
) {
  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = OfflineConstants.SCREEN_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom) {
          Spacer(modifier = Modifier.height(OfflineConstants.OVERVIEW_TOP_SPACER_HEIGHT))

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
                          contentDescription = OfflineConstants.OVERVIEW_ICON,
                          modifier = Modifier.size(OfflineConstants.OFFLINE_ICON_SIZE),
                          tint = MaterialTheme.colorScheme.onBackground // black
                          )
                      Spacer(modifier = Modifier.height(OfflineConstants.ICON_SPACING))
                      Text(
                          text = OfflineConstants.OFFLINE_OVERVIEW_MESSAGE,
                          style = MaterialTheme.typography.titleMedium,
                          modifier = Modifier.fillMaxWidth(),
                          textAlign = TextAlign.Center)
                    }
              }

          Spacer(modifier = Modifier.height(OfflineConstants.OVERVIEW_BUTTON_TOP_SPACER_HEIGHT))

          Button(
              onClick = onShowDownloadedHunts,
              shape = OfflineConstants.BUTTON_SHAPE,
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = OfflineConstants.BUTTON_CONTAINER_COLOR,
                      contentColor = MaterialTheme.colorScheme.onPrimary // white text
                      ),
              modifier = Modifier.fillMaxWidth(OfflineConstants.BUTTON_WIDTH_RATIO)) {
                Text(text = OfflineConstants.SHOW_DOWNLOADED_HUNTS_BUTTON)
              }

          Spacer(modifier = Modifier.height(OfflineConstants.OVERVIEW_BUTTON_BOTTOM_SPACER_HEIGHT))
        }
  }
}

/** Design-time preview of [OfflineOverviewScreen] using mock profile data. */
@Preview
@Composable
fun OfflineOverviewScreenPreview() {
  OfflineOverviewScreen(onShowDownloadedHunts = {})
}
