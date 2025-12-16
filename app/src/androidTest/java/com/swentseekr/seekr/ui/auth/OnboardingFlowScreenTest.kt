package com.swentseekr.seekr.ui.auth

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.model.authentication.OnboardingHandler
import com.swentseekr.seekr.ui.auth.OnboardingFlowTestTags.CHECKBOX_AGREE
import com.swentseekr.seekr.ui.auth.OnboardingFlowTestTags.CONTINUE_BUTTON
import com.swentseekr.seekr.ui.auth.OnboardingFlowTestTags.FINISH_BUTTON
import com.swentseekr.seekr.ui.auth.OnboardingFlowTestTags.I_AGREE_BUTTON
import com.swentseekr.seekr.ui.auth.OnboardingFlowTestTags.PROFILE_SETUP_DIALOG
import com.swentseekr.seekr.ui.auth.OnboardingFlowTestTags.TERMS_DIALOG
import com.swentseekr.seekr.ui.auth.OnboardingFlowTestTags.WELCOME_DIALOG
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Full UI test suite for the OnboardingFlow.
 *
 * Uses a FakeOnboardingHandler implementing the onboarding interface so callbacks can be verified
 * without needing mocks or Firebase.
 *
 * Tests:
 * - Dialog visibility and transitions
 * - Button enable/disable logic
 * - Text input behavior
 * - Final callback execution
 */
