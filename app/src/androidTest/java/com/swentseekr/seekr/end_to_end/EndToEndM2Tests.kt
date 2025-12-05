package com.swentseekr.seekr.end_to_end

import android.Manifest
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
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
import com.swentseekr.seekr.ui.profile.EditProfileTestTags
import com.swentseekr.seekr.ui.profile.ProfileTestTags
import com.swentseekr.seekr.ui.settings.SettingsScreenTestTags
import com.swentseekr.seekr.utils.FakeAuthEmulator
import com.swentseekr.seekr.utils.FakeJwtGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

// -------------------------------------------------------------------------
// Timeouts used for polling UI state via waitUntil.
// Keep them centralized so we can tweak timings easily if needed.
// -------------------------------------------------------------------------
private const val WAIT_SHORT_MS = 10_000L
private const val WAIT_LONG_MS = 40_000L

/**
 * End-to-end tests for "Milestone 2" flows.
 *
 * We always:
 * - Boot the real app entrypoint [SeekrRootApp].
 * - Start in an authenticated state via [FakeAuthEmulator].
 * - Drive navigation only through UI (tabs, buttons, forms, etc.).
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
   * When [SeekrRootApp] starts, it sees an already authenticated user and goes directly to the main
   * navigation (Overview/Profile/etc.) instead of the auth flow.
   */
  @Before
  fun setupFirebaseAndAuth() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()

    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    val fakeToken = FakeJwtGenerator.createFakeGoogleIdToken()
    FakeAuthEmulator.signInWithFakeGoogleToken(fakeToken)
  }

  /**
   * E2E scenario:
   * - Start app in authenticated state.
   * - Navigate Overview → Profile → Settings → Edit Profile.
   * - Change the pseudonym and save.
   * - Verify the updated pseudonym is visible again on the Profile screen.
   */
  /*@Test
  fun editProfile_fromSettings_updatesPseudonymOnProfile() {
    val newPseudonym = "E2E Edited Pseudonym"
    val newBio = "E2E test bio for this user"

    compose.setContent { SeekrRootApp() }

    // 0) Because we are authenticated, we should land on Overview.
    val overview = OverviewRobot(compose).assertOnOverview()

    // 1) Navigate to Profile via bottom bar.
    val profileRobot = overview.openProfileViaBottomBar()

    // 2) From Profile → open Settings.
    val settingsRobot = profileRobot.assertOnProfile().openSettings()

    // 3) From Settings → open Edit Profile, change pseudonym & bio, and save (back to
    // Settings).
    val settingsAfterSave =
        settingsRobot
            .openEditProfile()
            .typePseudonym(newPseudonym)
            .typeBio(newBio)
            .save() // now returns SettingsRobot

    // 4) Go back from Settings → Profile and assert pseudonym updated.
    settingsAfterSave.goBackToProfile().assertPseudonymVisible(newPseudonym)
  }*/
}
/* ------------------------------------------------------------------------ */
/*                                  Robots                                  */
/* ------------------------------------------------------------------------ */

/**
 * Small wrapper around the auth screen.
 *
 * Mostly useful if we add flows that explicitly sign out and back in.
 */
private class AuthRobot(private val rule: ComposeTestRule) {

  fun assertOnSignIn(): AuthRobot {
    rule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
    return this
  }

  fun tapLogin(): OverviewRobot {
    rule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).performClick()
    rule.waitForIdleSync()
    return OverviewRobot(rule).assertOnOverview()
  }
}

/** Robot for interacting with the Overview (main feed) screen. */
private class OverviewRobot(private val rule: ComposeTestRule) {

  fun assertOnOverview(): OverviewRobot {
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule
            .onAllNodesWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN, useUnmergedTree = true)
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
 * - Waiting for profile content to load (and loading indicator to disappear).
 * - Opening the Add Hunt FAB.
 * - Navigating to Settings.
 * - Verifying and opening hunts shown in the "My Hunts" list.
 */
private class ProfileRobot(private val rule: ComposeTestRule) {
  private val TEXT_SETTINGS = "Settings"

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

    // ✅ Make sure no more layout / animation is in progress.
    rule.waitForIdleSync()

    return this
  }

