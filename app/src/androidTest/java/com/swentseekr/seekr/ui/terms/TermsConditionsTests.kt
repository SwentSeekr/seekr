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
 * This test suite verifies that the terms and conditions screen components are displayed
 * correctly and that user interactions, such as clicking the back button, function as expected.
 */
@RunWith(AndroidJUnit4::class)
class TermsAndConditionsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun termsScreen_isDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithTag(TermsScreenTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun topBar_displaysCorrectTitle() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithText(TermsScreenStrings.TITLE).assertIsDisplayed()
  }

  @Test
  fun backButton_isDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule
        .onNodeWithTag(TermsScreenTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .assertHasClickAction()
  }

  @Test
  fun backButton_triggersCallback_whenClicked() {
    var backPressed = false
    composeTestRule.setContent { TermsAndConditionsScreen(onGoBack = { backPressed = true }) }

    composeTestRule.onNodeWithTag(TermsScreenTestTags.BACK_BUTTON).performClick()

    assert(backPressed)
  }

  @Test
  fun contentColumn_isDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithTag(TermsScreenTestTags.CONTENT_COLUMN).assertIsDisplayed()
  }

  @Test
  fun lastUpdated_isDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithText(TermsScreenStrings.LAST_UPDATED).assertIsDisplayed()
  }

  @Test
  fun section1_titleAndContent_areDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_1_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_1_CONTENT, substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun section2_titleAndContent_areDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_2_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_2_CONTENT, substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun section3_titleAndContent_areDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_3_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_3_CONTENT, substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun section4_titleAndContent_areDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_4_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_4_CONTENT, substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun section5_titleAndContent_areDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithTag(TermsScreenTestTags.LAST_SPACER).performScrollTo()
    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_5_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_5_CONTENT, substring = true)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun section6_titleAndContent_areDisplayed() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule.onNodeWithTag(TermsScreenTestTags.LAST_SPACER).performScrollTo()

    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_6_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(TermsScreenStrings.SECTION_6_CONTENT, substring = true)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun allSections_areDisplayed() {
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
  fun contentColumn_isScrollable() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    // Verifies the column can be scrolled to the last section
    composeTestRule
        .onNodeWithTag(TermsScreenTestTags.CONTENT_COLUMN)
        .performScrollToNode(hasText(TermsScreenStrings.SECTION_6_TITLE))

    // verifies last section is displayed after scroll
    composeTestRule.onNodeWithText(TermsScreenStrings.SECTION_6_TITLE).assertIsDisplayed()
  }

  @Test
  fun termsSection_displaysCorrectly() {
    val testTitle = "Test Title"
    val testContent = "Test content for the section"

    composeTestRule.setContent { TermsSection(title = testTitle, content = testContent) }

    composeTestRule.onNodeWithText(testTitle).assertIsDisplayed()
    composeTestRule.onNodeWithText(testContent).assertIsDisplayed()
  }

  @Test
  fun backButton_hasCorrectContentDescription() {
    composeTestRule.setContent { TermsAndConditionsScreen() }

    composeTestRule
        .onNodeWithContentDescription(TermsScreenStrings.BACK_DESCRIPTION)
        .assertIsDisplayed()
  }

  @Test
  fun termsScreen_withNoCallback_doesNotCrash() {
    // Test that screen works even without callback
    composeTestRule.setContent { TermsAndConditionsScreen(onGoBack = {}) }

    composeTestRule.onNodeWithTag(TermsScreenTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(TermsScreenTestTags.BACK_BUTTON).performClick()

    // Should not crash
    composeTestRule.onNodeWithTag(TermsScreenTestTags.SCREEN).assertIsDisplayed()
  }
}
