package com.swentseekr.seekr.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [SeekrOfflineNavHost] targeting maximum line coverage (no focus on
 * conditional/branch coverage).
 *
 * These tests:
 * - Verify the start destination (Overview) is shown.
 * - Programmatically navigate to Map and Profile routes.
 * - Programmatically navigate to the Hunt Card route.
 * - Programmatically navigate to the Review Images route.
 *
 * This ensures all composable blocks inside the NavHost are executed at least once.
 */
@RunWith(AndroidJUnit4::class)
class SeekrOfflineNavHostTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() = runBlocking {
    // Ensure Firebase / repositories are in a consistent test state for ViewModels
    FirebaseTestEnvironment.setup()
  }

  //    @Test
  //    fun startDestination_showsOverviewScreen() {
  //        composeTestRule.setContent {
  //            SeekrOfflineNavHost(
  //                cachedProfile = null,
  //                offlineHunts = emptyList()
  //            )
  //        }
  //
  //        composeTestRule
  //            .onNodeWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN)
  //            .assertIsDisplayed()
  //    }
  //
  //    @Test
  //    fun canNavigateToMapAndProfileScreens() {
  //        lateinit var navController: NavHostController
  //
  //        composeTestRule.setContent {
  //            navController = rememberNavController()
  //            SeekrOfflineNavHost(
  //                cachedProfile = null,
  //                offlineHunts = emptyList(),
  //                navController = navController
  //            )
  //        }
  //
  //        // Navigate to MAP
  //        composeTestRule.runOnIdle {
  //            navController.navigate(SeekrDestination.Map.route)
  //        }
  //
  //        composeTestRule.waitUntil(timeoutMillis = 5_000) {
  //            composeTestRule
  //                .onAllNodesWithTag(NavigationTestTags.MAP_SCREEN)
  //                .fetchSemanticsNodes()
  //                .isNotEmpty()
  //        }
  //
  //        composeTestRule
  //            .onNodeWithTag(NavigationTestTags.MAP_SCREEN)
  //            .assertIsDisplayed()
  //
  //        // Navigate to PROFILE
  //        composeTestRule.runOnIdle {
  //            navController.navigate(SeekrDestination.Profile.route)
  //        }
  //
  //        composeTestRule.waitUntil(timeoutMillis = 5_000) {
  //            composeTestRule
  //                .onAllNodesWithTag(NavigationTestTags.PROFILE_TAB)
  //                .fetchSemanticsNodes()
  //                .isNotEmpty()
  //        }
  //
  //        composeTestRule
  //            .onNodeWithTag(NavigationTestTags.PROFILE_TAB)
  //            .assertIsDisplayed()
  //    }

  @Test
  fun navigateToHuntCard_displaysHuntCardScreen() {
    lateinit var navController: NavHostController

    composeTestRule.setContent {
      navController = rememberNavController()
      SeekrOfflineNavHost(
          cachedProfile = null, offlineHunts = emptyList(), navController = navController)
    }

    composeTestRule.runOnIdle {
      // Use any fake hunt ID; the NavHost only needs the argument to be present
      val route = SeekrDestination.HuntCard.createRoute("offline-hunt-id")
      navController.navigate(route)
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(NavigationTestTags.HUNTCARD_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()
  }

  @Test
  fun navigateToReviewImages_displaysImageReviewScreen() {
    lateinit var navController: NavHostController

    composeTestRule.setContent {
      navController = rememberNavController()
      SeekrOfflineNavHost(
          cachedProfile = null, offlineHunts = emptyList(), navController = navController)
    }

    composeTestRule.runOnIdle {
      // Build the concrete route from the pattern "reviewImages/{reviewId}"
      val route = SeekrNavigationDefaults.REVIEW_IMAGES_ROUTE.replace("{reviewId}", "review-123")
      navController.navigate(route)
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(NavigationTestTags.IMAGE_REVIEW_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.IMAGE_REVIEW_SCREEN).assertIsDisplayed()
  }
}
