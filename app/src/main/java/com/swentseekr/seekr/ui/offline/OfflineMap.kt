package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

/** Map placeholder screen displayed when the user is offline. */
@Composable
fun OfflineMapScreen(modifier: Modifier = Modifier) {
  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Box(
          modifier =
              Modifier.fillMaxWidth(OfflineConstants.OFFLINE_CARD_WIDTH_RATIO)
                  .height(OfflineConstants.OFFLINE_CARD_HEIGHT)
                  .background(
                      color = OfflineConstants.LIGHT_GREEN_BACKGROUND,
                      shape = OfflineConstants.CARD_SHAPE),
          contentAlignment = Alignment.Center) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                  Icon(
                      imageVector = Icons.Default.Warning,
                      contentDescription = OfflineConstants.MAP_ICON,
                      modifier = Modifier.size(OfflineConstants.OFFLINE_ICON_SIZE),
                      tint = MaterialTheme.colorScheme.onBackground // black from theme
                      )
                  Spacer(modifier = Modifier.height(OfflineConstants.ICON_SPACING))
                  Text(
                      text = OfflineConstants.OFFLINE_MAP_MESSAGE,
                      style = MaterialTheme.typography.titleMedium,
                      modifier = Modifier.fillMaxWidth(),
                      textAlign = TextAlign.Center)
                }
          }
    }
  }
}

/** Design-time preview of [OfflineMapScreen]. */
@Preview
@Composable
fun OfflineMapScreenPreview() {
  OfflineMapScreen()
}
