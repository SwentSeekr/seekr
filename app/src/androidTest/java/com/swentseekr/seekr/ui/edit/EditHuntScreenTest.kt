package com.swentseekr.seekr.ui.edit

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.HuntScreenTestTags
import com.swentseekr.seekr.ui.hunt.add.AddHuntViewModel
import com.swentseekr.seekr.ui.hunt.edit.EditHuntScreen
import com.swentseekr.seekr.ui.hunt.edit.EditHuntViewModel
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import com.swentseekr.seekr.utils.FirebaseTestEnvironment.clearEmulatorData
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class EditHuntScreenTest {

    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var repository: HuntsRepository
    private lateinit var addVM: AddHuntViewModel

    @Before
    fun setUp() =
        kotlinx.coroutines.test.runTest {
            FirebaseTestEnvironment.setup()
            if (FirebaseTestEnvironment.isEmulatorActive()) {
                clearEmulatorData()
            }
            FirebaseAuth.getInstance().signInAnonymously().await()
            repository = HuntRepositoryProvider.repository
            addVM = AddHuntViewModel(repository)
        }

    @After
    fun tearDown() =
        kotlinx.coroutines.test.runTest {
            if (FirebaseTestEnvironment.isEmulatorActive()) {
                clearEmulatorData()
            }
            FirebaseAuth.getInstance().signOut()
        }

    private fun setContent(
        huntId: String,
        vm: EditHuntViewModel,
        onGoBack: () -> Unit = {},
        onDone: () -> Unit = {}
    ) {
        composeRule.setContent {
            MaterialTheme {
                EditHuntScreen(
                    huntId = huntId, editHuntViewModel = vm, onGoBack = onGoBack, onDone = onDone)
            }
        }
    }

    @Test
    fun back_navigates_from_edit_screen() =
        kotlinx.coroutines.test.runTest {
            createHunt()
            advanceUntilIdle()
            val all = repository.getAllHunts()
            assertTrue(all.isNotEmpty())
            val id = all.first().uid
            val vm = EditHuntViewModel(repository)
            var backCalled = false
            setContent(id, vm, onGoBack = { backCalled = true })

            // Wait for load to complete by observing VM state
            composeRule.waitUntil(timeoutMillis = 5_000) { vm.uiState.value.title.isNotEmpty() }

            composeRule.onNodeWithContentDescription("Back").performClick()
            assertTrue(backCalled)
        }

    @Test
    fun load_populates_fields_from_firebase_and_enables_save() =
        kotlinx.coroutines.test.runTest {
            createHunt(
                title = "T",
                description = "D",
                time = "1.5",
                distance = "2.0",
                difficulty = Difficulty.EASY,
                status = HuntStatus.FUN)
            advanceUntilIdle()
            val all = repository.getAllHunts()
            assertTrue(all.isNotEmpty())
            val id = all.first().uid
            val vm = EditHuntViewModel(repository)
            setContent(id, vm)

            // Wait for load to complete
            composeRule.waitUntil(timeoutMillis = 5_000) { vm.uiState.value.title == "T" }

            // Assert fields populated
            composeRule
                .onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TITLE)
                .assert(
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.EditableText, AnnotatedString("T")))
            composeRule
                .onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_DESCRIPTION)
                .assert(
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.EditableText, AnnotatedString("D")))
            composeRule
                .onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TIME)
                .assert(
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.EditableText, AnnotatedString("1.5")))
            composeRule
                .onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_DISTANCE)
                .assert(
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.EditableText, AnnotatedString("2.0")))

            // Save should be enabled for a valid loaded state
            composeRule.onNodeWithTag(HuntScreenTestTags.HUNT_SAVE).assertIsEnabled()

            // VM error should be null after successful load
            assertNull(vm.uiState.value.errorMsg)
        }

    @Test
    fun save_updates_repository_and_calls_onDone() =
        kotlinx.coroutines.test.runTest {
            createHunt(title = "Old", description = "Desc", time = "1.0", distance = "1.0")
            advanceUntilIdle()
            val all = repository.getAllHunts()
            assertTrue(all.isNotEmpty())
            val id = all.first().uid

            val vm = EditHuntViewModel(repository)
            var doneCalled = false
            setContent(id, vm, onDone = { doneCalled = true })

            // Wait for load to complete
            composeRule.waitUntil(timeoutMillis = 5_000) { vm.uiState.value.title == "Old" }

            // Change fields
            composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TITLE).apply {
                performTextClearance()
                performTextInput("New Title")
            }
            composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_DESCRIPTION).apply {
                performTextClearance()
                performTextInput("New Desc")
            }
            composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TIME).apply {
                performTextClearance()
                performTextInput("2.5")
            }
            composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_DISTANCE).apply {
                performTextClearance()
                performTextInput("4.2")
            }

            // Save
            composeRule.onNodeWithTag(HuntScreenTestTags.HUNT_SAVE).performClick()
            composeRule.waitForIdle()

            // Verify repository updated and onDone called
            val updated = repository.getHunt(id)
            assertEquals("New Title", updated.title)
            assertEquals("New Desc", updated.description)
            assertEquals(2.5, updated.time, 0.0)
            assertEquals(4.2, updated.distance, 0.0)
            assertEquals(FirebaseAuth.getInstance().currentUser?.uid, updated.authorId)

            composeRule.waitUntil(timeoutMillis = 5_000) { doneCalled }
            assertTrue(doneCalled)
            assertNull(vm.uiState.value.errorMsg)
        }

    // Helpers

    private fun createHunt(
        title: String = "T",
        description: String = "D",
        time: String = "1.5",
        distance: String = "2.0",
        difficulty: Difficulty = Difficulty.EASY,
        status: HuntStatus = HuntStatus.FUN,
        imageUri: Uri = Uri.parse("file://test-image.jpg")
    ) {
        val a = Location(0.0, 0.0, "Start")
        val m = Location(0.5, 0.5, "Mid")
        val b = Location(1.0, 1.0, "End")

        addVM.setTitle(title)
        addVM.setDescription(description)
        addVM.setTime(time)
        addVM.setDistance(distance)
        addVM.setDifficulty(difficulty)
        addVM.setStatus(status)
        addVM.updateMainImageUri(imageUri)
        addVM.setPoints(listOf(a, m, b))
        val ok = addVM.submit()
        assertTrue(ok)
    }
}
