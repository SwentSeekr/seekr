package com.swentseekr.seekr.end_to_end

import android.Manifest
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.FirebaseApp
import com.swentseekr.seekr.ui.auth.SignInScreenTestTags
import com.swentseekr.seekr.ui.hunt.AddPointsMapScreenTestTags
import com.swentseekr.seekr.ui.hunt.HuntScreenTestTags
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrRootApp
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.ui.profile.ProfileTestTags
import com.swentseekr.seekr.ui.settings.SettingsScreenTestTags
import com.swentseekr.seekr.utils.FakeAuthEmulator
import com.swentseekr.seekr.utils.FakeJwtGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// -------------------------------------------------------------------------
// Timeouts used for polling UI state via waitUntil.
// Keep them centralized so we can tweak timings easily if needed.
// -------------------------------------------------------------------------
private const val WAIT_SHORT_MS = 10_000L
private const val WAIT_LONG_MS = 20_000L

/**
 * End-to-end test for the "Milestone 2" flow.
 *
 * High-level scenario:
 * 1) App starts while user is *already authenticated* via [FakeAuthEmulator].
 * 2) Overview screen appears.
 * 3) Navigate to Profile via bottom bar.
 * 4) From Profile, open "Add Hunt".
 * 5) Fill the hunt form, select 2 points on the map, and save the hunt.
 * 6) Back to Profile, verify the hunt exists in "My Hunts".
 * 7) Open the hunt in Edit mode.
 * 8) Change the title and save.
 * 9) Verify the edited title is visible in Profile.
 *
 * Notes:
 * - This test intentionally reuses the same semantics test tags used by the dedicated screen-level
 *   tests (Overview/Profile/Add/Edit/Map).
 * - Authentication uses a deterministic "fake" Google token and a Firebase emulator helper, so no
 *   real network / Google sign-in happens here.
 */
@RunWith(AndroidJUnit4::class)
class EndToEndM2Tests {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  /**
   * Grants location permissions so that any map components (e.g. Select Locations) can render and
   * interact without OS permission dialogs blocking the test.
   */
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION,
      )

  /**
   * Initializes Firebase once per test process and signs in a fake user using a JWT created by
   * [FakeJwtGenerator] and wired through [FakeAuthEmulator].
   *
   * The important part for the E2E flow is that, when [SeekrRootApp] starts, it will see an already
   * authenticated user and go directly to the main navigation (Overview/Profile/etc.) instead of
   * the auth flow.
   */
  @Before
  fun setupFirebaseAndAuth() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Initialize FirebaseApp if it hasn't been done already in this process.
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    // Create a deterministic fake Google ID token and sign in via the emulator helper.
    val fakeToken = FakeJwtGenerator.createFakeGoogleIdToken()
    FakeAuthEmulator.signInWithFakeGoogleToken(fakeToken)
  }

  /**
   * Main E2E scenario:
   * - Start app in authenticated state.
   * - Create a hunt (with 2 map points).
   * - Navigate back to profile, open the hunt, edit the title.
   * - Assert that the edited title is shown.
   */
  @Test
  fun addHunt_thenEditHunt() {
    val huntTitle = "E2E River Walk"
    val huntDescription = "Scenic path by the river."
    val editedTitle = "E2E River Walk (Edited)"

    // Entry point for the app under test.
    compose.setContent { SeekrRootApp() }

    // 0) We’re already authenticated via FakeAuthEmulator in @Before.
    //    Wait for the Overview screen to be ready.
    OverviewRobot(compose).assertOnOverview().openProfileViaBottomBar()

    // 1) Add Hunt from Profile screen.
    ProfileRobot(compose).assertOnProfile().tapAddHuntFab()

    // 2) Fill the Add Hunt form, select locations, and save.
    AddHuntRobot(compose)
        .assertOnAddHuntScreen()
        .typeTitle(huntTitle)
        .typeDescription(huntDescription)
        .typeTime("1.5")
        .typeDistance("3.2")
        .pickFirstStatus()
        .pickFirstDifficulty()
        .openSelectLocations()
        .addPointNamed("Start Bridge")
        .addPointNamed("End Promenade")
        .confirmPoints()
        .save() // -> Overview

    // 3) Navigate back to Profile to edit the just-created hunt.
    OverviewRobot(compose).openProfileViaBottomBar()

    ProfileRobot(compose).assertOnProfile().assertHuntVisible(huntTitle).openMyHunt(huntTitle)

    // 4) Edit the hunt title and verify the updated title is displayed in Profile.
    EditHuntRobot(compose).assertOnEditHunt().editTitle(editedTitle).save()

    ProfileRobot(compose).assertHuntVisible(editedTitle)
  }
}

