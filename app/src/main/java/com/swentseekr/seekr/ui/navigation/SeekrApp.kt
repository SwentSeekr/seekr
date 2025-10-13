package com.swentseekr.seekr.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.swentseekr.seekr.ui.map.MapScreen
import com.swentseekr.seekr.ui.profile.ProfileScreen
import com.swentseekr.seekr.ui.theme.*

// Test Tags
object NavigationTestTags {
  const val BOTTOM_NAVIGATION_MENU = "BOTTOM_NAVIGATION_MENU"
  const val OVERVIEW_TAB = "OVERVIEW_TAB"
  const val MAP_TAB = "MAP_TAB"
  const val PROFILE_TAB = "PROFILE_TAB"
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

  companion object {
    val all = listOf(Overview, Map, Profile)
  }
}

// Placeholder screen
@Composable
fun OverviewScreen() {
  Surface { Text("Overview Screen", modifier = Modifier.padding(32.dp)) }
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
fun SeekrApp(navController: NavHostController = rememberNavController()) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val currentDestination =
      SeekrDestination.all.find { it.route == currentRoute } ?: SeekrDestination.Overview

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = White,
      bottomBar = {
        SeekrNavigationBar(
            currentDestination = currentDestination,
            onTabSelected = { destination ->
              navController.navigate(destination.route) {
                launchSingleTop = true
                popUpTo(SeekrDestination.Overview.route)
              }
            })
      }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SeekrDestination.Overview.route,
            modifier = Modifier.padding(innerPadding)) {
              composable(SeekrDestination.Overview.route) { OverviewScreen() }
              composable(SeekrDestination.Map.route) { MapScreen() }
              composable(SeekrDestination.Profile.route) {
                val profile = mockProfileData()
                ProfileScreen(profile = profile, currentUserId = profile.uid)
              }
            }
      }
}

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SeekrAppPreview() {
  SampleAppTheme { SeekrApp() }
}
