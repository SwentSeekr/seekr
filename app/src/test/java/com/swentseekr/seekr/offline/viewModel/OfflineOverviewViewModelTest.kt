package com.swentseekr.seekr.offline.viewModel

import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.offline.OfflineOverviewViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfflineOverviewViewModelTest {

  private fun dummyLocation(): Location {
    return Location(latitude = 0.0, longitude = 0.0, name = "dummy", description = "dummy")
  }

  private fun fakeHunt(
      id: String,
      title: String,
      status: HuntStatus,
      difficulty: Difficulty
  ): Hunt {
    val loc = dummyLocation()
    return Hunt(
        uid = id,
        start = loc,
        end = loc,
        middlePoints = emptyList(),
        status = status,
        title = title,
        description = "",
        time = 0.0,
        distance = 0.0,
        difficulty = difficulty,
        authorId = "author-$id",
        otherImagesUrls = emptyList(),
        mainImageUrl = "",
        reviewRate = 0.0)
  }

  @Test
  fun initialState_containsAllHunts_andSearchIsEmpty() {
    val hunts =
        listOf(
            fakeHunt("1", "Alpha", HuntStatus.FUN, Difficulty.EASY),
            fakeHunt("2", "Beta", HuntStatus.DISCOVER, Difficulty.INTERMEDIATE))

    val vm = OfflineOverviewViewModel(hunts)

    val state = vm.uiState.value
    assertEquals(2, state.hunts.size)
    assertEquals("", state.searchWord)
    assertEquals("", vm.searchQuery)
    assertNull(state.selectedStatus)
    assertNull(state.selectedDifficulty)
  }

  @Test
  fun onSearchChange_updatesSearchQuery_andFiltersByTitle() {
    val hunts =
        listOf(
            fakeHunt("1", "My Hunt One", HuntStatus.FUN, Difficulty.EASY),
            fakeHunt("2", "Another Two", HuntStatus.FUN, Difficulty.INTERMEDIATE),
            fakeHunt("3", "Something Else", HuntStatus.SPORT, Difficulty.DIFFICULT))

    val vm = OfflineOverviewViewModel(hunts)

    // Trigger non-empty searchWord branch in applyFilters
    vm.onSearchChange("hunt")

    val stateAfter = vm.uiState.value
    assertEquals("hunt", vm.searchQuery)
    assertEquals("hunt", stateAfter.searchWord)

    // Only titles containing "hunt" (case-insensitive) should remain
    val remainingTitles = stateAfter.hunts.map { it.hunt.title }
    assertEquals(1, remainingTitles.size)
    assertEquals("My Hunt One", remainingTitles.first())
  }

  @Test
  fun onClearSearch_resetsSearchAndRestoresAllHunts() {
    val hunts =
        listOf(
            fakeHunt("1", "First", HuntStatus.FUN, Difficulty.EASY),
            fakeHunt("2", "Second", HuntStatus.DISCOVER, Difficulty.INTERMEDIATE))

    val vm = OfflineOverviewViewModel(hunts)

    // Put viewmodel into a filtered state first
    vm.onSearchChange("first")
    assertEquals(1, vm.uiState.value.hunts.size)

    // Now clear search – should reset searchWord + restore all hunts
    vm.onClearSearch()

    val stateAfter = vm.uiState.value
    assertEquals("", vm.searchQuery)
    assertEquals("", stateAfter.searchWord)
    assertEquals(2, stateAfter.hunts.size)
    // also hits the "no filters and empty searchWord" early return in applyFilters
  }

  @Test
  fun onStatusFilterSelect_togglesSameStatusOnAndOff() {
    val hunts =
        listOf(
            fakeHunt("1", "A", HuntStatus.FUN, Difficulty.EASY),
            fakeHunt("2", "B", HuntStatus.DISCOVER, Difficulty.INTERMEDIATE))

    val vm = OfflineOverviewViewModel(hunts)
    val status = HuntStatus.FUN

    // First select: selectedStatus becomes FUN
    vm.onStatusFilterSelect(status)
    val afterFirst = vm.uiState.value
    assertEquals(status, afterFirst.selectedStatus)

    // Second select with same status: should toggle back to null
    vm.onStatusFilterSelect(status)
    val afterSecond = vm.uiState.value
    assertNull(afterSecond.selectedStatus)
  }

  @Test
  fun onDifficultyFilterSelect_togglesSameDifficultyOnAndOff() {
    val hunts =
        listOf(
            fakeHunt("1", "A", HuntStatus.FUN, Difficulty.EASY),
            fakeHunt("2", "B", HuntStatus.DISCOVER, Difficulty.EASY),
            fakeHunt("3", "C", HuntStatus.SPORT, Difficulty.DIFFICULT))

    val vm = OfflineOverviewViewModel(hunts)
    val difficulty = Difficulty.EASY

    // First select: selectedDifficulty becomes EASY
    vm.onDifficultyFilterSelect(difficulty)
    val afterFirst = vm.uiState.value
    assertEquals(difficulty, afterFirst.selectedDifficulty)

    // Second select with same difficulty: should toggle back to null
    vm.onDifficultyFilterSelect(difficulty)
    val afterSecond = vm.uiState.value
    assertNull(afterSecond.selectedDifficulty)
  }

  @Test
  fun applyFilters_combinesSearch_status_andDifficulty() {
    val hunts =
        listOf(
            // Matches everything
            fakeHunt("1", "Magic Hunt", HuntStatus.FUN, Difficulty.EASY),
            // Wrong status
            fakeHunt("2", "Magic Hunt", HuntStatus.SPORT, Difficulty.EASY),
            // Wrong difficulty
            fakeHunt("3", "Magic Hunt", HuntStatus.FUN, Difficulty.DIFFICULT),
            // Wrong title
            fakeHunt("4", "Other Title", HuntStatus.FUN, Difficulty.EASY))

    val vm = OfflineOverviewViewModel(hunts)

    // Set search to non-empty to drive searchMatches branch
    vm.onSearchChange("magic")

    // Apply status filter
    vm.onStatusFilterSelect(HuntStatus.FUN)

    // Apply difficulty filter
    vm.onDifficultyFilterSelect(Difficulty.EASY)

    // Now all three filters must match, so only hunt "1" should remain
    val finalState = vm.uiState.value
    assertEquals(1, finalState.hunts.size)
    assertEquals("1", finalState.hunts.first().hunt.uid)
  }
}