/* ------------------------------------------------------------------------ */
/*                                  Robots                                  */
/* ------------------------------------------------------------------------ */

/**
 * Small wrapper around the auth screen.
 *
 * In this specific E2E test we start with an authenticated user, so this robot is mostly useful
 * when we deliberately sign out or write additional tests that exercise the login UI.
 */
private class AuthRobot(private val rule: ComposeTestRule) {

  /** Verifies that the Sign-In screen is currently visible. */
  fun assertOnSignIn(): AuthRobot {
    rule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
    return this
  }

  /**
   * Taps the login button and waits for the Overview screen to be ready, returning an
   * [OverviewRobot].
   */
  fun tapLogin(): OverviewRobot {
    rule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).performClick()
    rule.waitForIdleSync()
    return OverviewRobot(rule).assertOnOverview()
  }
}

/** Robot for interacting with the Overview (main feed) screen. */
private class OverviewRobot(private val rule: ComposeTestRule) {

  /**
   * Waits until at least one node with the Overview tag exists and then asserts that the overview
   * screen is displayed.
   *
   * This protects us against transient/loading states and recompositions.
   */
  fun assertOnOverview(): OverviewRobot {
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule
            .onAllNodesWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN)
            .fetchSemanticsNodes()
            .isNotEmpty()
      } catch (_: Throwable) {
        false
      }
    }

    rule.onNodeWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN).assertIsDisplayed()
    return this
  }

  /** Clicks the Profile tab in the bottom navigation bar and returns a [ProfileRobot]. */
  fun openProfileViaBottomBar(): ProfileRobot {
    rule.clickTag(NavigationTestTags.PROFILE_TAB)
    rule.waitForIdleSync()
    return ProfileRobot(rule).assertOnProfile()
  }
}

/**
 * Robot for the Profile screen.
 *
 * Handles:
 * - Waiting for the profile content to load (and loading indicator to disappear).
 * - Opening the Add Hunt FAB.
 * - Navigating to Settings.
 * - Verifying and opening hunts shown in the "My Hunts" list.
 */
private class ProfileRobot(private val rule: ComposeTestRule) {
  private val TEXT_SETTINGS = "Settings"

  /**
   * Waits until:
   * - the profile screen node exists; and
   * - the loading node is no longer present; then asserts that key profile elements are displayed.
   */
  fun assertOnProfile(): ProfileRobot {
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      val hasProfileScreen =
          try {
            rule.onNodeWithTag(ProfileTestTags.PROFILE_SCREEN).fetchSemanticsNode()
            true
          } catch (_: Throwable) {
            false
          }

      val isLoading =
          try {
            rule.onNodeWithTag(ProfileTestTags.PROFILE_LOADING).fetchSemanticsNode()
            true
          } catch (_: Throwable) {
            false
          }

      hasProfileScreen && !isLoading
    }

    rule.onNodeWithTag(ProfileTestTags.PROFILE_SCREEN).assertIsDisplayed()
    rule.onNodeWithTag(ProfileTestTags.PROFILE_PSEUDONYM).assertIsDisplayed()
    rule.onNodeWithTag(ProfileTestTags.PROFILE_HUNTS_LIST).assertIsDisplayed()

