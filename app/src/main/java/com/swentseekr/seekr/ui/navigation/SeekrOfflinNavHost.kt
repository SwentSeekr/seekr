package com.swentseekr.seekr.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.offline.OfflineCachedProfileScreen
import com.swentseekr.seekr.ui.offline.OfflineMapScreen
import com.swentseekr.seekr.ui.offline.OfflineOverviewHuntsScreen
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.ui.profile.Profile
import com.swentseekr.seekr.ui.theme.White

/**
 * Offline navigation host.
 *
 * Reuses:
 * - SeekrDestination for routes & bottom bar labels/icons
 * - SeekrNavigationBar for the bottom navigation UI
 *
 * But displays:
 * - OfflineOverviewHuntsScreen for Overview
 * - OfflineMapScreen for Map
 * - OfflineCachedProfileScreen for Profile
 */
@Composable
fun SeekrOfflineNavHost(
    cachedProfile: Profile?,
    offlineHunts: List<Hunt>,
    navController: NavHostController = rememberNavController()
) {
  // Track current destination (same pattern as SeekrMainNavHost)
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val currentDestination =
      SeekrDestination.all.find { it.route == currentRoute } ?: SeekrDestination.Overview
  val showBottomBar = SeekrDestination.all.any { it.route == currentRoute }

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = SeekrNavigationDefaults.ScaffoldContainerColor,
      bottomBar = {
        if (showBottomBar) {
          SeekrNavigationBar(
              currentDestination = currentDestination,
              onTabSelected = { destination ->
                navController.navigate(destination.route) {
                  launchSingleTop = true
                  popUpTo(SeekrDestination.Overview.route)
                }
              })
        }
      }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SeekrDestination.Overview.route,
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(White)) {

              // OFFLINE OVERVIEW = stored hunts, clone of original overview UI
              composable(SeekrDestination.Overview.route) {
                Surface(
                    modifier =
                        Modifier.fillMaxSize().testTag(OverviewScreenTestTags.OVERVIEW_SCREEN)) {
                      OfflineOverviewHuntsScreen(
                          hunts = offlineHunts,
                          onHuntClick = { /* You can later wire to an offline detail screen */})
                    }
              }

              // OFFLINE MAP
              composable(SeekrDestination.Map.route) {
                Surface(modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.MAP_SCREEN)) {
                  OfflineMapScreen()
                }
              }

              // OFFLINE PROFILE (cached profile)
              composable(SeekrDestination.Profile.route) {
                Surface(
                    modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.PROFILE_TAB),
                ) {
                  OfflineCachedProfileScreen(profile = cachedProfile)
                }
              }
            }
      }
}
