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

/**
 * Root entry point for the Seekr application UI.
 *
 * This composable is responsible for:
 * - Observing the authentication state to decide between the authenticated main flow and the
 *   authentication flow.
 * - Observing internet connectivity to decide between online and offline experiences.
 * - Observing a locally cached [Profile] to enable an offline-first experience when possible.
 * - Routing to the appropriate navigation host:
 *     - [SeekrOfflineNavHost] when the user is offline and a cached profile is available.
 *     - [OfflineRequiredScreen] when the user is offline and no cached profile exists.
 *     - [SeekrMainNavHost] when the user is online and authenticated.
 *     - [AuthNavHost] when the user is online but not authenticated.
 *
 * @param context The [Context] used for acquiring system services such as [CredentialManager] and
 *   for starting system settings activities. Defaults to [LocalContext.current].
 * @param authViewModel The [AuthViewModel] that exposes the authentication UI state. Defaults to
 *   [viewModel] scoped to this composable.
 */
@Composable
fun SeekrRootApp(
    context: Context = LocalContext.current,
    authViewModel: AuthViewModel = viewModel()
) {
  // Credential manager used by the authentication flow (e.g., Sign in with Google, etc.).
  val credentialManager = remember { CredentialManager.create(context) }

  // Observe authentication state (user signed in or not).
  val state by authViewModel.uiState.collectAsState()

  // Observe internet connectivity state for online/offline routing.
  val internetConnectivityObserver = remember { InternetConnectivityObserver(context) }
  val isOnline by internetConnectivityObserver.connectionState.collectAsState()

  /**
   * Start and stop the connectivity observer with the lifecycle of this composable.
   *
   * When the composable enters the composition, we start listening for connectivity changes. When
   * it leaves, we stop to avoid leaks and unnecessary work.
   */
  DisposableEffect(internetConnectivityObserver) {
    internetConnectivityObserver.start()
    onDispose { internetConnectivityObserver.stop() }
  }

  /**
   * Opens the system wireless/network settings screen.
   *
   * Used from the offline-required UI to allow the user to restore connectivity.
   */
  val openSettings: () -> Unit = {
    val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
  }

  // Observe cached profile for offline mode. When available, this enables
  // an offline-first experience (e.g., showing profile and hunts without network).
  val cachedProfile by ProfileCache.observeProfile(context).collectAsState(initial = null)

  /**
   * Build a list of "stored hunts" for offline overview.
   *
   * Currently, "stored hunts" are derived from:
   * - [Profile.myHunts]
   * - [Profile.doneHunts]
   * - [Profile.likedHunts]
   *
   * This can later be replaced by a dedicated offline hunts cache. Duplicate hunts are filtered out
   * by [Hunt.uid].
   */
  val offlineHunts: List<Hunt> =
      remember(cachedProfile) {
        val profile: Profile? = cachedProfile
        if (profile == null) {
          emptyList()
        } else {
          (profile.myHunts + profile.doneHunts + profile.likedHunts).distinctBy {
            it.uid
          } // Avoid duplicates based on unique hunt ID.
        }
      }

  /**
   * High-level UI scaffold for the root application shell.
   *
   * The inner content is switched based on:
   * - Connectivity state: online vs offline.
   * - Availability of cached profile data for offline usage.
   * - Authentication state of the current user.
   */
  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    when {
      // OFFLINE MODE WITH CACHED DATA → offline navigation (overview/map/profile, etc.).
      !isOnline && cachedProfile != null -> {
        SeekrOfflineNavHost(
            cachedProfile = cachedProfile,
            offlineHunts = offlineHunts,
            navController = rememberNavController(),
        )
      }

      // OFFLINE MODE WITHOUT ANY CACHED PROFILE → show an explanatory "offline required" screen.
      !isOnline ->
          OfflineRequiredScreen(
              modifier = Modifier.padding(innerPadding),
              onOpenSettings = openSettings,
          )

      // ONLINE & AUTHENTICATED → main navigation host (full app experience).
      state.user != null -> SeekrMainNavHost(state.user)

      // ONLINE & NOT AUTHENTICATED → authentication flow (login/sign-up).
      else ->
          AuthNavHost(
              credentialManager = credentialManager,
              viewModel = authViewModel,
              onSignedIn = {
                // No-op: AuthViewModel observes FirebaseAuth changes automatically.
              },
              modifier = Modifier.padding(innerPadding),
          )
    }
  }
}
