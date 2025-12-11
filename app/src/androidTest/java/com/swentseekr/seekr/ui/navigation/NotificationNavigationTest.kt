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
import com.swentseekr.seekr.utils.FakeRepoSuccess
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
    const val SHORT = 3_000L
    const val MED = 5_000L
    const val LONG = 10_000L
    const val XLONG = 40_000L
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
  fun deepLink_withHuntId_navigatesToHuntCardScreen() {
    val testHuntId = "deeplink_hunt_123"
    val hunt = createHunt(uid = testHuntId, title = "DeepLink Hunt")

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
  fun deepLink_fromIntent_navigatesToCorrectHunt() {
    val huntIdFromNotification = "notification_hunt_456"
    val huntTitle = "Notification Hunt"
    val hunt = createHunt(uid = huntIdFromNotification, title = huntTitle)

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      val intent =
          Intent(compose.activity, compose.activity::class.java).apply {
            putExtra("huntId", huntIdFromNotification)
          }

      compose.activity.setContent {
        val navController = rememberNavController()

        val deepLinkHuntId = intent.getStringExtra("huntId")

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
            intent.removeExtra("huntId")
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
  fun deepLink_removesHuntIdFromIntent_afterNavigation() {
    val huntId = "remove_after_nav"
    val hunt = createHunt(uid = huntId, title = "Remove Test Hunt")

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      val intent =
          Intent(compose.activity, compose.activity::class.java).apply {
            putExtra("huntId", huntId)
          }

      compose.activity.setContent {
        val navController = rememberNavController()
        val deepLinkHuntId = intent.getStringExtra("huntId")

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
            intent.removeExtra("huntId")
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
      assert(intent.getStringExtra("huntId") == null) {
        "huntId should be removed from intent after navigation"
      }
    }
  }

  @Test
  fun deepLink_navigatesWithLaunchSingleTop() {
    val huntId = "single_top_hunt"
    val hunt = createHunt(uid = huntId, title = "Single Top Hunt")

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
  fun deepLink_fromNotification_canNavigateBackToOverview() {
    val huntId = "back_to_overview_hunt"
    val hunt = createHunt(uid = huntId, title = "Back Test Hunt")

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      val intent =
          Intent(compose.activity, compose.activity::class.java).apply {
            putExtra("huntId", huntId)
          }

      compose.activity.setContent {
        val navController = rememberNavController()
        val deepLinkHuntId = intent.getStringExtra("huntId")

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
            intent.removeExtra("huntId")
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
  fun deepLink_withSpecialCharacters_handlesCorrectly() {
    val huntId = "hunt-with_special.chars123"
    val hunt = createHunt(uid = huntId, title = "Special Chars Hunt")

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
  fun deepLink_afterAppKilled_navigatesCorrectly() {
    val huntId = "killed_app_hunt"
    val hunt = createHunt(uid = huntId, title = "Killed App Hunt")

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      val restartIntent =
          Intent(compose.activity, compose.activity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("huntId", huntId)
          }

      compose.activity.setContent {
        val navController = rememberNavController()
        val deepLinkHuntId = restartIntent.getStringExtra("huntId")

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
            restartIntent.removeExtra("huntId")
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
  fun huntCardRoute_createdWithCorrectFormat() {
    val huntId = "test_route_hunt"
    val expectedRoute = "hunt/$huntId"
    val actualRoute = SeekrDestination.HuntCard.createRoute(huntId)
    assert(actualRoute == expectedRoute) { "Expected route: $expectedRoute, but got: $actualRoute" }
  }
}
