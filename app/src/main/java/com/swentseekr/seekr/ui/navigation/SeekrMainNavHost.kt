package com.swentseekr.seekr.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swentseekr.seekr.model.profile.mockProfileData
import com.swentseekr.seekr.ui.components.HuntCardScreen
import com.swentseekr.seekr.ui.hunt.add.AddHuntScreen
import com.swentseekr.seekr.ui.map.MapScreen
import com.swentseekr.seekr.ui.overview.OverviewScreen
import com.swentseekr.seekr.ui.profile.ProfileScreen
import com.swentseekr.seekr.ui.theme.*
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.swentseekr.seekr.ui.hunt.edit.EditHuntScreen
import com.swentseekr.seekr.ui.settings.SettingsScreen
import com.swentseekr.seekr.ui.huntcardview.AddReviewScreen

// Test Tags
object NavigationTestTags {
  const val BOTTOM_NAVIGATION_MENU = "BOTTOM_NAVIGATION_MENU"
  const val OVERVIEW_TAB = "OVERVIEW_TAB"
  const val MAP_TAB = "MAP_TAB"
  const val PROFILE_TAB = "PROFILE_TAB"
  const val HUNTCARD_SCREEN = "HUNTCARD_SCREEN"
  const val ADD_HUNT_SCREEN = "ADD_HUNT_SCREEN"
}

// Destinations as sealed class
sealed class SeekrDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
  object Overview : SeekrDestination("overview", "Overview", Icons.Filled.List)

  object Map : SeekrDestination("map", "Map", Icons.Filled.Place)

  object Profile : SeekrDestination("profile", "Profile", Icons.Filled.Person)

  object HuntCard : SeekrDestination("hunt/{huntId}", "Hunt", Icons.Filled.List) {
    fun createRoute(huntId: String) = "hunt/$huntId"

    const val ARG_HUNT_ID = "huntId"
  }

  object AddHunt : SeekrDestination("add_hunt", "Add Hunt", Icons.Filled.List)

    object Settings : SeekrDestination("settings", "Settings", Icons.Filled.List)

    object EditHunt : SeekrDestination("edit_hunt/{huntId}", "Edit Hunt", Icons.Filled.List) {
        fun createRoute(huntId: String) = "edit_hunt/$huntId"
        const val ARG_HUNT_ID = "huntId"
    }

    object AddReview : SeekrDestination("add_review/{huntId}", "Add Review", Icons.Filled.List) {
        fun createRoute(huntId: String) = "add_review/$huntId"
        const val ARG_HUNT_ID = "huntId"
    }

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
  val containerColor = GrassGreen
  val iconColor = Black

  NavigationBar(
      containerColor = containerColor,
      modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)) {
        SeekrDestination.all.forEach { dest ->
          val testTag =
              when (dest) {
                is SeekrDestination.Overview -> NavigationTestTags.OVERVIEW_TAB
                is SeekrDestination.Map -> NavigationTestTags.MAP_TAB
                is SeekrDestination.Profile -> NavigationTestTags.PROFILE_TAB
                else -> "IGNORED" // HuntCard or any non-bottom-bar destination
              }

          NavigationBarItem(
              selected = currentDestination.route == dest.route,
              onClick = { onTabSelected(dest) },
              icon = { Icon(dest.icon, contentDescription = dest.label, tint = iconColor) },
              label = { Text(dest.label, color = iconColor) },
              modifier = Modifier.testTag(testTag),
              colors =
                  NavigationBarItemDefaults.colors(
                      selectedIconColor = iconColor,
                      unselectedIconColor = iconColor,
                      selectedTextColor = iconColor,
                      unselectedTextColor = iconColor,
                      indicatorColor = containerColor))
        }
      }
}

