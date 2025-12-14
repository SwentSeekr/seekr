package com.swentseekr.seekr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.swentseekr.seekr.ui.navigation.SeekrRootApp
import com.swentseekr.seekr.ui.theme.SampleAppTheme

/** Main entry point for the Seekr app. */
class MainActivity : ComponentActivity() {
  /**
   * Called when the activity is first created.
   *
   * @param savedInstanceState If the activity is being re-initialized after previously being shut
   *   down, this Bundle contains the data it most recently supplied. Otherwise, it is null.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { SampleAppTheme { SeekrRootApp() } }
  }
}
