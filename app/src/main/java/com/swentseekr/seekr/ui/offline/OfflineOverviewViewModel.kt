package com.swentseekr.seekr.ui.offline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.ui.overview.HuntUiState
import com.swentseekr.seekr.ui.overview.OverviewUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel backing the offline overview screen.
 *
 * This class mirrors the filtering and search behavior of the online OverviewViewModel, but
 * operates exclusively on a fixed, in-memory collection of hunts supplied at construction time.
 *
 * Responsibilities:
 * - Maintain UI state for offline overview
 * - Apply search, status, and difficulty filters
 * - Expose a reactive StateFlow for Compose UI consumption
 *
 * No network, repository, or persistence access occurs in this class.
 *
 * @param initialHunts Immutable list of hunts available offline.
 */
class OfflineOverviewViewModel(initialHunts: List<Hunt>) {

  private val _uiState = MutableStateFlow(OverviewUIState())
  val uiState: StateFlow<OverviewUIState> = _uiState.asStateFlow()

  private var huntItems: MutableList<HuntUiState> =
      initialHunts.map { HuntUiState(hunt = it) }.toMutableList()

  init {
    _uiState.value = _uiState.value.copy(hunts = huntItems)
  }

  var searchQuery by mutableStateOf("")
    private set

  /**
   * Updates the search query and reapplies all active filters.
   *
   * @param newSearch New search term entered by the user.
   */
  fun onSearchChange(newSearch: String) {
    searchQuery = newSearch
    _uiState.value = _uiState.value.copy(searchWord = newSearch)
    applyFilters()
  }

  /** Clears the current search term and resets the hunt list to show all hunts. */
  fun onClearSearch() {
    searchQuery = ""
    _uiState.value = _uiState.value.copy(searchWord = "", hunts = huntItems)
    applyFilters()
  }

  /**
   * Toggles the selected hunt status filter.
   *
   * Selecting the same status twice clears the filter.
   *
   * @param status Status to apply, or null to clear the filter.
   */
  fun onStatusFilterSelect(status: HuntStatus?) {
    val current = _uiState.value
    val newStatus = if (current.selectedStatus == status) null else status
    _uiState.value = current.copy(selectedStatus = newStatus)
    applyFilters()
  }

  /**
   * Toggles the selected hunt difficulty filter.
   *
   * Selecting the same difficulty twice clears the filter.
   *
   * @param difficulty Difficulty to apply, or null to clear the filter.
   */
  fun onDifficultyFilterSelect(difficulty: Difficulty?) {
    val current = _uiState.value
    val newDifficulty = if (current.selectedDifficulty == difficulty) null else difficulty
    _uiState.value = current.copy(selectedDifficulty = newDifficulty)
    applyFilters()
  }

  /** Applies the selected status and difficulty filters + search to the hunt list. */
  private fun applyFilters() {
    val currentState = _uiState.value
    val searchWord = currentState.searchWord
    val selectedStatus = currentState.selectedStatus
    val selectedDifficulty = currentState.selectedDifficulty

    if (selectedStatus == null && selectedDifficulty == null && searchWord.isEmpty()) {
      _uiState.value = currentState.copy(hunts = huntItems)
      return
    }

    val filtered =
        huntItems.filter { huntUiState ->
          val hunt = huntUiState.hunt

          val searchMatches =
              if (searchWord.isNotEmpty()) {
                hunt.title.contains(searchWord, ignoreCase = true)
              } else {
                true
              }

          val statusMatches = selectedStatus?.let { hunt.status == it } ?: true
          val difficultyMatches = selectedDifficulty?.let { hunt.difficulty == it } ?: true

          searchMatches && statusMatches && difficultyMatches
        }

    _uiState.value = currentState.copy(hunts = filtered)
  }
}
