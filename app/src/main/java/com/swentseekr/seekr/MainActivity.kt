package com.swentseekr.seekr

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.swentseekr.seekr.ui.navigation.SeekrRootApp
import com.swentseekr.seekr.ui.theme.SampleAppTheme

class MainActivity : ComponentActivity() {
  private val requestNotificationPermission =
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // Optional: show a toast if permission denied
      }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    setContent { SampleAppTheme { SeekrRootApp() } }
  }
}