    return this
  }

  /**
   * Taps the "Add Hunt" Floating Action Button on the Profile screen and returns an [AddHuntRobot]
   * for further interaction.
   */
  fun tapAddHuntFab(): AddHuntRobot {
    // Use the dedicated tag from ProfileTestTags instead of textual matching.
    rule.onNodeWithTag(ProfileTestTags.ADD_HUNT).assertIsDisplayed().performClick()
    rule.waitForIdleSync()
    return AddHuntRobot(rule)
  }

  /** Opens the Settings screen from Profile by clicking on the "Settings" entry. */
  fun openSettings(): SettingsRobot {
    rule.onNodeWithText(TEXT_SETTINGS, substring = true).performClick()
    rule.waitForIdleSync()
    return SettingsRobot(rule).assertOnSettings()
  }

  /**
   * Ensures the given [title] is visible somewhere in the "My Hunts" tab.
   *
   * The method:
   * 1) Ensures the "My Hunts" tab is selected (best-effort).
   * 2) Waits until the title appears in the UI.
   * 3) Scrolls to it if needed.
   * 4) Asserts it is displayed.
   */
  fun assertHuntVisible(title: String): ProfileRobot {
    // 1) Make sure we are on the "My Hunts" tab (ignore failures if tab not found).
    try {
      rule.onNodeWithTag(ProfileTestTags.TAB_MY_HUNTS).performClick()
      rule.waitForIdleSync()
    } catch (_: Throwable) {
      // If the tab isn't found, we assume the default tab is already MY_HUNTS.
    }

    // 2) Wait for the hunt title to actually appear in the UI (handles async loading).
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule.onNodeWithText(title, substring = false).fetchSemanticsNode()
        true
      } catch (_: Throwable) {
        false
      }
    }

    // 3) Try to scroll to it in case it's off-screen inside a LazyColumn.
    try {
      rule.onNodeWithText(title, substring = false).performScrollTo()
    } catch (_: Throwable) {
      // If no scrollable parent or it's already visible, ignore.
    }

    // 4) Final assertion.
    rule.onNodeWithText(title, substring = false).assertIsDisplayed()
    rule.waitForIdleSync()

    return this
  }

  /** Opens a hunt from the "My Hunts" list by its [title], and returns an [EditHuntRobot]. */
  fun openMyHunt(title: String): EditHuntRobot {
    rule.onNodeWithText(title, substring = false).performClick()
    rule.waitForIdleSync()
    return EditHuntRobot(rule).assertOnEditHunt()
  }
}

/**
 * Robot for the "Add Hunt" screen (create or edit form).
 *
 * Encapsulates interactions with the form fields, dropdowns, and the navigation to the "Select
 * Locations" map.
 */
private class AddHuntRobot(private val rule: ComposeTestRule) {
  private val TAG_TITLE = HuntScreenTestTags.INPUT_HUNT_TITLE
  private val TAG_DESC = HuntScreenTestTags.INPUT_HUNT_DESCRIPTION
  private val TAG_TIME = HuntScreenTestTags.INPUT_HUNT_TIME
  private val TAG_DISTANCE = HuntScreenTestTags.INPUT_HUNT_DISTANCE
  private val TAG_STATUS = HuntScreenTestTags.DROPDOWN_STATUS
  private val TAG_DIFFICULTY = HuntScreenTestTags.DROPDOWN_DIFFICULTY
  private val TAG_SELECT_LOCATIONS = HuntScreenTestTags.BUTTON_SELECT_LOCATION
  private val TAG_SAVE = HuntScreenTestTags.HUNT_SAVE

  /** Ensures we are on the Add Hunt screen. */
  fun assertOnAddHuntScreen(): AddHuntRobot {
    rule.onNodeWithTag(HuntScreenTestTags.ADD_HUNT_SCREEN).assertIsDisplayed()
    return this
  }

  /** Replaces the content of the Title text field. */
  fun typeTitle(text: String): AddHuntRobot {
    rule.replaceText(TAG_TITLE, text)
    return this
  }

  /** Replaces the content of the Description text field. */
  fun typeDescription(text: String): AddHuntRobot {
    rule.replaceText(TAG_DESC, text)
    return this
  }

  /** Replaces the content of the Time text field. */
  fun typeTime(text: String): AddHuntRobot {
    rule.replaceText(TAG_TIME, text)
    return this
  }

