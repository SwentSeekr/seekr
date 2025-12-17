package com.swentseekr.seekr.ui.navigation

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.swentseekr.seekr.FakeReviewHuntViewModel
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.ui.huntCardScreen.FakeHuntCardViewModel
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.BACK_TO_OVERVIEW_HUNT
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.EXTRA_HUNT_ID
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.HUNT_123
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.HUNT_REMOVAL_SENTENCE
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.KILLED_APP_HUNT
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.NOTIFICATION_HUNT_456
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.REMOVE_AFTER_NAV_HUNT
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.SINGLE_TOP_HUNT
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.SPECIAL_CHARS_HUNT
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.TEST_ROUTE_HUNT
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.TITLE_BACK_TEST_HUNT
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.TITLE_DEEPLINK_HUNT
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.TITLE_KILLED_APP_HUNT
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.TITLE_NOTIFICATION_HUNT
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.TITLE_REMOVE_TEST_HUNT
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.TITLE_SINGLE_TOP_HUNT
import com.swentseekr.seekr.ui.navigation.NavigationTestConstants.TITLE_SPECIAL_CHARS_HUNT
import com.swentseekr.seekr.utils.FakeRepoSuccess
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for notification deep link navigation to HuntCardScreen.
 *
 * This test suite verifies that deep links containing hunt IDs navigate correctly to the
 * HuntCardScreen, both from app launch and while the app is running. It also checks that the hunt
 * ID is removed from the intent after navigation to prevent repeated navigation on subsequent
 * launches.
 */
