package com.swentseekr.seekr.ui.huntCardScreen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.swentseekr.seekr.ui.components.DotMenu
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DotMenuTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun dotMenu_opens_and_triggers_edit() {
    var editClicked = false

    composeRule.setContent { DotMenu(onEdit = { editClicked = true }, onDelete = {}) }

    // Open menu
    composeRule.onNodeWithTag("DOT_MENU_BUTTON").performClick()

    // Click Edit
    composeRule.onNodeWithTag("DOT_MENU_EDIT").assertIsDisplayed().performClick()

    assertTrue(editClicked)
  }

  @Test
  fun dotMenu_opens_and_triggers_delete() {
    var deleteClicked = false

    composeRule.setContent { DotMenu(onEdit = {}, onDelete = { deleteClicked = true }) }

    // Open menu
    composeRule.onNodeWithTag("DOT_MENU_BUTTON").performClick()

    // Click Delete
    composeRule.onNodeWithTag("DOT_MENU_DELETE").assertIsDisplayed().performClick()

    assertTrue(deleteClicked)
  }
}
