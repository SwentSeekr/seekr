package com.swentseekr.seekr.ui.hunt.edit

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.ui.hunt.BaseHuntViewModel
import com.swentseekr.seekr.ui.hunt.HuntUIState

class EditHuntViewModel(repository: HuntsRepository = HuntRepositoryProvider.repository) :
    BaseHuntViewModel(repository) {

  private var huntId: String? = null

  var mainImageUri: Uri? = null

  // Images existantes à supprimer de Firebase Storage
  private val pendingDeletionUrls = mutableListOf<String>()

  suspend fun load(id: String) {
    try {
      val hunt = repository.getHunt(id)

      // Initialise l'état UI avec les données existantes
      _uiState.value =
          _uiState.value.copy(
              title = hunt.title,
              description = hunt.description,
              points = listOf(hunt.start) + hunt.middlePoints + listOf(hunt.end),
              time = hunt.time.toString(),
              distance = hunt.distance.toString(),
              difficulty = hunt.difficulty,
              status = hunt.status,
              mainImageUrl = hunt.mainImageUrl,
              otherImagesUrls = hunt.otherImagesUrls, // <-- IMPORTANT
              reviewRate = hunt.reviewRate)

      huntId = id
    } catch (e: Exception) {
      huntId = null
      setErrorMsg("Failed to load hunt: ${e.message}")
    }
  }

  override fun buildHunt(state: HuntUIState): Hunt {
    val id = requireNotNull(huntId) { "No hunt loaded to edit." }
    val authorId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

    return Hunt(
        uid = id,
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
        mainImageUrl = state.mainImageUrl,
        otherImagesUrls = state.otherImagesUrls, // sera mis à jour après upload
        reviewRate = state.reviewRate)
  }

  /**
   * Supprimer une image déjà stockée dans Firebase (retire de l’état et marque pour suppression)
   */
  fun removeExistingOtherImage(url: String) {
    pendingDeletionUrls += url

    val newList = _uiState.value.otherImagesUrls - url
    _uiState.value = _uiState.value.copy(otherImagesUrls = newList)
  }

  override suspend fun persist(hunt: Hunt) {
    val id = requireNotNull(huntId)

    repository.editHunt(
        huntID = id,
        newValue = hunt,
        mainImageUri = mainImageUri,
        addedOtherImages = otherImagesUris,
        removedOtherImages = pendingDeletionUrls)
  }
}
