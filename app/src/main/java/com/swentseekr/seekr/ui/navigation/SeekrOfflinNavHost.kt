package com.swentseekr.seekr.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
                          onHuntClick = { huntId ->
                            navController.navigate(SeekrDestination.HuntCard.createRoute(huntId)) {
                              launchSingleTop = true
                            }
                          })
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
                Surface(modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.PROFILE_TAB)) {
                  OfflineCachedProfileScreen(profile = cachedProfile)
                }
              }

              // OFFLINE HUNT CARD (reuse same HuntCardScreen)
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

                    val huntCardVm: HuntCardViewModel = viewModel()
                    val reviewVm: ReviewHuntViewModel = viewModel()

                    HuntCardScreen(
                        huntId = huntId,
                        onGoBack = { navController.popBackStack() },
                        goProfile = {
                          // In offline mode you could no-op or later route to a cached public
                          // profile
                        },
                        beginHunt = { /* wire offline start if needed */},
                        addReview = { /* probably disabled offline */},
                        editHunt = { /* probably disabled offline */},
                        huntCardViewModel = huntCardVm,
                        reviewViewModel = reviewVm,
                        modifier = Modifier.testTag(NavigationTestTags.HUNTCARD_SCREEN),
                        navController = navController)
                  }

              // REVIEW IMAGES (for "See Pictures" inside HuntCardScreen)
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
                    // In online app you load from network; offline this will only work if cached
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
