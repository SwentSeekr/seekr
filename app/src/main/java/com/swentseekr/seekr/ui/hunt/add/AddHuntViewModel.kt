package com.swentseekr.seekr.ui.hunt.add

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.ui.hunt.BaseHuntViewModel
import com.swentseekr.seekr.ui.hunt.HuntUIState

/**
 * ViewModel responsible for managing the state and actions
 * related to adding a new hunt.
 *
 * This ViewModel extends [BaseHuntViewModel] and provides
 * logic for building a [Hunt] object and persisting it
 * to the repository.
 *
 * @param repository Repository used to persist hunts. Defaults
 *   to the shared instance provided by [HuntRepositoryProvider].
 */
class AddHuntViewModel(repository: HuntsRepository = HuntRepositoryProvider.repository) :
    BaseHuntViewModel(repository) {

  var mainImageUri: Uri? = null
  val UNKNOWN = "unknown"

    /**
     * Builds a [Hunt] object from the given [HuntUIState].
     *
     * @param state The UI state containing user input for the hunt.
     * @return A new [Hunt] object ready to be persisted.
     */
  override fun buildHunt(state: HuntUIState): Hunt {
    val uid = repository.getNewUid()
    val authorId = FirebaseAuth.getInstance().currentUser?.uid ?: UNKNOWN

    return Hunt(
        uid = uid,
        start = state.points.first(),
        end = state.points.last(),
        middlePoints = state.points.drop(1).dropLast(1),
        status = state.status!!,
        title = state.title,
        description = state.description,
        time = state.time.toDouble(),
        distance = state.distance.toDouble(),
        difficulty = state.difficulty!!,
        authorId = authorId,
        mainImageUrl = "",
        otherImagesUrls = emptyList(),
        reviewRate = state.reviewRate)
  }

    /**
     * Persists the given [Hunt] to the repository, including
     * uploading the main and other images if present.
     *
     * @param hunt The [Hunt] object to persist.
     */
  override suspend fun persist(hunt: Hunt) {
    repository.addHunt(hunt = hunt, mainImageUri = mainImageUri, otherImageUris = otherImagesUris)
  }
}
