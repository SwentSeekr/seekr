package com.swentseekr.seekr.ui.add

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.AddPointsMapScreenTestTags
import com.swentseekr.seekr.ui.hunt.BaseAddPointsMapScreen
import com.swentseekr.seekr.ui.hunt.PointNameDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val title = "Select Hunt Points"
private val confirmButtonText0 = "Confirm Points (0)"
private val confirmButtonText2 = "Confirm Points (2)"

class AddPointsMapScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private fun setContent(
      initPoints: List<Location> = emptyList(),
      onDone: (List<Location>) -> Unit = {},
      onCancel: () -> Unit = {}
  ) {
    composeRule.setContent {
      MaterialTheme {
        BaseAddPointsMapScreen(onDone = onDone, initPoints = initPoints, onCancel = onCancel)
      }
    }
  }

  @Test
  fun initial_empty_showsTitle_cancel_andConfirmDisabled() {
    var canceled = false
    setContent(onCancel = { canceled = true })

    // Title visible
    composeRule.onNodeWithText(title).assertExists()

    // Confirm shows count 0 and disabled
    composeRule
        .onNodeWithTag(AddPointsMapScreenTestTags.CONFIRM_BUTTON)
        .assertExists()
        .assertTextContains(confirmButtonText0)
        .assertIsNotEnabled()

    // Map exists
    composeRule.onNodeWithTag(AddPointsMapScreenTestTags.MAP_VIEW).assertExists()

    // Cancel invokes callback
    composeRule.onNodeWithTag(AddPointsMapScreenTestTags.CANCEL_BUTTON).performClick()
    assertTrue(canceled)
  }

  @Test
  fun initPoints_two_enablesConfirm_andConfirmPassesPoints() {
    val p1 = Location(0.0, 0.0, "A")
    val p2 = Location(1.0, 1.0, "B")
    var received: List<Location>? = null

    setContent(initPoints = listOf(p1, p2), onDone = { received = it })

    // Confirm shows count 2 and enabled
    composeRule
        .onNodeWithTag(AddPointsMapScreenTestTags.CONFIRM_BUTTON)
        .assertTextContains(confirmButtonText2)
        .assertIsEnabled()

    // Confirm passes initial points through
    composeRule.onNodeWithTag(AddPointsMapScreenTestTags.CONFIRM_BUTTON).performClick()
    composeRule.waitForIdle()

    val result = requireNotNull(received)
    assertEquals(2, result.size)
    assertEquals(p1, result[0])
    assertEquals(p2, result[1])
  }

  @Test
  fun pointNameDialog_validationLogic_works() {
    var confirmedName: String? = null
    var confirmedDescription: String? = null

    composeRule.setContent {
      MaterialTheme {
        PointNameDialog(
            show = true,
            onDismiss = {},
            onConfirm = { name, description ->
              confirmedName = name
              confirmedDescription = description
            })
      }
    }

    // Initially disabled
    composeRule.onNodeWithText(AddTestConstants.ADD).assertIsNotEnabled()

    // Enter valid name should enable
    composeRule
        .onNodeWithTag(AddPointsMapScreenTestTags.POINT_NAME_FIELD)
        .performTextInput("Eiffel Tower")
    composeRule.onNodeWithText(AddTestConstants.ADD).assertIsEnabled()

    // Clear name should show error and disable
    composeRule.onNodeWithTag(AddPointsMapScreenTestTags.POINT_NAME_FIELD).performTextClearance()
    composeRule.onNodeWithText(AddTestConstants.NAME_EMPTY).assertExists()
    composeRule.onNodeWithText(AddTestConstants.ADD).assertIsNotEnabled()

    // Enter another valid name and confirm
    composeRule
        .onNodeWithTag(AddPointsMapScreenTestTags.POINT_NAME_FIELD)
        .performTextInput("Louvre Museum")
    composeRule
        .onNodeWithTag(AddPointsMapScreenTestTags.POINT_DESCRIPTION_FIELD)
        .performTextInput("Paris Museum")
    composeRule.onNodeWithText(AddTestConstants.ADD).performClick()

    composeRule.waitForIdle()
    assertEquals("Louvre Museum", confirmedName)
    assertEquals("Paris Museum", confirmedDescription)
  }
}