  /** Replaces the content of the Distance text field. */
  fun typeDistance(text: String): AddHuntRobot {
    rule.replaceText(TAG_DISTANCE, text)
    return this
  }

  /**
   * Opens the Status dropdown and selects the first enum value from
   * [com.swentseekr.seekr.model.hunt.HuntStatus].
   */
  fun pickFirstStatus(): AddHuntRobot {
    rule.clickTag(TAG_STATUS)
    val first = com.swentseekr.seekr.model.hunt.HuntStatus.values().first().name
    rule.onNodeWithText(first).performClick()
    rule.waitForIdleSync()
    return this
  }

  /**
   * Opens the Difficulty dropdown and selects the first enum value from
   * [com.swentseekr.seekr.model.hunt.Difficulty].
   */
  fun pickFirstDifficulty(): AddHuntRobot {
    rule.clickTag(TAG_DIFFICULTY)
    val first = com.swentseekr.seekr.model.hunt.Difficulty.values().first().name
    rule.onNodeWithText(first).performClick()
    rule.waitForIdleSync()
    return this
  }

  /** Navigates to the "Select Locations" map screen for picking points. */
  fun openSelectLocations(): MapRobot {
    rule.clickTag(TAG_SELECT_LOCATIONS)
    rule.waitForIdleSync()
    return MapRobot(rule)
  }

  /**
   * Clicks the Save button (after ensuring it is enabled) and returns an [OverviewRobot].
   *
   * This assumes that after saving, the screen navigates back to Overview.
   */
  fun save(): OverviewRobot {
    rule.onNodeWithTag(TAG_SAVE).assertIsEnabled().performClick()
    rule.waitForIdleSync()
    return OverviewRobot(rule).assertOnOverview()
  }
}

/**
 * Robot for the "Select Points on Map" flow.
 *
 * Encapsulates:
 * - Adding points by tapping the map and filling the dialog.
 * - Confirming the selected points and returning to Add Hunt.
 */
private class MapRobot(private val rule: ComposeTestRule) {
  private val TAG_MAP = AddPointsMapScreenTestTags.MAP_VIEW
  private val TAG_CONFIRM = AddPointsMapScreenTestTags.CONFIRM_BUTTON
  private val TAG_POINT_FIELD = AddPointsMapScreenTestTags.POINT_NAME_FIELD

  /**
   * Adds a point with the given [name]:
   * 1) Taps the map (this should open a dialog).
   * 2) Waits for the point-name field to appear.
   * 3) Types the name and taps "Add".
   */
  fun addPointNamed(name: String): MapRobot {
    // 1) Wait for the map to actually be on screen (robust on slow CI).
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule.onNodeWithTag(TAG_MAP).assertIsDisplayed()
        true
      } catch (_: Throwable) {
        false
      }
    }

    // 2) Tap the map to open the point-name dialog.
    rule.onNodeWithTag(TAG_MAP).performClick()

    // 3) Wait until the point-name text field actually exists before typing.
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule.onNodeWithTag(TAG_POINT_FIELD).fetchSemanticsNode()
        true
      } catch (_: Throwable) {
        false
      }
    }

    // 4) Type the name and confirm.
    rule.onNodeWithTag(TAG_POINT_FIELD).performTextClearance()
    rule.onNodeWithTag(TAG_POINT_FIELD).performTextInput(name)
    rule.onNodeWithText("Add").performClick()
    rule.waitForIdleSync()
    return this
  }

  /** Confirms all selected points and returns to the Add Hunt screen. */
  fun confirmPoints(): AddHuntRobot {
    rule.onNodeWithTag(TAG_CONFIRM).assertIsEnabled().performClick()
    rule.waitForIdleSync()
    return AddHuntRobot(rule).assertOnAddHuntScreen()
  }
}

/** Robot for the Settings screen. */
private class SettingsRobot(private val rule: ComposeTestRule) {

  /** Verifies that the Settings screen is visible. */
  fun assertOnSettings(): SettingsRobot {
    rule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
    return this
  }

  /**
   * Taps the "Sign out" button and returns an [AuthRobot], which expects to see the login screen
   * again.
   */
  fun tapSignOut(): AuthRobot {
    rule.onNodeWithTag(SettingsScreenTestTags.LOGOUT_BUTTON).performClick()
    rule.waitForIdleSync()
    return AuthRobot(rule)
  }
}

