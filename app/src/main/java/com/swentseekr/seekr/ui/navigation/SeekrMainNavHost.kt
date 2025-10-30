package com.swentseekr.seekr.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swentseekr.seekr.model.profile.mockProfileData
import com.swentseekr.seekr.ui.addhunt.AddHuntScreen
import com.swentseekr.seekr.ui.components.HuntCardScreen
import com.swentseekr.seekr.ui.map.MapScreen
import com.swentseekr.seekr.ui.overview.OverviewScreen
import com.swentseekr.seekr.ui.profile.ProfileScreen
import com.swentseekr.seekr.ui.theme.*

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
    modifier: Modifier = Modifier
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
                    testMode = testMode)
              }
              composable(
                  route = SeekrDestination.HuntCard.route,
                  arguments =
                      listOf(
                          androidx.navigation.navArgument(SeekrDestination.HuntCard.ARG_HUNT_ID) {
                            type = androidx.navigation.NavType.StringType
                          })) { backStackEntry ->
                    val argId =
                        backStackEntry.arguments?.getString(SeekrDestination.HuntCard.ARG_HUNT_ID)
                    val huntId = argId ?: lastHuntId.orEmpty() // fallback if ever needed

                    HuntCardScreen(
                        huntId = huntId,
                        onGoBack = { navController.popBackStack() },
                        modifier = Modifier.testTag(NavigationTestTags.HUNTCARD_SCREEN))
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
            }
      }
}

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SeekrAppPreview() {
  SampleAppTheme { SeekrMainNavHost() }
}