@RunWith(AndroidJUnit4::class)
class OnboardingFlowScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  // -------------------------------------------------------------------
  // Fake implementation of the onboarding handler
  // -------------------------------------------------------------------
  class FakeOnboardingHandler : OnboardingHandler {
    var called = false
    var userId: String? = null
    var pseudo: String? = null
    var bio: String? = null

    override fun completeOnboarding(userId: String, pseudonym: String, bio: String) {
      called = true
      this.userId = userId
      this.pseudo = pseudonym
      this.bio = bio
    }
  }

  private fun setContent(userId: String, handler: FakeOnboardingHandler) {
    composeTestRule.setContent { OnboardingFlow(userId = userId, onboardingHandler = handler) }
  }

  // -------------------------------------------------------------
  // 1. Welcome dialog is shown first
  // -------------------------------------------------------------
  @Test
  fun initialScreen_showsWelcomeDialog() {
    setContent("user1", FakeOnboardingHandler())

    composeTestRule.onNodeWithTag(WELCOME_DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CONTINUE_BUTTON).assertIsDisplayed()
  }

  // -------------------------------------------------------------
  // 2. Continue button transitions to Terms dialog
  // -------------------------------------------------------------
  @Test
  fun clickingContinue_showsTermsDialog() {
    setContent("user1", FakeOnboardingHandler())

    composeTestRule.onNodeWithTag(CONTINUE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(TERMS_DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CHECKBOX_AGREE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(I_AGREE_BUTTON).assertIsNotEnabled()
  }

  // -------------------------------------------------------------
  // 3. Agree button only enabled after checking checkbox
  // -------------------------------------------------------------
  @Test
  fun termsDialog_agreeEnabled_onlyAfterCheckingBox() {
    setContent("user1", FakeOnboardingHandler())

    composeTestRule.onNodeWithTag(CONTINUE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(I_AGREE_BUTTON).assertIsNotEnabled()

    composeTestRule.onNodeWithTag(CHECKBOX_AGREE).performClick()

    composeTestRule.onNodeWithTag(I_AGREE_BUTTON).assertIsEnabled()
  }

  // -------------------------------------------------------------
  // 4. Agree → Profile setup dialog
  // -------------------------------------------------------------
  @Test
  fun clickingAgree_showsProfileSetupDialog() {
    setContent("user1", FakeOnboardingHandler())

    composeTestRule.onNodeWithTag(CONTINUE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(CHECKBOX_AGREE).performClick()
    composeTestRule.onNodeWithTag(I_AGREE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(PROFILE_SETUP_DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FINISH_BUTTON).assertIsNotEnabled()
  }

  // -------------------------------------------------------------
  // 5. Enter pseudonym → Finish enabled
  // -------------------------------------------------------------
  @Test
  fun enteringPseudonym_enablesFinish() {
    setContent("user1", FakeOnboardingHandler())

    composeTestRule.onNodeWithTag(CONTINUE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(CHECKBOX_AGREE).performClick()
    composeTestRule.onNodeWithTag(I_AGREE_BUTTON).performClick()

    composeTestRule
        .onNode(hasText(OnboardingFlowScreenTestsConstants.PSEUDONYM))
        .performTextInput("John Doe")

    composeTestRule.onNodeWithTag(FINISH_BUTTON).assertIsEnabled()
  }

  // -------------------------------------------------------------
  // 6. Full onboarding triggers handler callback
  // -------------------------------------------------------------
  @Test
  fun fullFlow_callsOnboardingHandler() {
    val handler = FakeOnboardingHandler()
    val userId = "newUserABC"

    setContent(userId, handler)

    // Step 1
    composeTestRule.onNodeWithTag(CONTINUE_BUTTON).performClick()

    // Step 2
    composeTestRule.onNodeWithTag(CHECKBOX_AGREE).performClick()
    composeTestRule.onNodeWithTag(I_AGREE_BUTTON).performClick()

    // Step 3
    val pseudo = "Explorer"
    val bio = "Adventure lover"

    composeTestRule
        .onNode(hasText(OnboardingFlowScreenTestsConstants.PSEUDONYM))
        .performTextInput(pseudo)
    composeTestRule.onNode(hasText(OnboardingFlowScreenTestsConstants.BIO)).performTextInput(bio)
    composeTestRule.onNodeWithTag(FINISH_BUTTON).performClick()

    // Assertions
    assertTrue(OnboardingFlowScreenTestsConstants.ONBOARDING_HANDLER, handler.called)
    assertEquals(OnboardingFlowScreenTestsConstants.USER_MATCH, userId, handler.userId)
    assertEquals(OnboardingFlowScreenTestsConstants.PSEUDONYM_MISMATCH, pseudo, handler.pseudo)
    assertEquals(OnboardingFlowScreenTestsConstants.BIO_MISMATCH, bio, handler.bio)
  }

  // -------------------------------------------------------------
  // 7. Bio optional: finish still works
  // -------------------------------------------------------------
  @Test
  fun bioOptional_finishStillCallsHandler() {
    val handler = FakeOnboardingHandler()

    setContent("userX", handler)

    composeTestRule.onNodeWithTag(CONTINUE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(CHECKBOX_AGREE).performClick()
    composeTestRule.onNodeWithTag(I_AGREE_BUTTON).performClick()

    composeTestRule
        .onNode(hasText(OnboardingFlowScreenTestsConstants.PSEUDONYM))
        .performTextInput(OnboardingFlowScreenTestsConstants.USER_NO_BIO)
    composeTestRule.onNodeWithTag(FINISH_BUTTON).performClick()

    assertTrue(handler.called)
    assertEquals(OnboardingFlowScreenTestsConstants.USER_NO_BIO, handler.pseudo)
  }

  // -------------------------------------------------------------
  // 8. Finishing removes all dialogs
  // -------------------------------------------------------------
  @Test
  fun afterFinish_noDialogsRemainVisible() {
    val handler = FakeOnboardingHandler()
    var done = false
    var showFlow by mutableStateOf(true)

    composeTestRule.setContent {
      if (showFlow) {
        OnboardingFlow(
            userId = "userZ",
            onboardingHandler = handler,
            onDone = {
              done = true
              showFlow = false
            })
      }
    }

    // Flow
    composeTestRule.onNodeWithTag(CONTINUE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(CHECKBOX_AGREE).performClick()
    composeTestRule.onNodeWithTag(I_AGREE_BUTTON).performClick()

    composeTestRule
        .onNode(hasText(OnboardingFlowScreenTestsConstants.PSEUDONYM))
        .performTextInput("FinalUser")
    composeTestRule
        .onNode(hasText(OnboardingFlowScreenTestsConstants.BIO))
        .performTextInput("Some bio")
    composeTestRule.onNodeWithTag(FINISH_BUTTON).performClick()

    done
    assertTrue(done)

    composeTestRule.onNodeWithTag(WELCOME_DIALOG, useUnmergedTree = true).assertDoesNotExist()
    composeTestRule.onNodeWithTag(TERMS_DIALOG, useUnmergedTree = true).assertDoesNotExist()
    composeTestRule.onNodeWithTag(PROFILE_SETUP_DIALOG, useUnmergedTree = true).assertDoesNotExist()
  }

  // -------------------------------------------------------------
  // 9. Clicks on Terms link, opens new dialog, comes back
  // -------------------------------------------------------------
  @Test
  fun clickingTermsLink_opensAndClosesTermsDialog() {
    setContent("user1", FakeOnboardingHandler())

    composeTestRule.onNodeWithTag(CONTINUE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(TERMS_DIALOG).assertIsDisplayed()

    composeTestRule.onNodeWithTag(OnboardingFlowTestTags.TERMS).performClick()

    composeTestRule.onNodeWithTag(OnboardingFlowTestTags.TERMS_CONDITION_DIALOG).assertIsDisplayed()

    composeTestRule.onNodeWithTag(OnboardingFlowTestTags.I_DONT_AGREE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(TERMS_DIALOG).assertIsDisplayed()
  }
}
