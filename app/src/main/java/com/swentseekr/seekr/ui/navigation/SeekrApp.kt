package com.swentseekr.seekr.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Test Tags
object NavigationTestTags {
  const val BOTTOM_NAVIGATION_MENU = "BOTTOM_NAVIGATION_MENU"
  const val OVERVIEW_TAB = "OVERVIEW_TAB"
  const val MAP_TAB = "MAP_TAB"
  const val PROFILE_TAB = "PROFILE_TAB"
}

// Placeholder screens
@Composable
fun OverviewScreen() {
  Surface { Text("Overview Screen", modifier = Modifier.padding(32.dp)) }
}

@Composable
fun MapScreen() {
  Surface { Text("Map Screen", modifier = Modifier.padding(32.dp)) }
}

@Composable
fun ProfileScreen() {
  Surface { Text("Profile Screen", modifier = Modifier.padding(32.dp)) }
}

// Enum for destinations
enum class SeekrScreen {
  OVERVIEW,
  MAP,
  PROFILE
}

// Bottom Navigation Bar
@Composable
fun SeekrNavigationBar(currentScreen: SeekrScreen, onTabSelected: (SeekrScreen) -> Unit) {
  val grassGreen = Color(0xFF4CAF50) // nice natural green
  val black = Color.Black

  NavigationBar(
      containerColor = grassGreen,
      modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)) {
        NavigationBarItem(
            selected = currentScreen == SeekrScreen.OVERVIEW,
            onClick = { onTabSelected(SeekrScreen.OVERVIEW) },
            icon = { Icon(Icons.Filled.List, contentDescription = "Overview", tint = black) },
            label = { Text("Overview", color = black) },
            modifier = Modifier.testTag(NavigationTestTags.OVERVIEW_TAB),
            colors =
                NavigationBarItemDefaults.colors(
                    selectedIconColor = black,
                    unselectedIconColor = black,
                    selectedTextColor = black,
                    unselectedTextColor = black,
                    indicatorColor = grassGreen))
        NavigationBarItem(
            selected = currentScreen == SeekrScreen.MAP,
            onClick = { onTabSelected(SeekrScreen.MAP) },
            icon = { Icon(Icons.Filled.Place, contentDescription = "Map", tint = black) },
            label = { Text("Map", color = black) },
            modifier = Modifier.testTag(NavigationTestTags.MAP_TAB),
            colors =
                NavigationBarItemDefaults.colors(
                    selectedIconColor = black,
                    unselectedIconColor = black,
                    selectedTextColor = black,
                    unselectedTextColor = black,
                    indicatorColor = grassGreen))
        NavigationBarItem(
            selected = currentScreen == SeekrScreen.PROFILE,
            onClick = { onTabSelected(SeekrScreen.PROFILE) },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile", tint = black) },
            label = { Text("Profile", color = black) },
            modifier = Modifier.testTag(NavigationTestTags.PROFILE_TAB),
            colors =
                NavigationBarItemDefaults.colors(
                    selectedIconColor = black,
                    unselectedIconColor = black,
                    selectedTextColor = black,
                    unselectedTextColor = black,
                    indicatorColor = grassGreen))
      }
}

// App Navigation Scaffold
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekrApp(navController: NavHostController = rememberNavController()) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val currentScreen =
      when (currentRoute) {
        "overview" -> SeekrScreen.OVERVIEW
        "map" -> SeekrScreen.MAP
        "profile" -> SeekrScreen.PROFILE
        else -> SeekrScreen.OVERVIEW
      }

  Scaffold(
      containerColor = Color.White,
      bottomBar = {
        SeekrNavigationBar(
            currentScreen = currentScreen,
            onTabSelected = { screen ->
              val route =
                  when (screen) {
                    SeekrScreen.OVERVIEW -> "overview"
                    SeekrScreen.MAP -> "map"
                    SeekrScreen.PROFILE -> "profile"
                  }
              navController.navigate(route) {
                launchSingleTop = true
                popUpTo("overview")
              }
            })
      }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "overview",
            modifier = Modifier.padding(innerPadding)) {
              composable("overview") { OverviewScreen() }
              composable("map") { MapScreen() }
              composable("profile") { ProfileScreen() }
            }
      }
}

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SeekrAppPreview() {
  MaterialTheme { SeekrApp() }
}
