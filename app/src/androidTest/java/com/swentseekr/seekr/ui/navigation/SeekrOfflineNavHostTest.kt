package com.swentseekr.seekr.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.model.profile.createOverviewTestHunt
import com.swentseekr.seekr.model.profile.sampleProfile
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [SeekrOfflineNavHost] navigation in offline mode.
 *
 * This test suite verifies that navigating to different screens within the offline navigation host
 * works correctly, including HuntCardScreen, ImageReviewScreen, and bottom bar tab navigation.
 */
@RunWith(AndroidJUnit4::class)
class SeekrOfflineNavHostTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before fun setup() = runBlocking { FirebaseTestEnvironment.setup() }

  @Test
  fun navigateToHuntCardDisplaysHuntCardScreen() {
    lateinit var navController: NavHostController

    composeTestRule.setContent {
      navController = rememberNavController()
      SeekrOfflineNavHost(
          cachedProfile = null, offlineHunts = emptyList(), navController = navController)
    }

    composeTestRule.runOnIdle {
      val route = SeekrDestination.HuntCard.createRoute("offline-hunt-id")
      navController.navigate(route)
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(NavigationTestTags.HUNTCARD_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onAllNodesWithTag(NavigationTestTags.HUNTCARD_SCREEN)[0].assertIsDisplayed()
  }

  @Test
  fun navigateToReviewImagesDisplaysImageReviewScreen() {
    lateinit var navController: NavHostController

    composeTestRule.setContent {
      navController = rememberNavController()
      SeekrOfflineNavHost(
          cachedProfile = null, offlineHunts = emptyList(), navController = navController)
    }

    composeTestRule.runOnIdle {
      val route = SeekrNavigationDefaults.REVIEW_IMAGES_ROUTE.replace("{reviewId}", "review-123")
      navController.navigate(route)
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(NavigationTestTags.IMAGE_REVIEW_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onAllNodesWithTag(NavigationTestTags.IMAGE_REVIEW_SCREEN)[0].assertIsDisplayed()
  }

  /**
   * Triggers OfflineOverviewHuntsScreen's onHuntClick, which executes:
   *
   * navController.navigate(SeekrDestination.HuntCard.createRoute(huntId)) { launchSingleTop = true
   * }
   */
  @Test
  fun clickingOverviewHuntNavigatesViaOnHuntClickToHuntCard() {
    val offlineHunt =
        createOverviewTestHunt(
            uid = "offline-hunt-id",
            title = "Offline Hunt 1",
            description = "Test offline hunt",
            time = 30.0,
            distance = 2.5,
        )

    composeTestRule.setContent {
      SeekrOfflineNavHost(
          cachedProfile = null,
          offlineHunts = listOf(offlineHunt),
      )
    }

    // There are two nodes with this tag (Surface + Column), so use onAllNodesWithTag and pick one.
    composeTestRule.onAllNodesWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN)[0].assertIsDisplayed()

    // Same story: if the text exists in multiple semantic nodes, use index 0.
    composeTestRule.onAllNodesWithText("Offline Hunt 1")[0].performClick()

    composeTestRule.onAllNodesWithTag(NavigationTestTags.HUNTCARD_SCREEN)[0].assertIsDisplayed()
  }

  /**
   * Clicks the bottom bar items (Map, Profile) to trigger:
   *
   * navController.navigate(destination.route) { launchSingleTop = true
   * popUpTo(SeekrDestination.Overview.route) }
   *
   * and to render the Map and Profile surfaces.
   */
  @Test
  fun bottomBarTabsNavigateToMapAndProfileAndRenderScreens() {
    val cachedProfile =
        sampleProfile(
            myHunts = emptyList(),
            doneHunts = emptyList(),
            likedHunts = emptyList(),
        )

    composeTestRule.setContent {
      SeekrOfflineNavHost(
          cachedProfile = cachedProfile,
          offlineHunts = emptyList(),
      )
    }

    // Wait until some tabs (Role.Tab) are present.
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodes(hasRole(Role.Tab)).fetchSemanticsNodes().isNotEmpty()
    }

    val tabs = composeTestRule.onAllNodes(hasRole(Role.Tab))

    // Assuming SeekrDestination.all is [Overview, Map, Profile]:
    // click Map
    tabs[1].performClick()
    composeTestRule.onAllNodesWithTag(NavigationTestTags.MAP_SCREEN)[0].assertIsDisplayed()

    // click Profile
    tabs[2].performClick()
    composeTestRule.onAllNodesWithTag(NavigationTestTags.PROFILE_TAB)[0].assertIsDisplayed()
  }
}

/** Local helper to match nodes with a specific Role. */
private fun hasRole(role: Role): SemanticsMatcher =
    SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
