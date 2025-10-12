package com.swentseekr.seekr.ui.overview

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
    val selectedDifficulty: Difficulty? = null
)
/**
 * Data class representing the UI state for a single Hunt item in the Overview screen.
 *
 * @param hunt The Hunt item.
 * @param isLiked A boolean indicating whether the hunt is liked by the user.
 * @param isAchived A boolean indicating whether the hunt has been achieved by the user.
 */
data class HuntUiState(
    val hunt: Hunt,
    val isLiked: Boolean = false,
    val isAchived: Boolean = false
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
        // _uiState.value = hunts
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

  /**
   * Handles the click event on a hunt item identified by [huntID]. Navigate to the card overview
   */
  fun onHuntClick(huntID: String) {
    // TODO: Navigate to Hunt Detail Screen
  }

  /** Updates the search word and filters the hunts based on the new search term [newSearch]. */
  fun onSearchChange(newSearch: String) {
    if (newSearch != "") {
      _uiState.value = _uiState.value.copy(searchWord = newSearch)
      // filter the hunts based on the word searched
      val filteredHunts =
          huntItems.filter {
            it.hunt.title.contains(newSearch, ignoreCase = true) ||
                it.hunt.description.contains(newSearch, ignoreCase = true) ||
                it.hunt.status.toString().contains(newSearch, ignoreCase = true) ||
                it.hunt.difficulty.toString().contains(newSearch, ignoreCase = true) ||
                it.hunt.author.pseudonym.contains(newSearch, ignoreCase = true)
          }
      _uiState.value = _uiState.value.copy(hunts = filteredHunts)
    }
  }
  /** Clears the current search term and resets the hunt list to show all hunts. */
  fun onClearSearch() {
    _uiState.value = _uiState.value.copy(searchWord = "", hunts = huntItems)
  }

  private fun applyFilters() {
    val currentState = _uiState.value
    val filtered =
        huntItems.filter { huntUiState ->
          val statusMatches =
              currentState.selectedStatus?.let { huntUiState.hunt.status == it } ?: true

          val difficultyMatches =
              currentState.selectedDifficulty?.let { huntUiState.hunt.difficulty == it } ?: true

          statusMatches && difficultyMatches
        }

    _uiState.value = currentState.copy(hunts = filtered)
  }

  /** Filters the hunts based on the selected [status]. */
  fun onStatusFilterSelect(status: HuntStatus?) {
    // change color botton in the UI OverviewScreen use FilterBotton
    val filteredHunts =
        huntItems.filter {
          it.hunt.status.toString().contains(status.toString(), ignoreCase = true)
        }
    _uiState.value = _uiState.value.copy(hunts = filteredHunts)
    applyFilters()
  }
  /** Filters the hunts based on the selected [difficulty]. */
  fun onDifficultyFilterSelect(difficulty: Difficulty?) {
    // change color botton in the UI OverviewScreen use FilterBotton
    val filteredHunts =
        huntItems.filter {
          it.hunt.difficulty.toString().contains(difficulty.toString(), ignoreCase = true)
        }
    _uiState.value = _uiState.value.copy(hunts = filteredHunts)
    applyFilters()
  }
  /** Filters the hunts to show only those that have been achieved by the user. */
  fun onAchivedClick() {
    //
    val filteredHunts = huntItems.filter { it.isAchived }
    _uiState.value = _uiState.value.copy(hunts = filteredHunts)
  }

  /** Handles the click event on the icon marker to navigate to the map screen. */
  fun onIconMarkerClick() {
    // TODO: Navigate to Map Screen
  }
}
