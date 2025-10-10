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
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.map.MapScreen
import com.swentseekr.seekr.ui.profile.Profile
import com.swentseekr.seekr.ui.profile.ProfileScreen

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

              // Real Map screen
              composable("map") { MapScreen() }

              // Real Profile screen — pass mock data for now
              composable("profile") {
                val sampleAuthor =
                    Author(
                        pseudonym = "Spike Man",
                        bio = "Avid adventurer and puzzle solver.",
                        profilePicture = 0,
                        reviewRate = 4.5,
                        sportRate = 4.8)

                val sampleProfile =
                    Profile(
                        uid = "user123",
                        author = sampleAuthor,
                        myHunts =
                            mutableListOf(
                                Hunt(
                                    uid = "hunt123",
                                    start = Location(40.7128, -74.0060, "New York"),
                                    end = Location(40.730610, -73.935242, "Brooklyn"),
                                    middlePoints = emptyList(),
                                    status = HuntStatus.FUN,
                                    title = "City Exploration",
                                    description = "Discover hidden gems in the city",
                                    time = 2.5,
                                    distance = 5.0,
                                    difficulty = Difficulty.DIFFICULT,
                                    author = sampleAuthor,
                                    image = com.swentseekr.seekr.R.drawable.ic_launcher_foreground,
                                    reviewRate = 4.5)),
                        doneHunts = mutableListOf(),
                        likedHunts = mutableListOf())

                ProfileScreen(profile = sampleProfile, currentUserId = "user123")
              }
            }
      }
}

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SeekrAppPreview() {
  MaterialTheme { SeekrApp() }
}
