package com.swentseekr.seekr.ui.offline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.profile.Profile

/**
 * ViewModel-like state holder for offline profile UI.
 *
 * This class exposes a read-only [profile] and derived values used to render the offline profile
 * screen, including:
 * - The currently selected tab ([selectedTab])
 * - Aggregated statistics (e.g. [doneHuntsCount], [reviewRate], [sportRate])
 * - The list of hunts to display for the active tab ([huntsToDisplay])
 *
 * It does not perform any I/O or business logic; it is intended to wrap an already-cached [Profile]
 * instance (e.g. loaded from [com.swentseekr.seekr.offline.cache.ProfileCache]) and provide a
 * simple, Compose-friendly state API.
 *
 * @param initialProfile The cached profile to display when offline. May be `null` if no profile is
 *   available, in which case safe default values are used.
 */
class OfflineViewModel(initialProfile: Profile?) {

  /**
   * The cached profile used to render offline user information.
   *
   * This value is immutable from the outside and is expected to be the same snapshot that was
   * stored in the offline cache. When `null`, all derived properties fall back to the defaults
   * defined in [OfflineConstants].
   */
  var profile by mutableStateOf(initialProfile)
    private set

  /**
   * Currently selected offline profile tab.
   *
   * Controls which subset of hunts is exposed through [huntsToDisplay]. Defaults to
   * [OfflineProfileTab.MY_HUNTS].
   */
  var selectedTab by mutableStateOf(OfflineProfileTab.MY_HUNTS)
    private set

  /**
   * Total number of hunts the user has completed.
   *
   * Falls back to [OfflineConstants.DEFAULT_INT] when [profile] is `null`.
   */
  val doneHuntsCount: Int
    get() = profile?.doneHunts?.size ?: OfflineConstants.DEFAULT_INT

  /**
   * Aggregated review rating for the user in offline mode.
   *
   * This is expected to be precomputed and stored on [Profile.author.reviewRate] by the online
   * profile logic before being cached. When no profile is available,
   * [OfflineConstants.DEFAULT_DOUBLE] is returned.
   */
  val reviewRate: Double
    get() = profile?.author?.reviewRate ?: OfflineConstants.DEFAULT_DOUBLE

  /**
   * Aggregated sport rating for the user in offline mode.
   *
   * This is expected to be precomputed and stored on [Profile.author.sportRate] by the online
   * profile logic before being cached. When no profile is available,
   * [OfflineConstants.DEFAULT_DOUBLE] is returned.
   */
  val sportRate: Double
    get() = profile?.author?.sportRate ?: OfflineConstants.DEFAULT_DOUBLE

  /**
   * List of hunts to display for the current [selectedTab].
   * - [OfflineProfileTab.MY_HUNTS] → [Profile.myHunts]
   * - [OfflineProfileTab.DONE_HUNTS] → [Profile.doneHunts]
   * - [OfflineProfileTab.LIKED_HUNTS] → [Profile.likedHunts]
   *
   * When [profile] is `null`, this returns [OfflineConstants.DEFAULT_HUNT_LIST].
   */
  val huntsToDisplay: List<Hunt>
    get() =
        when (selectedTab) {
          OfflineProfileTab.MY_HUNTS -> profile?.myHunts ?: OfflineConstants.DEFAULT_HUNT_LIST
          OfflineProfileTab.DONE_HUNTS -> profile?.doneHunts ?: OfflineConstants.DEFAULT_HUNT_LIST
          OfflineProfileTab.LIKED_HUNTS -> profile?.likedHunts ?: OfflineConstants.DEFAULT_HUNT_LIST
        }

  /**
   * Updates the currently selected offline profile tab.
   *
   * This triggers a recomposition of any composables observing [selectedTab] or [huntsToDisplay].
   *
   * @param tab The tab that should become active.
   */
  fun selectTab(tab: OfflineProfileTab) {
    selectedTab = tab
  }
}
