package com.swentseekr.seekr.ui.navigation

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.swentseekr.seekr.ui.hunt.review.EditReviewScreen
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
import com.swentseekr.seekr.ui.hunt.review.ReviewImagesScreen
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import com.swentseekr.seekr.ui.map.MapScreen
import com.swentseekr.seekr.ui.map.MapViewModel
import com.swentseekr.seekr.ui.overview.OverviewScreen
import com.swentseekr.seekr.ui.profile.EditProfileScreen
import com.swentseekr.seekr.ui.profile.ProfileReviewsScreen
import com.swentseekr.seekr.ui.profile.ProfileScreen
import com.swentseekr.seekr.ui.settings.SettingsScreen
import com.swentseekr.seekr.ui.terms.TermsAndConditionsScreen

// Destinations as sealed class
sealed class SeekrDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
  object Overview :
      SeekrDestination(
          SeekrNavigationDefaults.OVERVIEW_ROUTE,
          SeekrNavigationDefaults.OVERVIEW_LABEL,
          Icons.AutoMirrored.Filled.List)

  object Map :
      SeekrDestination(
          SeekrNavigationDefaults.MAP_ROUTE, SeekrNavigationDefaults.MAP_LABEL, Icons.Filled.Place)

  object Profile :
      SeekrDestination(
          SeekrNavigationDefaults.PROFILE_ROUTE,
          SeekrNavigationDefaults.PROFILE_LABEL,
          Icons.Filled.Person) {
    fun createRoute(userId: String) = "${SeekrNavigationDefaults.PROFILE_PATH}$userId"

    const val ARG_USER_ID = "userId"

    object Reviews :
        SeekrDestination(
            route =
                "${SeekrNavigationDefaults.PROFILE_PATH}{${ARG_USER_ID}}${SeekrNavigationDefaults.REVIEWS_PATH}",
            label = SeekrNavigationDefaults.PROFILE_REVIEWS_LABEL,
            icon = Icons.AutoMirrored.Filled.List) {
      fun createRoute(userId: String) =
          "${SeekrNavigationDefaults.PROFILE_PATH}$userId${SeekrNavigationDefaults.REVIEWS_PATH}"

      const val ARG_USER_ID = Profile.ARG_USER_ID
    }
  }

  object HuntCard :
      SeekrDestination(
          SeekrNavigationDefaults.HUNT_CARD_ROUTE,
          SeekrNavigationDefaults.HUNT_CARD_LABEL,
          Icons.Filled.List) {
    fun createRoute(huntId: String) = "${SeekrNavigationDefaults.HUNT_PATH}$huntId"

    const val ARG_HUNT_ID = "huntId"
  }

  object EditHunt :
      SeekrDestination(
          SeekrNavigationDefaults.EDIT_HUNT_ROUTE,
          SeekrNavigationDefaults.EDIT_HUNT_LABEL,
          Icons.Filled.List) {
    fun createRoute(huntId: String) = "${SeekrNavigationDefaults.EDIT_HUNT_PATH}$huntId"

    const val ARG_HUNT_ID = "huntId"
  }

  object AddReview :
      SeekrDestination(
          SeekrNavigationDefaults.ADD_REVIEW_ROUTE,
          SeekrNavigationDefaults.ADD_REVIEW_LABEL,
          Icons.Filled.List) {
    fun createRoute(huntId: String) = "${SeekrNavigationDefaults.ADD_REVIEW_PATH}$huntId"

    const val ARG_HUNT_ID = "huntId"
  }

  object EditReview :
      SeekrDestination(
          SeekrNavigationDefaults.EDIT_REVIEW_ROUTE,
          SeekrNavigationDefaults.EDIT_REVIEW_LABEL,
          Icons.Filled.List) {
    fun createRoute(huntId: String, reviewId: String) =
        "${SeekrNavigationDefaults.EDIT_REVIEW_PATH}$huntId/$reviewId"

    const val ARG_HUNT_ID = "huntId"
    const val ARG_REVIEW_ID = "reviewId"
  }

  object AddHunt :
      SeekrDestination(
          SeekrNavigationDefaults.ADD_HUNT_ROUTE,
          SeekrNavigationDefaults.ADD_HUNT_LABEL,
          Icons.Filled.List)

  object Settings :
      SeekrDestination(
          SeekrNavigationDefaults.SETTINGS_ROUTE,
          SeekrNavigationDefaults.SETTINGS_LABEL,
          Icons.Filled.List)

  object TermsConditions :
      SeekrDestination(
          SeekrNavigationDefaults.TERMS_AND_CONDITION_ROUTE,
          SeekrNavigationDefaults.TERMS_AND_CONDITION_LABEL,
          Icons.Filled.Info)

  object EditProfile :
      SeekrDestination(
          SeekrNavigationDefaults.EDIT_PROFILE_ROUTE,
          SeekrNavigationDefaults.EDIT_PROFILE_LABEL,
          Icons.Filled.List)

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
                else -> SeekrNavigationDefaults.IGNORED_TEST_TAG
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
                                            iconColor.copy(alpha = BottomNavUIConstants.HALO_ALPHA),
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
  val context = LocalContext.current
  val activity = context as? Activity

  // Read huntId if app was launched/tapped via notification
  val deepLinkHuntId = remember {
    activity?.intent?.getStringExtra(SeekrNavigationDefaults.HUNT_ID)
  }

  // Navigate exactly once when a notification tap occurs
  LaunchedEffect(deepLinkHuntId) {
    if (deepLinkHuntId != null) {
      navController.navigate(SeekrDestination.HuntCard.createRoute(deepLinkHuntId)) {
        launchSingleTop = true
      }
      activity?.intent?.removeExtra(SeekrNavigationDefaults.HUNT_ID) // prevent re-trigger
    }
  }

  fun NavHostController.goToProfileReviews(userId: String?) {
    if (!userId.isNullOrBlank()) {
      navigate(SeekrDestination.Profile.Reviews.createRoute(userId))
    }
  }

  // Onboarding check
  val authViewModel: AuthViewModel = viewModel()
  val uiState by authViewModel.uiState.collectAsState()

  // Shared MapViewModel for the whole nav graph
  val mapViewModel: MapViewModel = viewModel()

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
                  MapScreen(viewModel = mapViewModel)
                }
              }

              // Profile
              composable(SeekrDestination.Profile.route) {
                ProfileScreen(
                    userId = user?.uid,
                    onAddHunt = { navController.navigate(SeekrDestination.AddHunt.route) },
                    onMyHuntClick = { huntId ->
                      navController.navigate(SeekrDestination.HuntCard.createRoute(huntId)) {
                        launchSingleTop = true
                      }
                    },
                    onSettings = { navController.navigate(SeekrDestination.Settings.route) },
                    testMode = testMode,
                    onReviewsClick = { navController.goToProfileReviews(user?.uid) })
              }
              // Public profile
              composable(
                  route =
                      "${SeekrNavigationDefaults.PROFILE_PATH}{${SeekrDestination.Profile.ARG_USER_ID}}",
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
                        beginHunt = {

                          // 1. Select hunt in MapViewModel
                          mapViewModel.selectHuntById(huntId)
                          mapViewModel.onViewHuntClick()

                          // 2. navigate to Map
                          navController.navigate(SeekrDestination.Map.route) {
                            launchSingleTop = true
                            popUpTo(SeekrDestination.Overview.route)
                          }
                        },
                        addReview = {
                          navController.navigate(SeekrDestination.AddReview.createRoute(huntId)) {
                            launchSingleTop = true
                          }
                        },
                        editHunt = {
                          navController.navigate(SeekrDestination.EditHunt.createRoute(huntId)) {
                            launchSingleTop = true
                          }
                        },
                        editReview = { reviewId ->
                          navController.navigate(
                              SeekrDestination.EditReview.createRoute(huntId, reviewId)) {
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

              // Edit Review
              composable(
                  route = SeekrDestination.EditReview.route,
                  arguments =
                      listOf(
                          navArgument(SeekrDestination.EditReview.ARG_HUNT_ID) {
                            type = NavType.StringType
                          },
                          navArgument(SeekrDestination.EditReview.ARG_REVIEW_ID) {
                            type = NavType.StringType
                          })) { backStackEntry ->
                    val huntId =
                        backStackEntry.arguments
                            ?.getString(SeekrDestination.EditReview.ARG_HUNT_ID)
                            .orEmpty()
                    val reviewId =
                        backStackEntry.arguments
                            ?.getString(SeekrDestination.EditReview.ARG_REVIEW_ID)
                            .orEmpty()

                    Surface(
                        modifier =
                            Modifier.fillMaxSize()
                                .testTag(NavigationTestTags.EDIT_REVIEW_HUNT_SCREEN)) {
                          EditReviewScreen(
                              huntId = huntId,
                              reviewId = reviewId,
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
                          },
                          onViewTerms = {
                            navController.navigate(SeekrDestination.TermsConditions.route) {
                              launchSingleTop = true
                            }
                          })
                    }
              }

              // Terms and Conditions
              composable(SeekrDestination.TermsConditions.route) {
                Surface(
                    modifier =
                        Modifier.fillMaxSize()
                            .testTag(NavigationTestTags.TERMS_CONDITIONS_SCREEN)) {
                      TermsAndConditionsScreen(onGoBack = { navController.popBackStack() })
                    }
              }

              // Review Images Screen
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
                    LaunchedEffect(reviewId) { reviewHuntViewModel.loadReview(reviewId) }
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
                  route = SeekrNavigationDefaults.PROFILE_REVIEWS_ROUTE,
                  arguments =
                      listOf(
                          navArgument(SeekrNavigationDefaults.USER_ID) {
                            type = NavType.StringType
                          })) { backStackEntry ->
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
