package com.swentseekr.seekr.ui.terms

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the TermsAndConditionsScreen composable.
 *
 * This test suite verifies that the terms and conditions screen components are displayed correctly
 * and that user interactions, such as clicking the back button, function as expected.
 */
@RunWith(AndroidJUnit4::class)
class TermsAndConditionsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun termsScreenIsDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithTag(TermsScreenTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun topBarDisplaysCorrectTitle() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithText(TermsScreenStrings.TITLE).assertIsDisplayed()
  }

  @Test
  fun backButtonIsDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule
        .onNodeWithTag(TermsScreenTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .assertHasClickAction()
  }

  @Test
  fun backButtonTriggersCallbackWhenClicked() {
    var backPressed = false
    composeTestRule.setContent { TermsAndConditionsScreen(onGoBack = { backPressed = true }) }

    composeTestRule.onNodeWithTag(TermsScreenTestTags.BACK_BUTTON).performClick()

    assert(backPressed)
  }

  @Test
  fun contentColumnIsDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithTag(TermsScreenTestTags.CONTENT_COLUMN).assertIsDisplayed()
  }

  @Test
  fun lastUpdatedIsDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithText(TermsScreenStrings.LAST_UPDATED).assertIsDisplayed()
  }

  @Test
  fun section1TitleAndContentAreDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_1_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_1_CONTENT, substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun section2TitleAndContentAreDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_2_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_2_CONTENT, substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun section3TitleAndContentAreDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_3_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_3_CONTENT, substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun section4TitleAndContentAreDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_4_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_4_CONTENT, substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun section5TitleAndContentAreDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithTag(TermsScreenTestTags.LAST_SPACER).performScrollTo()
    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_5_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_5_CONTENT, substring = true)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun section6TitleAndContentAreDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithTag(TermsScreenTestTags.LAST_SPACER).performScrollTo()

    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_6_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_6_CONTENT, substring = true)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun allSectionsAreDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    // Verifies all section titles are displayed
    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_1_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_2_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_3_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_4_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_5_TITLE)
        .performScrollTo()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_6_TITLE)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun contentColumnIsScrollable() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    // Verifies the column can be scrolled to the last section
    composeTestRule
        .onNodeWithTag(TermsScreenTestTags.CONTENT_COLUMN)
        .performScrollToNode(hasText(TermsScreenStrings.SECTION_6_TITLE))

    // verifies last section is displayed after scroll
    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_6_TITLE).assertIsDisplayed()
  }

  @Test
  fun termsSectionDisplaysCorrectly() {
    val testTitle = "Test Title"
    val testContent = "Test content for the section"

    composeTestRule.setContent { TermsSection(title = testTitle, content = testContent) }

    composeTestRule.onNodeWithText(testTitle).assertIsDisplayed()
    composeTestRule.onNodeWithText(testContent).assertIsDisplayed()
  }

  @Test
  fun backButtonHasCorrectContentDescription() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule
        .onNodeWithContentDescription(TermsScreenStrings.BACK_DESCRIPTION)
        .assertIsDisplayed()
  }

  @Test
  fun termsScreenWithNoCallbackDoesNotCrash() {
    // Test that screen works even without callback
    composeTestRule.setContent { TermsAndConditionsScreen(onGoBack = {}) }

    composeTestRule.onNodeWithTag(TermsScreenTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(TermsScreenTestTags.BACK_BUTTON).performClick()

    // Should not crash
    composeTestRule.onNodeWithTag(TermsScreenTestTags.SCREEN).assertIsDisplayed()
  }
}