  /**
   * Asserts that the profile pseudonym text eventually matches [expected].
   *
   * Uses a matcher constrained to the pseudonym tag + the expected text to avoid collisions with
   * other nodes.
   */
  fun assertPseudonymVisible(expected: String): ProfileRobot {
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule
            .onNode(
                hasTestTag(ProfileTestTags.PROFILE_PSEUDONYM) and
                    hasText(expected, substring = true))
            .fetchSemanticsNode()
        true
      } catch (_: Throwable) {
        false
      }
    }

    rule
        .onNode(
            hasTestTag(ProfileTestTags.PROFILE_PSEUDONYM) and hasText(expected, substring = true))
        .assertIsDisplayed()

    rule.waitForIdleSync()
    return this
  }

  fun tapAddHuntFab(): AddHuntRobot {
    rule.onNodeWithTag(ProfileTestTags.ADD_HUNT).assertIsDisplayed().performClick()
    rule.waitForIdleSync()
    return AddHuntRobot(rule)
  }

  /** Opens the Settings screen from Profile by clicking on the "Settings" entry. */
  /** Opens the Settings screen from Profile by clicking on the "Settings" entry. */
  /** Opens the Settings screen from Profile by clicking the Settings icon button. */
  fun openSettings(): SettingsRobot {
    // 1) Wait until the Settings icon is in the semantics tree and visible.
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule.onNodeWithTag(ProfileTestTags.SETTINGS, useUnmergedTree = true).assertIsDisplayed()
        true
      } catch (_: Throwable) {
        false
      }
    }

    // 2) Click the icon button.
    rule.onNodeWithTag(ProfileTestTags.SETTINGS, useUnmergedTree = true).performClick()

    // 3) Let navigation finish and assert we’re on the Settings screen.
    rule.waitForIdleSync()
    return SettingsRobot(rule).assertOnSettings()
  }

  fun assertHuntVisible(title: String): ProfileRobot {
    // Try to ensure we’re on the "My Hunts" tab.
    try {
      rule.onNodeWithTag(ProfileTestTags.TAB_MY_HUNTS).performClick()
      rule.waitForIdleSync()
    } catch (_: Throwable) {
      // If not found, assume default tab is already MY_HUNTS.
    }

    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule.onNodeWithText(title, substring = false).fetchSemanticsNode()
        true
      } catch (_: Throwable) {
        false
      }
    }

    try {
      rule.onNodeWithText(title, substring = false).performScrollTo()
    } catch (_: Throwable) {
      // Ignore if not scrollable or already visible.
    }

    rule.onNodeWithText(title, substring = false).assertIsDisplayed()
    rule.waitForIdleSync()
    return this
  }

  fun openMyHunt(title: String): EditHuntRobot {
    rule.onNodeWithText(title, substring = false).performClick()
    rule.waitForIdleSync()
    return EditHuntRobot(rule).assertOnEditHunt()
  }
}

/** Robot for the "Add Hunt" screen (create or edit form). */
private class AddHuntRobot(private val rule: ComposeTestRule) {
  private val TAG_TITLE = HuntScreenTestTags.INPUT_HUNT_TITLE
  private val TAG_DESC = HuntScreenTestTags.INPUT_HUNT_DESCRIPTION
  private val TAG_TIME = HuntScreenTestTags.INPUT_HUNT_TIME
  private val TAG_DISTANCE = HuntScreenTestTags.INPUT_HUNT_DISTANCE
  private val TAG_STATUS = HuntScreenTestTags.DROPDOWN_STATUS
  private val TAG_DIFFICULTY = HuntScreenTestTags.DROPDOWN_DIFFICULTY
  private val TAG_SELECT_LOCATIONS = HuntScreenTestTags.BUTTON_SELECT_LOCATION
  private val TAG_SAVE = HuntScreenTestTags.HUNT_SAVE

  fun assertOnAddHuntScreen(): AddHuntRobot {
    rule.onNodeWithTag(HuntScreenTestTags.ADD_HUNT_SCREEN).assertIsDisplayed()
    return this
  }

  fun typeTitle(text: String): AddHuntRobot {
    rule.replaceText(TAG_TITLE, text)
    return this
  }

  fun typeDescription(text: String): AddHuntRobot {
    rule.replaceText(TAG_DESC, text)
    return this
  }

  fun typeTime(text: String): AddHuntRobot {
    rule.replaceText(TAG_TIME, text)
    return this
  }

  fun typeDistance(text: String): AddHuntRobot {
    rule.replaceText(TAG_DISTANCE, text)
    return this
  }

  fun pickFirstStatus(): AddHuntRobot {
    rule.clickTag(TAG_STATUS)
    val first = com.swentseekr.seekr.model.hunt.HuntStatus.values().first().name
    rule.onNodeWithText(first).performClick()
    rule.waitForIdleSync()
    return this
  }

