package com.swentseekr.seekr.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.components.HuntCardScreen
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
import com.swentseekr.seekr.ui.hunt.review.ReviewImagesScreen
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import com.swentseekr.seekr.ui.offline.OfflineCachedProfileScreen
import com.swentseekr.seekr.ui.offline.OfflineMapScreen
import com.swentseekr.seekr.ui.offline.OfflineOverviewHuntsScreen
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.ui.profile.Profile
import com.swentseekr.seekr.ui.theme.White

/**
 * Navigation host for the offline Seekr experience.
 *
 * This composable provides a dedicated navigation graph for offline usage, leveraging:
 * - A cached [Profile], when available, to render the profile tab.
 * - A list of locally available [Hunt]s for overview and details.
 * - The same high-level navigation structure as the online app (overview, map, profile, hunt card),
 *   but backed by offline data sources or limited functionality where network access is required.
 *
 * The offline navigation graph includes:
 * - Overview: [OfflineOverviewHuntsScreen] showing locally available hunts.
 * - Map: [OfflineMapScreen] for a map-based offline view (e.g., using stored locations).
 * - Profile: [OfflineCachedProfileScreen] using the cached [Profile].
 * - Hunt Card: [HuntCardScreen] re-used for offline hunt details.
 * - Review Images: [ReviewImagesScreen] for viewing stored review photos (if available offline).
 *
 * This host is typically used from [SeekrRootApp] when:
 * - The device is offline, and
 * - A cached profile is present.
 *
 * @param cachedProfile Cached user [Profile] used to render the profile tab in offline mode. Can be
 *   `null`, in which case the profile tab displays an appropriate offline state.
 * @param offlineHunts List of [Hunt] entities available offline, used for overview and hunt
 *   details.
 * @param navController Optional [NavHostController] that drives navigation within the offline
 *   graph. Defaults to a fresh [rememberNavController] instance.
 */
@Composable
fun SeekrOfflineNavHost(
    cachedProfile: Profile?,
    offlineHunts: List<Hunt>,
    navController: NavHostController = rememberNavController()
) {
  // Track current destination (same pattern as SeekrMainNavHost).
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val currentDestination =
      SeekrDestination.all.find { it.route == currentRoute } ?: SeekrDestination.Overview

  // Show the bottom bar for destinations that are part of the main tab navigation.
  val showBottomBar = SeekrDestination.all.any { it.route == currentRoute }

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.surfaceVariant)) {

              // OFFLINE OVERVIEW → stored hunts, clone of original overview UI, but using
              // offlineHunts.
              composable(SeekrDestination.Overview.route) {
                Surface(
                    modifier =
                        Modifier.fillMaxSize().testTag(OverviewScreenTestTags.OVERVIEW_SCREEN)) {
                      OfflineOverviewHuntsScreen(
                          hunts = offlineHunts,
                          onHuntClick = { huntId ->
                            navController.navigate(SeekrDestination.HuntCard.createRoute(huntId)) {
                              launchSingleTop = true
                            }
                          })
                    }
              }

              // OFFLINE MAP → map view suitable for offline context (e.g., cached tiles or
              // locations).
              composable(SeekrDestination.Map.route) {
                Surface(modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.MAP_SCREEN)) {
                  OfflineMapScreen()
                }
              }

              // OFFLINE PROFILE → uses cached profile data when available.
              composable(SeekrDestination.Profile.route) {
                Surface(modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.PROFILE_TAB)) {
                  OfflineCachedProfileScreen(profile = cachedProfile)
                }
              }

              // OFFLINE HUNT CARD → reuse HuntCardScreen with offline-compatible behaviors.
              composable(
                  route = SeekrDestination.HuntCard.route,
                  arguments =
                      listOf(
                          navArgument(SeekrDestination.HuntCard.ARG_HUNT_ID) {
                            type = NavType.StringType
                          })) { backStackEntry ->
                    val huntId =
                        backStackEntry.arguments
                            ?.getString(SeekrDestination.HuntCard.ARG_HUNT_ID)
                            .orEmpty()

                    // ViewModels are resolved in this back stack entry's scope.
                    val huntCardVm: HuntCardViewModel = viewModel()
                    val reviewVm: ReviewHuntViewModel = viewModel()

                    HuntCardScreen(
                        huntId = huntId,
                        onGoBack = { navController.popBackStack() },
                        goProfile = {
                          // In offline mode this could be a no-op, or later route to a cached
                          // public profile.
                        },
                        beginHunt = {
                          // Optionally wire an offline "start hunt" flow if/when supported.
                        },
                        addReview = {
                          // Typically disabled offline; reviews may require network.
                        },
                        editHunt = {
                          // Typically disabled offline; editing may require network / sync.
                        },
                        huntCardViewModel = huntCardVm,
                        reviewViewModel = reviewVm,
                        modifier = Modifier.testTag(NavigationTestTags.HUNTCARD_SCREEN),
                        navController = navController)
                  }

              // REVIEW IMAGES → displays review photos (if previously cached) in offline mode.
              composable(
                  route = SeekrNavigationDefaults.REVIEW_IMAGES_ROUTE,
                  arguments =
                      listOf(
                          navArgument(SeekrNavigationDefaults.REVIEW_IMAGES_REVIEW_ID_ARG) {
                            type = NavType.StringType
                          })) { backStackEntry ->
                    val reviewId =
                        backStackEntry.arguments
                            ?.getString(SeekrNavigationDefaults.REVIEW_IMAGES_REVIEW_ID_ARG)
                            .orEmpty()

                    val reviewHuntViewModel: ReviewHuntViewModel = viewModel()

                    // In the online app this usually triggers a network load; offline this will
                    // only
                    // succeed if data is already cached by the ViewModel / repository.
                    androidx.compose.runtime.LaunchedEffect(reviewId) {
                      reviewHuntViewModel.loadReview(reviewId)
                    }
                    val uiState by reviewHuntViewModel.uiState.collectAsState()

                    Surface(
                        modifier =
                            Modifier.fillMaxSize()
                                .testTag(NavigationTestTags.IMAGE_REVIEW_SCREEN)) {
                          ReviewImagesScreen(
                              photoUrls = uiState.photos,
                              onGoBack = { navController.popBackStack() })
                        }
                  }
            }
      }
}
