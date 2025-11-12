package com.swentseekr.seekr.end_to_end

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.swentseekr.seekr.ui.auth.SignInScreenTestTags
import com.swentseekr.seekr.ui.hunt.AddPointsMapScreenTestTags
import com.swentseekr.seekr.ui.hunt.HuntScreenTestTags
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrRootApp
import com.swentseekr.seekr.ui.settings.SettingsScreenTestTags
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E: login -> add hunt (fill + select 2 points) -> log out -> log in -> edit hunt
 *
 * Notes:
 * - Reuses the same tags & flows validated by the dedicated screen tests for Add/Edit Hunt (form
 *   fields, dropdowns, and the Select Locations map + dialog).
 */
@RunWith(AndroidJUnit4::class)
class EndToEndM2Tests {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setupFirebase() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
  }

  @Before
  fun setContent() {
    compose.setContent { SeekrRootApp() }
  }

  @Test
  fun login_addHunt_thenLogout_thenLogin_thenEditHunt() {
    val huntTitle = "E2E River Walk"
    val huntDescription = "Scenic path by the river."
    val editedTitle = "E2E River Walk (Edited)"

    // 0) Login (Auth flow -> Main app)
    AuthRobot(compose).assertOnSignIn().tapLogin()

    OverviewRobot(compose).assertOnOverview().openProfileViaBottomBar()

    // 1) Add Hunt (from Profile)
    ProfileRobot(compose).assertOnProfile().tapAddHuntFab()

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
        .save()

    OverviewRobot(compose).assertOnOverview().openProfileViaBottomBar()

    // 2) Verify hunt exists, then open Settings and log out
    ProfileRobot(compose).assertHuntVisible(huntTitle).openSettings()

    SettingsRobot(compose).assertOnSettings().tapSignOut()

    // 3) Back on Auth screen: login again
    AuthRobot(compose).assertOnSignIn().tapLogin()

    // 4) Open Profile, edit the hunt title, save and verify
    OverviewRobot(compose).openProfileViaBottomBar()

    ProfileRobot(compose).assertHuntVisible(huntTitle).openMyHunt(huntTitle)

    EditHuntRobot(compose).assertOnEditHunt().editTitle(editedTitle).save()

    ProfileRobot(compose).assertHuntVisible(editedTitle)
  }
}

/* ------------------------------ Robots ----------------------------------- */

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

private class OverviewRobot(private val rule: ComposeTestRule) {
  fun assertOnOverview(): OverviewRobot {
    rule.onNodeWithTag(NavigationTestTags.OVERVIEW_SCREEN).assertIsDisplayed()
    return this
  }

  fun openProfileViaBottomBar(): ProfileRobot {
    rule.clickTag(NavigationTestTags.PROFILE_TAB)
    rule.waitForIdleSync()
    return ProfileRobot(rule).assertOnProfile()
  }
}

private class ProfileRobot(private val rule: ComposeTestRule) {
  private val TEXT_MY_HUNTS_SECTION = "My Hunts"
  private val TEXT_SETTINGS = "Settings"
  private val TEXT_ADD_HUNT_FAB = "Add Hunt"

  fun assertOnProfile(): ProfileRobot {
    rule.onNodeWithText(TEXT_MY_HUNTS_SECTION, substring = true).assertIsDisplayed()
    return this
  }

  fun tapAddHuntFab(): AddHuntRobot {
    rule.tryClickByTextOrContentDesc(TEXT_ADD_HUNT_FAB)
    rule.waitForIdleSync()
    return AddHuntRobot(rule)
  }

  fun openSettings(): SettingsRobot {
    rule.onNodeWithText(TEXT_SETTINGS, substring = true).performClick()
    rule.waitForIdleSync()
    return SettingsRobot(rule).assertOnSettings()
  }

  fun assertHuntVisible(title: String): ProfileRobot {
    rule.onNodeWithText(title, substring = false).assertIsDisplayed()
    return this
  }

  fun openMyHunt(title: String): EditHuntRobot {
    rule.onNodeWithText(title, substring = false).performClick()
    rule.waitForIdleSync()
    return EditHuntRobot(rule).assertOnEditHunt()
  }
}

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
    return this
  }

  fun pickFirstDifficulty(): AddHuntRobot {
    rule.clickTag(TAG_DIFFICULTY)
    val first = com.swentseekr.seekr.model.hunt.Difficulty.values().first().name
    rule.onNodeWithText(first).performClick()
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

private class MapRobot(private val rule: ComposeTestRule) {
  private val TAG_MAP = AddPointsMapScreenTestTags.MAP_VIEW
  private val TAG_CONFIRM = AddPointsMapScreenTestTags.CONFIRM_BUTTON
  private val TAG_POINT_FIELD = AddPointsMapScreenTestTags.POINT_NAME_FIELD

  fun addPointNamed(name: String): MapRobot {
    rule.onNodeWithTag(TAG_MAP).performClick()
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

private class SettingsRobot(private val rule: ComposeTestRule) {
  fun assertOnSettings(): SettingsRobot {
    rule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
    return this
  }

  fun tapSignOut(): AuthRobot {
    rule.onNodeWithTag(SettingsScreenTestTags.LOGOUT_BUTTON).performClick()
    rule.waitForIdleSync()
    return AuthRobot(rule)
  }
}

private class EditHuntRobot(private val rule: ComposeTestRule) {
  // Reuse the same field & save tags as AddHunt (Edit screen renders BaseHuntFieldsScreen)
  private val TAG_TITLE_FIELD = HuntScreenTestTags.INPUT_HUNT_TITLE
  private val TAG_SAVE_BUTTON = HuntScreenTestTags.HUNT_SAVE

  fun assertOnEditHunt(): EditHuntRobot {
    rule.onNodeWithTag(NavigationTestTags.EDIT_HUNT_SCREEN).assertIsDisplayed()
    return this
  }

  fun editTitle(newTitle: String): EditHuntRobot {
    rule.onNodeWithTag(TAG_TITLE_FIELD).apply {
      performTextClearance()
      performTextInput(newTitle)
    }
    return this
  }

  fun save(): ProfileRobot {
    rule.onNodeWithTag(TAG_SAVE_BUTTON).assertIsEnabled().performClick()
    rule.waitForIdleSync()
    return ProfileRobot(rule)
  }
}

/* ----------------------------- Helpers ----------------------------------- */

private fun ComposeTestRule.clickTag(tag: String) {
  onNodeWithTag(tag).performClick()
}

private fun ComposeTestRule.replaceText(tag: String, text: String) {
  onNodeWithTag(tag).performTextClearance()
  onNodeWithTag(tag).performTextInput(text)
}

private fun ComposeTestRule.tryClickByTextOrContentDesc(textOrCd: String) {
  val byText = onNodeWithText(textOrCd, substring = true)
  try {
    byText.performClick()
    return
  } catch (_: Throwable) {
    /* fall through */
  }

  val byCd = onNodeWithContentDescription(textOrCd, substring = true)
  byCd.performClick()
}

private fun ComposeTestRule.waitForIdleSync() {
  this.waitForIdle()
}