  fun pickFirstDifficulty(): AddHuntRobot {
    rule.clickTag(TAG_DIFFICULTY)
    val first = com.swentseekr.seekr.model.hunt.Difficulty.values().first().name
    rule.onNodeWithText(first).performClick()
    rule.waitForIdleSync()
    return this
  }

  fun openSelectLocations(): MapRobot {
    rule.clickTag(TAG_SELECT_LOCATIONS)
    rule.waitForIdleSync()
    return MapRobot(rule)
  }

  fun save(): OverviewRobot {
    rule.onNodeWithTag(TAG_SAVE).assertIsEnabled().performClick()
    rule.waitForIdleSync()
    return OverviewRobot(rule).assertOnOverview()
  }
}

/** Robot for the "Select Points on Map" flow. */
private class MapRobot(private val rule: ComposeTestRule) {
  private val TAG_MAP = AddPointsMapScreenTestTags.MAP_VIEW
  private val TAG_CONFIRM = AddPointsMapScreenTestTags.CONFIRM_BUTTON
  private val TAG_POINT_FIELD = AddPointsMapScreenTestTags.POINT_NAME_FIELD

  fun addPointNamed(name: String): MapRobot {
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule.onNodeWithTag(TAG_MAP).assertIsDisplayed()
        true
      } catch (_: Throwable) {
        false
      }
    }

    rule.onNodeWithTag(TAG_MAP).performClick()

    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule.onNodeWithTag(TAG_POINT_FIELD).fetchSemanticsNode()
        true
      } catch (_: Throwable) {
        false
      }
    }

    rule.onNodeWithTag(TAG_POINT_FIELD).performTextClearance()
    rule.onNodeWithTag(TAG_POINT_FIELD).performTextInput(name)
    rule.onNodeWithText("Add").performClick()
    rule.waitForIdleSync()
    return this
  }

  fun confirmPoints(): AddHuntRobot {
    rule.onNodeWithTag(TAG_CONFIRM).assertIsEnabled().performClick()
    rule.waitForIdleSync()
    return AddHuntRobot(rule).assertOnAddHuntScreen()
  }
}

/** Robot for the Settings screen. */
private class SettingsRobot(private val rule: ComposeTestRule) {

  /** Asserts that the Settings screen is visible, with proper waiting. */
  fun assertOnSettings(): SettingsRobot {
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule
            .onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN, useUnmergedTree = true)
            .assertIsDisplayed()
        true
      } catch (_: Throwable) {
        false
      }
    }

    // Final hard assert once waitUntil succeeds.
    rule
        .onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN, useUnmergedTree = true)
        .assertIsDisplayed()

    rule.waitForIdleSync()
    return this
  }

  fun tapSignOut(): AuthRobot {
    rule.onNodeWithTag(SettingsScreenTestTags.LOGOUT_BUTTON).performClick()
    rule.waitForIdleSync()
    return AuthRobot(rule)
  }

  fun goBackToProfile(): ProfileRobot {
    rule.onNodeWithTag(SettingsScreenTestTags.BACK_BUTTON).performClick()
    rule.waitForIdleSync()
    return ProfileRobot(rule).assertOnProfile()
  }

  /**
   * Opens the "Edit Profile" screen from Settings.
   *
   * Tries a dedicated test tag first, then falls back to clicking by visible text / content
   * description "Edit profile".
   */
  fun openEditProfile(): EditProfileRobot {
    var clicked = false

    // 1) Preferred: dedicated test tag, if you have one.
    try {
      rule.onNodeWithTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON).performClick()
      clicked = true
    } catch (_: Throwable) {
      // Ignore and try alternatives.
    }

    if (!clicked) {
      // 2) Fallback: visible text.
      try {
        rule.onNodeWithText("Edit profile", substring = true).performClick()
        clicked = true
      } catch (_: Throwable) {
        // Ignore and try content description.
      }
    }

    if (!clicked) {
      // 3) Fallback: content description (for icon-only buttons).
      rule.tryClickByTextOrContentDesc("Edit profile")
    }

    rule.waitForIdleSync()
    return EditProfileRobot(rule).assertOnEditProfile()
  }
}

/** Robot for the Edit Hunt screen. */
private class EditHuntRobot(private val rule: ComposeTestRule) {
  private val TAG_TITLE_FIELD = HuntScreenTestTags.INPUT_HUNT_TITLE
  private val TAG_SAVE_BUTTON = HuntScreenTestTags.HUNT_SAVE

  fun assertOnEditHunt(): EditHuntRobot {
    rule.onNodeWithTag(NavigationTestTags.EDIT_HUNT_SCREEN).assertIsDisplayed()
    rule.waitForIdleSync()
    return this
  }

