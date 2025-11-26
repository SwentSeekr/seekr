package com.swentseekr.seekr.ui.offline

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineMapTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun offlineMapScreen_displaysOfflineMapMessage() {
        composeTestRule.setContent {
            TestOfflineMapScreen()
        }

        composeTestRule
            .onNodeWithText(OfflineConstants.OFFLINE_MAP_MESSAGE)
            .assertIsDisplayed()
    }
}

@Composable
private fun TestOfflineMapScreen() {
    MaterialTheme {
        OfflineMapScreen()
    }
}