// Main App Scaffold
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekrMainNavHost(
    navController: NavHostController = rememberNavController(),
    testMode: Boolean = false,
    modifier: Modifier = Modifier,
) {
  var lastHuntId by rememberSaveable { mutableStateOf<String?>(null) }
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val currentDestination =
      SeekrDestination.all.find { it.route == currentRoute } ?: SeekrDestination.Overview
  val showBottomBar = SeekrDestination.all.any { it.route == currentRoute }

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = White,
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
            composable(SeekrDestination.Overview.route) {
                OverviewScreen(
                    onhuntclick = { huntId ->
                        lastHuntId = huntId
                        navController.navigate(SeekrDestination.HuntCard.createRoute(huntId)) {
                            launchSingleTop = true
                        }
                    })
            }
            composable(SeekrDestination.Map.route) { MapScreen() }
            composable(SeekrDestination.Profile.route) {
                val profile = mockProfileData()
                ProfileScreen(
                    userId = profile.uid,
                    onAddHunt = { navController.navigate(SeekrDestination.AddHunt.route) },
                    onSettings = { navController.navigate(SeekrDestination.Settings.route) },
                    onMyHuntClick = { huntId ->
                        navController.navigate(SeekrDestination.EditHunt.createRoute(huntId)) {
                            launchSingleTop = true
                        }
                    },
                    testMode = testMode
                )
            }
            composable(
                route = SeekrDestination.HuntCard.route,
                arguments = listOf(navArgument(SeekrDestination.HuntCard.ARG_HUNT_ID) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val argId =
                    backStackEntry.arguments?.getString(SeekrDestination.HuntCard.ARG_HUNT_ID)
                val huntId = argId ?: lastHuntId.orEmpty()

                HuntCardScreen(
                    huntId = huntId,
                    onGoBack = { navController.popBackStack() },
                    onAddReview = { id ->
                        navController.navigate(SeekrDestination.AddReview.createRoute(id)) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.testTag(NavigationTestTags.HUNTCARD_SCREEN)
                )


        }
              composable(SeekrDestination.AddHunt.route) {
                // wrapper purely to expose a testTag for UI tests
                Surface(
                    modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.ADD_HUNT_SCREEN)) {
                      AddHuntScreen(
                          onGoBack = { navController.popBackStack() },
                          onDone = {
                            // go back to overview because no view model for profile to refresh the
                            // list yet
                            navController.navigate(SeekrDestination.Overview.route) {
                              launchSingleTop = true
                              popUpTo(SeekrDestination.Overview.route)
                            }
                          },
                          testMode = testMode)
                    }
              }
            composable(SeekrDestination.Settings.route) {
                SettingsScreen(
                    onSignedOut = {
                        // After logout, go to Overview
                        navController.navigate(SeekrDestination.Overview.route) {
                            launchSingleTop = true
                            popUpTo(SeekrDestination.Overview.route)
                        }
                    },
                    onGoBack = { navController.popBackStack() }
                )
            }
            composable(
                route = SeekrDestination.AddReview.route,
                arguments = listOf(navArgument(SeekrDestination.AddReview.ARG_HUNT_ID) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val huntId = backStackEntry.arguments?.getString(SeekrDestination.AddReview.ARG_HUNT_ID).orEmpty()
                AddReviewScreen(
                    huntId = huntId,
                    onGoBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = SeekrDestination.EditHunt.route,
                arguments = listOf(navArgument(SeekrDestination.EditHunt.ARG_HUNT_ID) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val huntId = backStackEntry.arguments?.getString(SeekrDestination.EditHunt.ARG_HUNT_ID).orEmpty()
                EditHuntScreen(
                    huntId = huntId,
                    onGoBack = { navController.popBackStack() },
                    onDone = {
                        // After saving edits, go back to Profile to see updates
                        navController.navigate(SeekrDestination.Profile.route) {
                            launchSingleTop = true
                            popUpTo(SeekrDestination.Profile.route)
                        }
                    },
                    testMode = testMode
                )
            }
            }
      }
}

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SeekrAppPreview() {
  SampleAppTheme { SeekrMainNavHost() }
}
