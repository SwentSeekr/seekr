package com.swentseekr.seekr.model.edithunt

import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.ui.edithunt.EditHuntViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After

@OptIn(ExperimentalCoroutinesApi::class)
class EditHuntViewModelTest {
    companion object {
        private const val SAMPLE_HUNT_UID = "1"
        private const val SAMPLE_HUNT_TITLE = "Test Hunt"
        private const val UPDATED_HUNT_TITLE = "Updated Hunt"
        private const val IMAGE_ID = 5
        private val START_LOCATION = Location(0.0, 0.0, "Start")
        private val END_LOCATION = Location(1.0, 1.0, "End")
    }

    private lateinit var viewModel: EditHuntViewModel
    private lateinit var repo: FakeHuntsRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        repo = FakeHuntsRepository()
        viewModel = EditHuntViewModel(repo)

        val sampleHunt = createHunt(uid = SAMPLE_HUNT_UID, title = SAMPLE_HUNT_TITLE)
        testScope.runTest { repo.addHunt(sampleHunt) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun load_hunt_sets_ui_state_correctly() = runTest {
        viewModel.loadHunt(SAMPLE_HUNT_UID)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SAMPLE_HUNT_UID, state.uid)
        assertEquals(SAMPLE_HUNT_TITLE, state.title)
        assertEquals("Desc $SAMPLE_HUNT_TITLE", state.description)
        assertEquals(2, state.points.size)
        assertEquals(Difficulty.EASY, state.difficulty)
        assertEquals(HuntStatus.FUN, state.status)
    }
    @Test
    fun edit_hunt_updates_repository() = runTest {
        viewModel.loadHunt(SAMPLE_HUNT_UID)
        advanceUntilIdle()
        viewModel.setTitle(UPDATED_HUNT_TITLE)
        viewModel.editHunt()
        advanceUntilIdle()


        val updatedHunt = repo.getAllHunts().first { it.uid == SAMPLE_HUNT_UID}
        assertEquals(UPDATED_HUNT_TITLE, updatedHunt.title)
    }

    @Test
    fun invalid_data_sets_error_message() = runTest {
        viewModel.loadHunt(SAMPLE_HUNT_UID)
        advanceUntilIdle()
        viewModel.setTitle("")
        viewModel.editHunt()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMsg)
        assertFalse(state.saveSuccessful)
    }

    @Test
    fun set_points_with_less_than_two_returns_false() = runTest {
        val result = viewModel.setPoints(listOf(START_LOCATION))
        val state = viewModel.uiState.value

        assertFalse(result)
        assertNotNull(state.errorMsg)
    }

    @Test
    fun set_points_with_two_or_more_returns_true() = runTest {
        val points = listOf(
          START_LOCATION,
           END_LOCATION
        )
        val result = viewModel.setPoints(points)
        val state = viewModel.uiState.value

        assertTrue(result)
        assertEquals(2, state.points.size)
        assertNull(state.errorMsg)
    }

    @Test
    fun set_title_sets_invalid_title_message_correctly() {
        viewModel.setTitle("")
        val state = viewModel.uiState.value
        assertEquals("Title cannot be empty", state.invalidTitleMsg)
    }

    @Test
    fun set_description_sets_invalid_description_message_correctly() {
        viewModel.setDescription("")
        val state = viewModel.uiState.value
        assertEquals("Description cannot be empty", state.invalidDescriptionMsg)
    }

    @Test
    fun set_time_sets_invalid_time_message_correctly() {
        viewModel.setTime("abc")
        val state = viewModel.uiState.value
        assertEquals("Invalid time format", state.invalidTimeMsg)
    }

    @Test
    fun set_distance_sets_invalid_distance_message_correctly() {
        viewModel.setDistance("xyz")
        val state = viewModel.uiState.value
        assertEquals("Invalid distance format", state.invalidDistanceMsg)
    }

    @Test
    fun set_difficulty_updates_state() {
        viewModel.setDifficulty(Difficulty.INTERMEDIATE)
        val state = viewModel.uiState.value
        assertEquals(Difficulty.INTERMEDIATE, state.difficulty)
    }

    @Test
    fun set_status_updates_state() {
        viewModel.setStatus(HuntStatus.DISCOVER)
        val state = viewModel.uiState.value
        assertEquals(HuntStatus.DISCOVER, state.status)
    }

    @Test
    fun set_image_updates_state() {
        viewModel.setImage(IMAGE_ID)
        val state = viewModel.uiState.value
        assertEquals(IMAGE_ID, state.image)
    }

    @Test
    fun set_is_selecting_points_updates_state() {
        viewModel.setIsSelectingPoints(true)
        val state = viewModel.uiState.value
        assertTrue(state.isSelectingPoints)
    }
}