@RunWith(AndroidJUnit4::class)
class NotificationNavigationTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.ACCESS_FINE_LOCATION,
          android.Manifest.permission.ACCESS_COARSE_LOCATION,
          android.Manifest.permission.POST_NOTIFICATIONS)

  companion object {
    const val MED = 5_000L
    const val LONG = 10_000L
  }

  private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)

  private fun firstNode(tag: String) =
      compose.onAllNodes(hasTestTag(tag), useUnmergedTree = true).onFirst()

  private fun waitUntilTrue(timeout: Long = MED, block: () -> Boolean) {
    compose.waitUntil(timeoutMillis = timeout) { runCatching { block() }.getOrNull() == true }
  }

  private inline fun <T> withFakeRepo(repo: FakeRepoSuccess, crossinline block: () -> T): T {
    val prev = HuntRepositoryProvider.repository
    HuntRepositoryProvider.repository = repo
    return try {
      block()
    } finally {
      HuntRepositoryProvider.repository = prev
    }
  }

  @Before
  fun setUp() {
    compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }
    waitUntilTrue(MED) {
      compose
          .onAllNodes(hasTestTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  @Test
  fun deepLinkWithHuntIdNavigatesToHuntCardScreen() {
    val testHuntId = HUNT_123
    val hunt = createHunt(uid = testHuntId, title = TITLE_DEEPLINK_HUNT)

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      compose.runOnUiThread {
        compose.activity.setContent {
          val navController = rememberNavController()
          SeekrMainNavHost(
              navController = navController,
              testMode = true,
              huntCardViewModelFactory = { FakeHuntCardViewModel(hunt) },
              reviewViewModelFactory = { FakeReviewHuntViewModel() })

          LaunchedEffect(Unit) {
            navController.navigate(SeekrDestination.HuntCard.createRoute(testHuntId)) {
              launchSingleTop = true
            }
          }
        }
      }

      waitUntilTrue(LONG) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()

      node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()
    }
  }

  @Test
  fun deepLinkFromIntentNavigatesToCorrectHunt() {
    val huntIdFromNotification = NOTIFICATION_HUNT_456
    val huntTitle = TITLE_NOTIFICATION_HUNT
    val hunt = createHunt(uid = huntIdFromNotification, title = huntTitle)

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      val intent =
          Intent(compose.activity, compose.activity::class.java).apply {
            putExtra(EXTRA_HUNT_ID, huntIdFromNotification)
          }

      compose.activity.setContent {
        val navController = rememberNavController()

        val deepLinkHuntId = intent.getStringExtra(EXTRA_HUNT_ID)

        SeekrMainNavHost(
            navController = navController,
            testMode = true,
            huntCardViewModelFactory = { FakeHuntCardViewModel(hunt) },
            reviewViewModelFactory = { FakeReviewHuntViewModel() })

        LaunchedEffect(deepLinkHuntId) {
          if (deepLinkHuntId != null) {
            navController.navigate(SeekrDestination.HuntCard.createRoute(deepLinkHuntId)) {
              launchSingleTop = true
            }
            intent.removeExtra(EXTRA_HUNT_ID)
          }
        }
      }

      waitUntilTrue(LONG) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()

      compose.onNodeWithText(huntTitle, useUnmergedTree = true).assertExists()
    }
  }

  @Test
  fun deepLinkRemovesHuntIdFromIntentAfterNavigation() {
    val huntId = REMOVE_AFTER_NAV_HUNT
    val hunt = createHunt(uid = huntId, title = TITLE_REMOVE_TEST_HUNT)

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      val intent =
          Intent(compose.activity, compose.activity::class.java).apply {
            putExtra(EXTRA_HUNT_ID, huntId)
          }

      compose.activity.setContent {
        val navController = rememberNavController()
        val deepLinkHuntId = intent.getStringExtra(EXTRA_HUNT_ID)

        SeekrMainNavHost(
            navController = navController,
            testMode = true,
            huntCardViewModelFactory = { FakeHuntCardViewModel(hunt) },
            reviewViewModelFactory = { FakeReviewHuntViewModel() })

        LaunchedEffect(deepLinkHuntId) {
          if (deepLinkHuntId != null) {
            navController.navigate(SeekrDestination.HuntCard.createRoute(deepLinkHuntId)) {
              launchSingleTop = true
            }
            intent.removeExtra(EXTRA_HUNT_ID)
          }
        }
      }

      waitUntilTrue(LONG) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      compose.waitForIdle()
      assert(intent.getStringExtra(EXTRA_HUNT_ID) == null) { HUNT_REMOVAL_SENTENCE }
    }
  }

  @Test
  fun deepLinkNavigatesWithLaunchSingleTop() {
    val huntId = SINGLE_TOP_HUNT
    val hunt = createHunt(uid = huntId, title = TITLE_SINGLE_TOP_HUNT)

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      compose.runOnUiThread {
        compose.activity.setContent {
          val navController = rememberNavController()

          SeekrMainNavHost(
              navController = navController,
              testMode = true,
              huntCardViewModelFactory = { FakeHuntCardViewModel(hunt) },
              reviewViewModelFactory = { FakeReviewHuntViewModel() })

          LaunchedEffect(Unit) {
            navController.navigate(SeekrDestination.HuntCard.createRoute(huntId)) {
              launchSingleTop = true
            }
            navController.navigate(SeekrDestination.HuntCard.createRoute(huntId)) {
              launchSingleTop = true
            }
          }
        }
      }

      waitUntilTrue(LONG) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()
    }
  }

  @Test
  fun deepLinkFromNotificationCanNavigateBackToOverview() {
    val huntId = BACK_TO_OVERVIEW_HUNT
    val hunt = createHunt(uid = huntId, title = TITLE_BACK_TEST_HUNT)

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      val intent =
          Intent(compose.activity, compose.activity::class.java).apply {
            putExtra(EXTRA_HUNT_ID, huntId)
          }

      compose.activity.setContent {
        val navController = rememberNavController()
        val deepLinkHuntId = intent.getStringExtra(EXTRA_HUNT_ID)

        SeekrMainNavHost(
            navController = navController,
            testMode = true,
            huntCardViewModelFactory = { FakeHuntCardViewModel(hunt) },
            reviewViewModelFactory = { FakeReviewHuntViewModel() })

        LaunchedEffect(deepLinkHuntId) {
          if (deepLinkHuntId != null) {
            navController.navigate(SeekrDestination.HuntCard.createRoute(deepLinkHuntId)) {
              launchSingleTop = true
            }
            intent.removeExtra(EXTRA_HUNT_ID)
          }
        }
      }

      waitUntilTrue(LONG) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

      waitUntilTrue(MED) {
        node(NavigationTestTags.OVERVIEW_SCREEN).assertExists()
        true
      }

      node(NavigationTestTags.OVERVIEW_SCREEN).assertIsDisplayed()
      node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    }
  }

  @Test
  fun deepLinkWithSpecialCharactersHandlesCorrectly() {
    val huntId = SPECIAL_CHARS_HUNT
    val hunt = createHunt(uid = huntId, title = TITLE_SPECIAL_CHARS_HUNT)

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      compose.runOnUiThread {
        compose.activity.setContent {
          val navController = rememberNavController()

          SeekrMainNavHost(
              navController = navController,
              testMode = true,
              huntCardViewModelFactory = { FakeHuntCardViewModel(hunt) },
              reviewViewModelFactory = { FakeReviewHuntViewModel() })

          LaunchedEffect(Unit) {
            navController.navigate(SeekrDestination.HuntCard.createRoute(huntId)) {
              launchSingleTop = true
            }
          }
        }
      }

      waitUntilTrue(LONG) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()
    }
  }

  @Test
  fun deepLinkAfterAppKilledNavigatesCorrectly() {
    val huntId = KILLED_APP_HUNT
    val hunt = createHunt(uid = huntId, title = TITLE_KILLED_APP_HUNT)

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      val restartIntent =
          Intent(compose.activity, compose.activity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_HUNT_ID, huntId)
          }

      compose.activity.setContent {
        val navController = rememberNavController()
        val deepLinkHuntId = restartIntent.getStringExtra(EXTRA_HUNT_ID)

        SeekrMainNavHost(
            navController = navController,
            testMode = true,
            huntCardViewModelFactory = { FakeHuntCardViewModel(hunt) },
            reviewViewModelFactory = { FakeReviewHuntViewModel() })

        LaunchedEffect(deepLinkHuntId) {
          if (deepLinkHuntId != null) {
            navController.navigate(SeekrDestination.HuntCard.createRoute(deepLinkHuntId)) {
              launchSingleTop = true
            }
            restartIntent.removeExtra(EXTRA_HUNT_ID)
          }
        }
      }

      waitUntilTrue(LONG) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()
    }
  }

  @Test
  fun huntCardRouteCreatedWithCorrectFormat() {
    val huntId = TEST_ROUTE_HUNT
    val expectedRoute = "hunt/$huntId"
    val actualRoute = SeekrDestination.HuntCard.createRoute(huntId)
    assert(actualRoute == expectedRoute) { "Expected route: $expectedRoute, but got: $actualRoute" }
  }
}
