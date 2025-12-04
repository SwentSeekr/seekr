package com.swentseekr.seekr.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
import com.google.firebase.auth.FirebaseUser
import com.swentseekr.seekr.ui.auth.AuthViewModel
import com.swentseekr.seekr.ui.auth.OnboardingFlow
import com.swentseekr.seekr.ui.components.HuntCardScreen
import com.swentseekr.seekr.ui.hunt.add.AddHuntScreen
import com.swentseekr.seekr.ui.hunt.edit.EditHuntScreen
import com.swentseekr.seekr.ui.hunt.review.AddReviewScreen
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
import com.swentseekr.seekr.ui.hunt.review.ReviewImagesScreen
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import com.swentseekr.seekr.ui.map.MapScreen
import com.swentseekr.seekr.ui.overview.OverviewScreen
import com.swentseekr.seekr.ui.profile.EditProfileScreen
import com.swentseekr.seekr.ui.profile.ProfileReviewsScreen
import com.swentseekr.seekr.ui.profile.ProfileScreen
import com.swentseekr.seekr.ui.settings.SettingsScreen

// Destinations as sealed class
sealed class SeekrDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
  object Overview : SeekrDestination("overview", "Overview", Icons.AutoMirrored.Filled.List)

  object Map : SeekrDestination("map", "Map", Icons.Filled.Place)

  object Profile : SeekrDestination("profile", "Profile", Icons.Filled.Person) {
    fun createRoute(userId: String) = "profile/$userId"

    const val ARG_USER_ID = "userId"

    object Reviews :
        SeekrDestination(
            route = "profile/{${ARG_USER_ID}}/reviews",
            label = "Profile Reviews",
            icon = Icons.AutoMirrored.Filled.List) {
      fun createRoute(userId: String) = "profile/$userId/reviews"

      const val ARG_USER_ID = Profile.ARG_USER_ID
    }
  }

  object HuntCard : SeekrDestination("hunt/{huntId}", "Hunt", Icons.Filled.List) {
    fun createRoute(huntId: String) = "hunt/$huntId"

    const val ARG_HUNT_ID = "huntId"
  }

  object EditHunt : SeekrDestination("edit_hunt/{huntId}", "Edit Hunt", Icons.Filled.List) {
    fun createRoute(huntId: String) = "edit_hunt/$huntId"

    const val ARG_HUNT_ID = "huntId"
  }

  object AddReview : SeekrDestination("add_review/{huntId}", "Add Review", Icons.Filled.List) {
    fun createRoute(huntId: String) = "add_review/$huntId"

    const val ARG_HUNT_ID = "huntId"
  }

  object AddHunt : SeekrDestination("add_hunt", "Add Hunt", Icons.Filled.List)

  object Settings : SeekrDestination("settings", "Settings", Icons.Filled.List)

  object EditProfile : SeekrDestination("edit_profile", "Edit Profile", Icons.Filled.List)

  companion object {
    val all = listOf(Overview, Map, Profile)
  }
}

// Bottom Navigation Bar
@Composable
fun SeekrNavigationBar(
    currentDestination: SeekrDestination,
    onTabSelected: (SeekrDestination) -> Unit
) {
  val containerColor = MaterialTheme.colorScheme.onPrimary
  val iconColor = MaterialTheme.colorScheme.primary

  NavigationBar(
      containerColor = containerColor,
      modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)) {
        SeekrDestination.all.forEach { dest ->
          val testTag =
              when (dest) {
                is SeekrDestination.Overview -> NavigationTestTags.OVERVIEW_TAB
                is SeekrDestination.Map -> NavigationTestTags.MAP_TAB
                is SeekrDestination.Profile -> NavigationTestTags.PROFILE_TAB
                else -> SeekrNavigationDefaults.IgnoredTestTag
              }

          val isSelected = currentDestination.route == dest.route

          NavigationBarItem(
              selected = isSelected,
              onClick = { onTabSelected(dest) },
              icon = {
                if (isSelected) {
                  Box(
                      contentAlignment = Alignment.Center,
                      modifier = Modifier.size(BottomNavUIConstants.IconContainerSize)) {
                        Box(
                            modifier =
                                Modifier.size(BottomNavUIConstants.IconHaloSize)
                                    .background(
                                        color =
                                            iconColor.copy(alpha = BottomNavUIConstants.HaloAlpha),
                                        shape = BottomNavUIConstants.HaloShape))
                        Icon(
                            dest.icon,
                            contentDescription = dest.label,
                            tint = iconColor,
                            modifier = Modifier.size(BottomNavUIConstants.IconSizeSelected))
                      }
                } else {
                  Icon(
                      dest.icon,
                      contentDescription = dest.label,
                      tint = iconColor,
                      modifier = Modifier.size(BottomNavUIConstants.IconSizeUnselected))
                }
              },
              label = { Text(dest.label, color = iconColor) },
              modifier = Modifier.testTag(testTag),
              colors =
                  NavigationBarItemDefaults.colors(
                      selectedIconColor = iconColor,
                      unselectedIconColor = iconColor,
                      selectedTextColor = iconColor,
                      unselectedTextColor = iconColor,
                      indicatorColor = BottomNavUIConstants.IndicatorColorTransparent))
        }
      }
}

