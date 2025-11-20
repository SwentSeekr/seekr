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
import com.swentseekr.seekr.offline.cache.ProfileCache
import com.swentseekr.seekr.offline.connectivity.InternetConnectivityObserver
import com.swentseekr.seekr.ui.auth.AuthViewModel
import com.swentseekr.seekr.ui.offline.OfflineCachedProfileScreen
import com.swentseekr.seekr.ui.offline.OfflineRequiredScreen

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

  val cachedProfile by ProfileCache.observeProfile(context).collectAsState(initial = null)

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    when {
      !isOnline && cachedProfile != null -> {
        OfflineCachedProfileScreen(
            profile = cachedProfile, modifier = Modifier.padding(innerPadding))
      }
      !isOnline ->
          OfflineRequiredScreen(
              modifier = Modifier.padding(innerPadding), onOpenSettings = openSettings)
      state.user != null -> SeekrMainNavHost(state.user)
      else ->
          AuthNavHost(
              credentialManager = credentialManager,
              viewModel = authViewModel,
              onSignedIn = { /* ViewModel observes FirebaseAuth automatically */},
              modifier = Modifier.padding(innerPadding))
    }
  }
}
