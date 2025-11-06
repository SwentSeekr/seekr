package com.swentseekr.seekr.ui.overview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class representing the UI state for the Overview screen.
 *
 * @param hunts A list of hunts to be displayed.
 * @param searchWord The current search word entered by the user.
 * @param errorMsg An optional error message to be displayed to the user.
 */
data class OverviewUIState(
    val hunts: List<HuntUiState> = emptyList(),
    val searchWord: String = "",
    val errorMsg: String? = null,
    val selectedStatus: HuntStatus? = null,
    val selectedDifficulty: Difficulty? = null,
    val signedOut: Boolean = false
)
/**
 * Data class representing the UI state for a single Hunt item in the Overview screen.
 *
 * @param hunt The Hunt item.
 * @param isLiked A boolean indicating whether the hunt is liked by the user.
 * @param isAchieved A boolean indicating whether the hunt has been achieved by the user.
 */
data class HuntUiState(
    val hunt: Hunt,
    val isLiked: Boolean = false,
    val isAchieved: Boolean = false
)

/**
 * ViewModel for the Overview screen.
 *
 * Responsible for managing the UI state, by fetching and providing Hunt items via the
 * [HuntsRepository].
 *
 * @property huntRepository The repository used to fetch and manage Hunt items.
 */
class OverviewViewModel(
    private val repository: HuntsRepository = HuntRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(OverviewUIState())
  val uiState: StateFlow<OverviewUIState> = _uiState.asStateFlow()

  private var huntItems: MutableList<HuntUiState> = mutableListOf()

  init {
    loadHunts()
  }
  /** Refreshes the UI state by fetching all Hunt items from the repository. */
  fun refreshUIState() {
    loadHunts()
  }

  /** Fetches all hunts from the repository and updates the UI state. */
  private fun loadHunts() {
    viewModelScope.launch {
      try {
        val hunts = repository.getAllHunts()
        huntItems = hunts.map { HuntUiState(it) }.toMutableList()
        _uiState.value = _uiState.value.copy(hunts = huntItems)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = e.message)
      }
    }
  }

  /** Toggles the 'like' botton of a hunt item identified by [huntID]. */
  fun onLikeClick(huntID: String) {
    val index = huntItems.indexOfFirst { it.hunt.uid == huntID }
    if (index != -1) {
      val currentHuntUiState = huntItems[index]
      val updatedHuntUiState = currentHuntUiState.copy(isLiked = !currentHuntUiState.isLiked)
      huntItems[index] = updatedHuntUiState
      _uiState.value = _uiState.value.copy(hunts = huntItems)
    }
  }

  var searchQuery by mutableStateOf("")
    private set

  /** Updates the search word and filters the hunts based on the new search term [newSearch]. */
  fun onSearchChange(newSearch: String) {
    searchQuery = newSearch
    if (newSearch != "") {
      _uiState.value = _uiState.value.copy(searchWord = newSearch)
      applyFilters()
    }
  }
  /** Clears the current search term and resets the hunt list to show all hunts. */
  fun onClearSearch() {
    searchQuery = ""
    _uiState.value = _uiState.value.copy(searchWord = "", hunts = huntItems)
    applyFilters()
  }

  /** Updates the selected status filter and applies the filter to the hunt list. */
  fun onStatusFilterSelect(status: HuntStatus?) {
    val newStatus = if (_uiState.value.selectedStatus == status) null else status
    _uiState.value = _uiState.value.copy(selectedStatus = newStatus)
    applyFilters()
  }

  /** Updates the selected difficulty filter and applies the filter to the hunt list. */
  fun onDifficultyFilterSelect(difficulty: Difficulty?) {
    val newDifficulty = if (_uiState.value.selectedDifficulty == difficulty) null else difficulty
    _uiState.value = _uiState.value.copy(selectedDifficulty = newDifficulty)
    applyFilters()
  }

  /** Applies the selected status and difficulty filters to the hunt list. */
  private fun applyFilters() {
    val currentState = _uiState.value
    val searchQuery = currentState.searchWord
    val selectedStatus = currentState.selectedStatus
    val selectedDifficulty = currentState.selectedDifficulty

    if (selectedStatus == null && selectedDifficulty == null && searchQuery.isEmpty()) {
      _uiState.value = currentState.copy(hunts = huntItems)
      return
    }
    val filtered =
        huntItems.filter { huntUiState ->
          val hunt = huntUiState.hunt
          val searchMatches =
              if (searchQuery.isNotEmpty()) {
                hunt.title.contains(searchQuery, ignoreCase = true)
              } else {
                true
              }
          val statusMatches = selectedStatus?.let { hunt.status == it } ?: true
          val difficultyMatches = selectedDifficulty?.let { hunt.difficulty == it } ?: true
          statusMatches && difficultyMatches && searchMatches
        }

    _uiState.value = currentState.copy(hunts = filtered)
  }
}
