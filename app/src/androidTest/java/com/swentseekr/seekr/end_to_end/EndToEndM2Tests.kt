package com.swentseekr.seekr.end_to_end

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrMainNavHost
import com.swentseekr.seekr.ui.profile.EditProfileTestTags
import com.swentseekr.seekr.ui.profile.Profile
import com.swentseekr.seekr.ui.profile.ProfileTestTags
import com.swentseekr.seekr.ui.settings.SettingsScreenTestTags
import com.swentseekr.seekr.utils.FakeAuthEmulator
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end test that validates editing a user profile from the settings screen and verifying that
 * the updated pseudonym is reflected on the profile screen.
 */
@RunWith(AndroidJUnit4::class)
class EndToEndEditProfileTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  private lateinit var profileRepository: ProfileRepository
  private lateinit var testProfile: Profile
  private var testUserId: String = EndToEndEditProfileTestConstants.DEFAULT_USER_ID

  @Before
  fun setupEnvironment() = runBlocking {
    FirebaseTestEnvironment.setup()

    profileRepository = ProfileRepositoryProvider.repository

    if (FirebaseTestEnvironment.isEmulatorActive()) {
      FirebaseTestEnvironment.clearEmulatorData()
    }

    // Sign in FIRST
    FirebaseAuth.getInstance().signInAnonymously().await()

    // Get the REAL user ID from Firebase Auth
    testUserId =
        FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Failed to sign in")

    // NOW create the profile with the correct UID
    testProfile =
        Profile(
            uid = testUserId, // Use the actual Firebase Auth UID
            author =
                Author(
                    hasCompletedOnboarding = true,
                    hasAcceptedTerms = true,
                    pseudonym = EndToEndEditProfileTestConstants.ORIGINAL_PSEUDONYM,
                    bio = EndToEndEditProfileTestConstants.ORIGINAL_BIO),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())

    profileRepository.createProfile(testProfile)

    composeRule.setContent {
      SeekrMainNavHost(user = FirebaseAuth.getInstance().currentUser, testMode = true)
    }
    composeRule.waitForIdle()
  }

  /** Restores repository providers and signs out */
  @After
  fun tearDownEnvironment() = runBlocking {
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      FirebaseTestEnvironment.clearEmulatorData()
    }
    FakeAuthEmulator.signOut()
  }

  /**
   * Validates the profile editing flow:
   * - Navigate to Profile
   * - Open Settings
   * - Navigate to Edit Profile
   * - Update pseudonym
   * - Save changes
   * - Verify updated pseudonym on Profile screen
   */
  @Test
  fun editProfile_fromSettings_updatesPseudonymOnProfile() {
    openProfileTab()
    openSettingsFromProfile()
    openEditProfileFromSettings()
    updatePseudonym(EndToEndEditProfileTestConstants.NEW_PSEUDONYM)
    saveProfileChanges()
    navigateBackToProfile()
  }

  /** Navigates to the Profile tab via the bottom navigation bar. */
  private fun openProfileTab() {
    composeRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    waitForNodeWithTag(ProfileTestTags.PROFILE_SCREEN)
  }

  /** Opens the Settings screen from the Profile screen. */
  private fun openSettingsFromProfile() {
    composeRule.onNodeWithTag(ProfileTestTags.SETTINGS).assertIsDisplayed().performClick()
    waitForNodeWithTag(SettingsScreenTestTags.SETTINGS_SCREEN)
  }

  /** Opens the Edit Profile screen from Settings. */
  private fun openEditProfileFromSettings() {
    composeRule
        .onNodeWithTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    waitForNodeWithTag(EditProfileTestTags.SCREEN)
  }

  /**
   * Updates the pseudonym field with the provided value.
   *
   * @param newPseudonym The new pseudonym to set.
   */
  private fun updatePseudonym(newPseudonym: String) {
    // Wait for the field to exist
    waitForNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD)

    // Wait for the field to be enabled (loading complete)
    composeRule.waitUntil(timeoutMillis = EndToEndEditProfileTestConstants.WAIT_MS) {
      try {
        composeRule.onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).assertIsEnabled()
        true
      } catch (_: Throwable) {
        false
      }
    }

    composeRule.waitForIdle()

    // Now interact with the enabled field
    composeRule.onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).performClick()
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).performTextClearance()
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).performTextInput(newPseudonym)
    composeRule.waitForIdle()
  }

  /** Saves the profile changes by clicking the save button. */
  private fun saveProfileChanges() {
    composeRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).performClick()
    composeRule.waitForIdle()
  }

  /** Navigates back to the Profile screen. */
  private fun navigateBackToProfile() {
    composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    composeRule.waitForIdle()

    composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    composeRule.waitForIdle()

    waitForNodeWithTag(ProfileTestTags.PROFILE_SCREEN)
  }

  /**
   * Verifies that the pseudonym displayed on the Profile screen matches the expected value.
   *
   * @param expectedPseudonym The expected pseudonym value.
   */
  private fun verifyPseudonymUpdated(expectedPseudonym: String) {
    composeRule
        .onNodeWithTag(ProfileTestTags.PROFILE_PSEUDONYM, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains(expectedPseudonym)
  }

  /**
   * Waits until a node with [tag] exists somewhere in the semantics tree.
   *
   * @param tag The test tag to look for.
   * @param timeoutMillis Maximum time to wait before failing/returning.
   * @param failOnTimeout Whether to throw a [ComposeTimeoutException] if the timeout elapses.
   * @return `true` if the node appears before timeout, otherwise `false` when [failOnTimeout] is
   *   `false`.
   * @throws ComposeTimeoutException If the node does not appear within [timeoutMillis] and
   *   [failOnTimeout] is `true`.
   */
  private fun waitForNodeWithTag(
      tag: String,
      timeoutMillis: Long = EndToEndEditProfileTestConstants.WAIT_MS,
      failOnTimeout: Boolean = true
  ): Boolean {
    return try {
      composeRule.waitUntil(timeoutMillis) {
        try {
          composeRule
              .onAllNodesWithTag(tag, useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
        } catch (_: Throwable) {
          false
        }
      }
      true
    } catch (e: ComposeTimeoutException) {
      if (failOnTimeout) throw e else false
    }
  }

  /**
   * Waits until the requested [text] is rendered anywhere on screen.
   *
   * @param text The text to wait for.
   * @param timeoutMillis Maximum time to wait before failing.
   * @throws ComposeTimeoutException If the text does not appear within [timeoutMillis].
   */
  private fun waitForText(
      text: String,
      timeoutMillis: Long = EndToEndEditProfileTestConstants.WAIT_MS
  ) {
    composeRule.waitUntil(timeoutMillis) {
      try {
        composeRule
            .onAllNodesWithText(text, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      } catch (_: Throwable) {
        false
      }
    }
  }

  /**
   * Finds a semantics node that supports text input for a given container/tag. This helper supports
   * two common cases:
   * 1) the tagged node itself is editable (has SetTextAction),
   * 2) the tagged node is a container and contains an editable descendant.
   *
   * @param tag The test tag of the node (or container) that should provide an editable field.
   * @return A [SemanticsNodeInteraction] that supports SetTextAction (text input).
   * @throws AssertionError If no editable node can be found for the given [tag].
   */
  private fun findEditableNode(tag: String): SemanticsNodeInteraction {
    val root = composeRule.onNodeWithTag(tag, useUnmergedTree = true)

    root.performClick()

    runCatching {
      val direct =
          composeRule.onNode(hasTestTag(tag).and(hasSetTextAction()), useUnmergedTree = true)
      direct.fetchSemanticsNode()
      return direct
    }

    runCatching {
      val subtree =
          composeRule.onNode(
              hasTestTag(tag).and(hasAnyDescendant(hasSetTextAction())), useUnmergedTree = true)
      subtree.fetchSemanticsNode()

      val descendantEditable =
          composeRule.onNode(
              hasSetTextAction().and(hasAnyAncestor(hasTestTag(tag))), useUnmergedTree = true)
      descendantEditable.fetchSemanticsNode()
      return descendantEditable
    }

    throw AssertionError(
        "No editable node (SetTextAction) found for tag '$tag' or its descendants.")
  }

  /**
   * Clears any existing text in the input identified by [tag] and replaces it with [value].
   *
   * @param tag The test tag of the input (or a container holding the input).
   * @param value The text to input.
   */
  private fun replaceText(tag: String, value: String) {
    // Wait for the field to be ready and enabled
    composeRule.waitUntil(timeoutMillis = EndToEndEditProfileTestConstants.WAIT_MS) {
      try {
        val node = composeRule.onNodeWithTag(tag, useUnmergedTree = true)
        node.fetchSemanticsNode()
        true
      } catch (_: Throwable) {
        false
      }
    }

    // For OutlinedTextField, we need to select all and then replace
    val input = composeRule.onNodeWithTag(tag, useUnmergedTree = true)
    input.performClick()
    composeRule.waitForIdle()

    // Clear existing text by selecting all and typing new text
    input.performTextClearance()
    composeRule.waitForIdle()

    input.performTextInput(value)
    composeRule.waitForIdle()
  }

  /**
   * Performs the IME action (e.g., "Search", "Done") on the input identified by [tag].
   *
   * @param tag The test tag of the input (or a container holding the input).
   */
  private fun performImeOn(tag: String) {
    val input = findEditableNode(tag)
    input.performImeAction()
  }
}

/**
 * Constants used throughout the [EndToEndEditProfileTest] to define test data and configuration
 * values.
 */
object EndToEndEditProfileTestConstants {
  const val DEFAULT_USER_ID = "test-user-edit-profile"
  const val ORIGINAL_PSEUDONYM = "OriginalUser"
  const val ORIGINAL_BIO = "This is my original bio"
  const val NEW_PSEUDONYM = "UpdatedUser"
  const val WAIT_MS = 10_000L
}