  fun editTitle(newTitle: String): EditHuntRobot {
    rule.onNodeWithTag(TAG_TITLE_FIELD).apply {
      performTextClearance()
      performTextInput(newTitle)
    }
    rule.waitForIdleSync()
    return this
  }

  fun save(): ProfileRobot {
    Espresso.closeSoftKeyboard()
    rule.waitForIdleSync()

    try {
      rule.onNodeWithTag(TAG_SAVE_BUTTON).performScrollTo()
    } catch (_: Throwable) {
      // Ignore if not in scrollable parent.
    }

    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule.onNodeWithTag(TAG_SAVE_BUTTON).assertIsEnabled()
        true
      } catch (_: Throwable) {
        false
      }
    }

    rule.onNodeWithTag(TAG_SAVE_BUTTON).performClick()
    rule.waitForIdleSync()

    return ProfileRobot(rule).assertOnProfile()
  }
}

/** Robot for the Edit Profile screen. */
private class EditProfileRobot(private val rule: ComposeTestRule) {

  private fun waitUntilReady() {
    // Wait until pseudonym field exists & is enabled
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule
            .onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD)
            .assertIsDisplayed()
            .assertIsEnabled()
        true
      } catch (_: Throwable) {
        false
      }
    }

    // Wait until bio field exists & is enabled
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule.onNodeWithTag(EditProfileTestTags.BIO_FIELD).assertIsDisplayed().assertIsEnabled()
        true
      } catch (_: Throwable) {
        false
      }
    }

    rule.waitForIdleSync()
  }

  fun assertOnEditProfile(): EditProfileRobot {
    waitUntilReady()
    rule.onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).assertIsDisplayed().assertIsEnabled()
    rule.onNodeWithTag(EditProfileTestTags.BIO_FIELD).assertIsDisplayed().assertIsEnabled()

    return this
  }

  fun typePseudonym(newPseudonym: String): EditProfileRobot {
    rule.onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).apply {
      performTextClearance()
      performTextInput(newPseudonym)
    }
    rule.waitForIdleSync()
    return this
  }

  fun typeBio(newBio: String): EditProfileRobot {
    rule.onNodeWithTag(EditProfileTestTags.BIO_FIELD).apply {
      performTextClearance()
      performTextInput(newBio)
    }
    rule.waitForIdleSync()
    return this
  }

  /** Saves changes on Edit Profile and returns to Settings. */
  fun save(): SettingsRobot {
    waitUntilReady()
    Espresso.closeSoftKeyboard()
    rule.waitForIdleSync()

    // Scroll Save button into view if necessary
    try {
      rule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).performScrollTo()
    } catch (_: Throwable) {
      // Ignore if not inside a scrollable parent
    }

    // 🔍 1) Prove that the Save button actually becomes enabled.
    // If this times out, the issue is in uiState.canSave / validation, NOT in the test.
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      try {
        rule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsEnabled()
        true
      } catch (_: Throwable) {
        false
      }
    }

    // 2) Click Save
    rule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).performClick()
    rule.waitForIdleSync()

    // 3) Wait for navigation: either Settings is visible or EditProfile is gone
    rule.waitUntil(timeoutMillis = WAIT_LONG_MS) {
      val onSettings =
          try {
            rule
                .onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN, useUnmergedTree = true)
                .assertIsDisplayed()
            true
          } catch (_: Throwable) {
            false
          }

      val editProfileGone =
          try {
            rule.onNodeWithTag(EditProfileTestTags.SCREEN, useUnmergedTree = true)
            // Found → still here
            false
          } catch (_: Throwable) {
            // Not found → we've navigated away
            true
          }

      onSettings || editProfileGone
    }

    // Even if we didn't yet see SETTINGS_SCREEN, construct the robot so the caller
    // can still chain .assertOnSettings(), which will fail if we never ended up there.
    return SettingsRobot(rule)
  }
}

/* ------------------------------------------------------------------------ */
/*                                 Helpers                                  */
/* ------------------------------------------------------------------------ */

private fun ComposeTestRule.clickTag(tag: String) {
  onNodeWithTag(tag).performClick()
}

private fun ComposeTestRule.replaceText(tag: String, text: String) {
  onNodeWithTag(tag).performTextClearance()
  onNodeWithTag(tag).performTextInput(text)
}

/**
 * Tries to click a node either by visible text or by content description, using [textOrCd] as the
 * match string (substring match).
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

/** Thin wrapper over [ComposeTestRule.waitForIdle]. */
private fun ComposeTestRule.waitForIdleSync() {
  this.waitForIdle()
}
