package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun OfflineRequiredScreen(modifier: Modifier = Modifier, onOpenSettings: () -> Unit) {
  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
        modifier = Modifier.fillMaxSize().padding(OfflineConstants.SCREEN_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = OfflineConstants.OFFLINE_REQUIRED_ICON_DESCRIPTION,
              tint = MaterialTheme.colorScheme.onBackground)

          Spacer(modifier = Modifier.height(OfflineConstants.ICON_SPACING))

          Text(
              text = OfflineConstants.OFFLINE_TITLE, style = MaterialTheme.typography.headlineSmall)

          Spacer(modifier = Modifier.height(OfflineConstants.MESSAGE_SPACING))

          Text(text = OfflineConstants.OFFLINE_MESSAGE, style = MaterialTheme.typography.bodyMedium)

          Spacer(modifier = Modifier.height(OfflineConstants.BUTTON_SPACING))

          Button(
              onClick = onOpenSettings,
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = OfflineConstants.BUTTON_CONTAINER_COLOR,
                      contentColor = MaterialTheme.colorScheme.onPrimary)) {
                Text(text = OfflineConstants.OPEN_SETTINGS_BUTTON)
              }
        }
  }
}

@Preview
@Composable
fun OfflineRequiredScreenPreview() {
  OfflineRequiredScreen(onOpenSettings = {})
}