/**
 * Robot for the Edit Hunt screen.
 *
 * Reuses the same semantic tags as the Add Hunt screen for form fields, plus a specific tag for the
 * Edit Hunt navigation entry.
 */
private class EditHuntRobot(private val rule: ComposeTestRule) {
  private val TAG_TITLE_FIELD = HuntScreenTestTags.INPUT_HUNT_TITLE
  private val TAG_SAVE_BUTTON = HuntScreenTestTags.HUNT_SAVE

  /**
   * Verifies we are currently on the Edit Hunt screen (by tag) and waits for Compose to be idle.
   */
  fun assertOnEditHunt(): EditHuntRobot {
    rule.onNodeWithTag(NavigationTestTags.EDIT_HUNT_SCREEN).assertIsDisplayed()
    rule.waitForIdleSync()
    return this
  }

  /**
   * Clears the existing title and types [newTitle].
   *
   * This method assumes that the title field is already present and focusable.
   */
  fun editTitle(newTitle: String): EditHuntRobot {
    rule.onNodeWithTag(TAG_TITLE_FIELD).apply {
      performTextClearance()
      performTextInput(newTitle)
    }
    rule.waitForIdleSync()
    return this
  }

  /**
   * Saves the current edit:
   * 1) Closes the soft keyboard (via Espresso).
   * 2) Waits for Compose to be idle.
   * 3) Scrolls the Save button into view (if necessary).
   * 4) Polls until the Save button is enabled.
   * 5) Clicks Save and returns a [ProfileRobot].
   */
  fun save(): ProfileRobot {
    // 1) Ask Espresso to close the soft keyboard.
    Espresso.closeSoftKeyboard()

    // 2) Give Compose time to settle after the keyboard closes.
    rule.waitForIdleSync()

    // 3) Try to scroll the Save button into view (if it’s inside a scrollable container).
    try {
      rule.onNodeWithTag(TAG_SAVE_BUTTON).performScrollTo()
    } catch (_: Throwable) {
      // If there is no scrollable parent, ignore.
    }

    // 4) Wait until the Save button is actually enabled
    //    (validation complete, recomposition done).
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule.onNodeWithTag(TAG_SAVE_BUTTON).assertIsEnabled()
        true
      } catch (_: Throwable) {
        false
      }
    }

    // 5) Click the Save button and wait until navigation is complete.
    rule.onNodeWithTag(TAG_SAVE_BUTTON).performClick()
    rule.waitForIdleSync()

    return ProfileRobot(rule)
  }
}

/* ------------------------------------------------------------------------ */
/*                                 Helpers                                  */
/* ------------------------------------------------------------------------ */

/** Convenience helper to click a node by its test [tag]. */
private fun ComposeTestRule.clickTag(tag: String) {
  onNodeWithTag(tag).performClick()
}

/**
 * Convenience helper to replace text in a text field identified by [tag]:
 * - Clears the existing text.
 * - Types the new [text].
 */
private fun ComposeTestRule.replaceText(tag: String, text: String) {
  onNodeWithTag(tag).performTextClearance()
  onNodeWithTag(tag).performTextInput(text)
}

/**
 * Tries to click a node either by visible text or by content description, using [textOrCd] as the
 * match string (substring match).
 *
 * This is useful for icon buttons that only expose a content description.
 */
private fun ComposeTestRule.tryClickByTextOrContentDesc(textOrCd: String) {
  val byText = onNodeWithText(textOrCd, substring = true)
  try {
    byText.performClick()
    return
  } catch (_: Throwable) {
    // Fall through and try by content description instead.
  }

  val byCd = onNodeWithContentDescription(textOrCd, substring = true)
  byCd.performClick()
}

/**
 * Thin wrapper over [ComposeTestRule.waitForIdle] with a more descriptive name for tests that
 * conceptually "wait for the UI to settle".
 */
private fun ComposeTestRule.waitForIdleSync() {
  this.waitForIdle()
}