// Main App Scaffold
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekrMainNavHost(
    user: FirebaseUser? = null,
    navController: NavHostController = rememberNavController(),
    huntCardViewModelFactory: (() -> HuntCardViewModel)? = null,
    reviewViewModelFactory: (() -> ReviewHuntViewModel)? = null,
    testMode: Boolean = false
) {

  fun NavHostController.goToProfileReviews(userId: String?) {
    if (!userId.isNullOrBlank()) {
      navigate(SeekrDestination.Profile.Reviews.createRoute(userId))
    }
  }

  // Onboarding check
  val authViewModel: AuthViewModel = viewModel()
  val uiState by authViewModel.uiState.collectAsState()

  if (uiState.needsOnboarding && user != null) {
    OnboardingFlow(userId = user.uid, onboardingHandler = authViewModel)
  }

  var lastHuntId by rememberSaveable { mutableStateOf<String?>(null) }
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
            modifier = Modifier.padding(innerPadding)) {

              // Overview
              composable(SeekrDestination.Overview.route) {
                Surface(
                    modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.OVERVIEW_SCREEN)) {
                      OverviewScreen(
                          onHuntClick = { huntId ->
                            lastHuntId = huntId
                            navController.navigate(SeekrDestination.HuntCard.createRoute(huntId)) {
                              launchSingleTop = true
                            }
                          })
                    }
              }

              // Map
              composable(SeekrDestination.Map.route) {
                Surface(modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.MAP_SCREEN)) {
                  MapScreen()
                }
              }

              // Profile
              composable(SeekrDestination.Profile.route) {
                ProfileScreen(
                    userId = user?.uid,
                    onAddHunt = { navController.navigate(SeekrDestination.AddHunt.route) },
                    onMyHuntClick = { huntId ->
                      navController.navigate(SeekrDestination.EditHunt.createRoute(huntId)) {
                        launchSingleTop = true
                      }
                    },
                    onSettings = { navController.navigate(SeekrDestination.Settings.route) },
                    testMode = testMode,
                    onReviewsClick = { navController.goToProfileReviews(user?.uid) })
              }
              // Public profile
              composable(
                  route = "profile/{${SeekrDestination.Profile.ARG_USER_ID}}",
                  arguments =
                      listOf(
                          navArgument(SeekrDestination.Profile.ARG_USER_ID) {
                            type = NavType.StringType
                          })) { backStackEntry ->
                    val userId =
                        backStackEntry.arguments?.getString(SeekrDestination.Profile.ARG_USER_ID)

                    ProfileScreen(
                        userId = userId,
                        onAddHunt = {},
                        onMyHuntClick = { huntId ->
                          lastHuntId = huntId
                          navController.navigate(SeekrDestination.HuntCard.createRoute(huntId)) {
                            launchSingleTop = true
                          }
                        },
                        onSettings = {},
                        onGoBack = { navController.popBackStack() },
                        testMode = testMode,
                        onReviewsClick = { navController.goToProfileReviews(userId) })
                  }

              // Hunt card (details)
              composable(
                  route = SeekrDestination.HuntCard.route,
                  arguments =
                      listOf(
                          navArgument(SeekrDestination.HuntCard.ARG_HUNT_ID) {
                            type = NavType.StringType
                          })) { backStackEntry ->
                    val argId =
                        backStackEntry.arguments?.getString(SeekrDestination.HuntCard.ARG_HUNT_ID)
                    val huntId = argId ?: lastHuntId.orEmpty()
                    val huntCardVm =
                        huntCardViewModelFactory?.invoke() ?: viewModel<HuntCardViewModel>()

                    val reviewVm =
                        reviewViewModelFactory?.invoke() ?: viewModel<ReviewHuntViewModel>()

                    HuntCardScreen(
                        huntId = huntId,
                        onGoBack = { navController.popBackStack() },
                        goProfile = { clickedUserId ->
                          navController.navigate(
                              SeekrDestination.Profile.createRoute(clickedUserId)) {
                                launchSingleTop = true
                              }
                        },
                        beginHunt = { /* wire if needed */},
                        addReview = {
                          navController.navigate(SeekrDestination.AddReview.createRoute(huntId)) {
                            launchSingleTop = true
                          }
                        },
                        huntCardViewModel = huntCardVm,
                        reviewViewModel = reviewVm,
                        modifier = Modifier.testTag(NavigationTestTags.HUNTCARD_SCREEN),
                        navController = navController)
                  }

              // Add Hunt
              composable(SeekrDestination.AddHunt.route) {
                Surface(
                    modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.ADD_HUNT_SCREEN)) {
                      AddHuntScreen(
                          onGoBack = { navController.popBackStack() },
                          onDone = {
                            navController.navigate(SeekrDestination.Overview.route) {
                              launchSingleTop = true
                              popUpTo(SeekrDestination.Overview.route)
                            }
                          },
                          testMode = testMode)
                    }
              }

              // Edit Hunt
              composable(
                  route = SeekrDestination.EditHunt.route,
                  arguments =
                      listOf(
                          navArgument(SeekrDestination.EditHunt.ARG_HUNT_ID) {
                            type = NavType.StringType
                          })) { backStackEntry ->
                    val huntId =
                        backStackEntry.arguments
                            ?.getString(SeekrDestination.EditHunt.ARG_HUNT_ID)
                            .orEmpty()

                    Surface(
                        modifier =
                            Modifier.fillMaxSize().testTag(NavigationTestTags.EDIT_HUNT_SCREEN)) {
                          EditHuntScreen(
                              huntId = huntId,
                              onGoBack = { navController.popBackStack() },
                              onDone = {
                                navController.navigate(SeekrDestination.Profile.route) {
                                  launchSingleTop = true
                                  popUpTo(SeekrDestination.Profile.route)
                                }
                              },
                              testMode = testMode)
                        }
                  }

              // Add Review (new)
              composable(
                  route = SeekrDestination.AddReview.route,
                  arguments =
                      listOf(
                          navArgument(SeekrDestination.AddReview.ARG_HUNT_ID) {
                            type = NavType.StringType
                          })) { backStackEntry ->
                    val huntId =
                        backStackEntry.arguments
                            ?.getString(SeekrDestination.AddReview.ARG_HUNT_ID)
                            .orEmpty()
                    Surface(
                        modifier =
                            Modifier.fillMaxSize().testTag(NavigationTestTags.REVIEW_HUNT_SCREEN)) {
                          AddReviewScreen(
                              huntId = huntId,
                              onGoBack = { navController.popBackStack() },
                              onCancel = { navController.popBackStack() },
                              onDone = { navController.popBackStack() })
                        }
                  }

              // Settings
              composable(SeekrDestination.Settings.route) {
                Surface(
                    modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.SETTINGS_SCREEN)) {
                      SettingsScreen(
                          onGoBack = { navController.popBackStack() },
                          onEditProfile = {
                            navController.navigate(SeekrDestination.EditProfile.route) {
                              launchSingleTop = true
                            }
                          })
                    }
              }

              // Review Images Screen
              composable(
                  route = "reviewImages/{reviewId}",
                  arguments = listOf(navArgument("reviewId") { type = NavType.StringType })) {
                      backStackEntry ->
                    val reviewId = backStackEntry.arguments?.getString("reviewId").orEmpty()

                    val reviewHuntViewModel: ReviewHuntViewModel = viewModel()
                    LaunchedEffect(reviewId) { reviewHuntViewModel.loadReview(reviewId) }
                    val uiState by reviewHuntViewModel.uiState.collectAsState()

                    Surface(modifier = Modifier.fillMaxSize().testTag("IMAGE_REVIEW_SCREEN")) {
                      ReviewImagesScreen(
                          photoUrls = uiState.photos, onGoBack = { navController.popBackStack() })
                    }
                  }

              // Edit Profile (new)
              composable(SeekrDestination.EditProfile.route) {
                Surface(
                    modifier =
                        Modifier.fillMaxSize().testTag(NavigationTestTags.EDIT_PROFILE_SCREEN)) {
                      EditProfileScreen(
                          userId = user?.uid, // pass current user if available
                          onGoBack = { navController.popBackStack() },
                          onDone = { navController.popBackStack() },
                          testMode = testMode)
                    }
              }

              composable(
                  route = "profile/{userId}/reviews",
                  arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                      backStackEntry ->
                    val userId =
                        backStackEntry.arguments
                            ?.getString(SeekrDestination.Profile.Reviews.ARG_USER_ID)
                            .orEmpty()

                    ProfileReviewsScreen(
                        userId = userId,
                        onGoBack = { navController.popBackStack() },
                        navController = navController)
                  }
            }
      }
}
