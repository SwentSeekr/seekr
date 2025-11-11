package com.swentseekr.seekr.end_to_end

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
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
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrRootApp
import com.swentseekr.seekr.ui.settings.SettingsScreenTestTags
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E: login -> add hunt -> log out -> log in -> edit hunt
 *
 * We mount SeekrRootApp(), so:
 *  - When signed out: AuthNavHost / SignInScreen
 *  - When signed in:  SeekrMainNavHost
 *
 * Robots use:
 *  - Login:  SignInScreenTestTags.LOGIN_BUTTON
 *  - Logout: SettingsScreenTestTags.LOGOUT_BUTTON
 *  - Screens: NavigationTestTags (Overview/Profile/Add/Edit/Settings/HuntCard)
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
    /**
     * First, Tests according to user stories.
     * I want to pick a hunt from the overview screen and start it, so that I can begin the hunt.
     * Then, I want to review it, and it should be in my finished hunts list.
     */

/* ------------------------------ Robots ----------------------------------- */

private class AuthRobot(private val rule: ComposeTestRule) {
    // We assert presence of the app logo or the login button; pick whichever is more stable.
    fun assertOnSignIn(): AuthRobot {
        rule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
        return this
    }

    fun tapLogin(): OverviewRobot {
        // Your SignInScreen’s Google button. In tests, back it with a repo stub or test-only path.
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
    // Adjust if you expose a dedicated PROFILE_SCREEN tag
    private val TEXT_MY_HUNTS_SECTION = "My Hunts"
    private val TEXT_SETTINGS = "Settings"
    private val TEXT_ADD_HUNT_FAB = "Add Hunt" // content-desc or text on your FAB

    fun assertOnProfile(): ProfileRobot {
        rule.onNodeWithText(TEXT_MY_HUNTS_SECTION, substring = true).assertIsDisplayed()
        return this
    }

    fun tapAddHuntFab(): AddHuntRobot {
        // Prefer a testTag on FAB; fallback to text/content-desc
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
    // Make sure your AddHuntScreen exposes these tags; otherwise replace with your actual ones
    private val TAG_TITLE_FIELD = "ADD_HUNT_TITLE"
    private val TAG_DESCRIPTION_FIELD = "ADD_HUNT_DESCRIPTION"
    private val TAG_SAVE_BUTTON = "ADD_HUNT_SAVE"

    fun assertOnAddHuntScreen(): AddHuntRobot {
        rule.onNodeWithTag(NavigationTestTags.ADD_HUNT_SCREEN).assertIsDisplayed()
        return this
    }

    fun typeTitle(text: String): AddHuntRobot {
        rule.replaceText(TAG_TITLE_FIELD, text)
        return this
    }

    fun typeDescription(text: String): AddHuntRobot {
        rule.replaceText(TAG_DESCRIPTION_FIELD, text)
        return this
    }

    fun save(): OverviewRobot {
        rule.clickTag(TAG_SAVE_BUTTON)
        rule.waitForIdleSync()
        return OverviewRobot(rule).assertOnOverview()
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
    private val TAG_TITLE_FIELD = "EDIT_HUNT_TITLE"
    private val TAG_SAVE_BUTTON = "EDIT_HUNT_SAVE"

    fun assertOnEditHunt(): EditHuntRobot {
        rule.onNodeWithTag(NavigationTestTags.EDIT_HUNT_SCREEN).assertIsDisplayed()
        return this
    }

    fun editTitle(newTitle: String): EditHuntRobot {
        rule.replaceText(TAG_TITLE_FIELD, newTitle)
        return this
    }

    fun save(): ProfileRobot {
        rule.clickTag(TAG_SAVE_BUTTON)
        rule.waitForIdleSync()
        return ProfileRobot(rule).assertOnProfile()
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
    // Try text first, then content description (helpful for icon FABs)
    val byText = onNodeWithText(textOrCd, substring = true)
    try {
        byText.performClick()
        return
    } catch (_: Throwable) { /* fall through */ }

    val byCd = onNodeWithContentDescription(textOrCd, substring = true)
    byCd.performClick()
}

private fun ComposeTestRule.waitForIdleSync() {
    this.waitForIdle()
}
