package com.swentseekr.seekr.ui.navigation

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.offline.cache.ProfileCache
import com.swentseekr.seekr.offline.connectivity.InternetConnectivityObserver
import com.swentseekr.seekr.ui.auth.AuthViewModel
import com.swentseekr.seekr.ui.offline.OfflineRequiredScreen
import com.swentseekr.seekr.ui.profile.Profile

@Composable
fun SeekrRootApp(
    context: Context = LocalContext.current,
    authViewModel: AuthViewModel = viewModel()
) {
  val credentialManager = remember { CredentialManager.create(context) }
  val state by authViewModel.uiState.collectAsState()

  val internetConnectivityObserver = remember { InternetConnectivityObserver(context) }
  val isOnline by internetConnectivityObserver.connectionState.collectAsState()

  DisposableEffect(internetConnectivityObserver) {
    internetConnectivityObserver.start()
    onDispose { internetConnectivityObserver.stop() }
  }

  val openSettings: () -> Unit = {
    val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
  }

  // Observe cached profile for offline mode
  val cachedProfile by ProfileCache.observeProfile(context).collectAsState(initial = null)

  // Build a list of "stored hunts" for offline overview.
  // Here we assume "stored hunts" are the hunts associated to the cached profile.
  // You can later replace this with a dedicated offline hunts cache if you have one.
  val offlineHunts: List<Hunt> =
      remember(cachedProfile) {
        val profile: Profile? = cachedProfile
        if (profile == null) emptyList()
        else {
          (profile.myHunts + profile.doneHunts + profile.likedHunts).distinctBy {
            it.uid
          } // avoid duplicates
        }
      }

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    when {
      // OFFLINE MODE WITH CACHED DATA → go to offline nav (overview/map/profile)
      !isOnline && cachedProfile != null -> {
        SeekrOfflineNavHost(
            cachedProfile = cachedProfile,
            offlineHunts = offlineHunts,
            navController = rememberNavController(), //
        )
      }

      // OFFLINE MODE WITHOUT ANY CACHED PROFILE → show "required" screen
      !isOnline ->
          OfflineRequiredScreen(
              modifier = Modifier.padding(innerPadding), onOpenSettings = openSettings)

      // ONLINE & AUTHENTICATED → main nav
      state.user != null -> SeekrMainNavHost(state.user)

      // ONLINE & NOT AUTHENTICATED → auth flow
      else ->
          AuthNavHost(
              credentialManager = credentialManager,
              viewModel = authViewModel,
              onSignedIn = { /* ViewModel observes FirebaseAuth automatically */},
              modifier = Modifier.padding(innerPadding))
    }
  }
}
