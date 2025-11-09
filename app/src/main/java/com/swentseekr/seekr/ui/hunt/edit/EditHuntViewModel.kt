package com.swentseekr.seekr.ui.hunt.edit

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.ui.hunt.BaseHuntViewModel
import com.swentseekr.seekr.ui.hunt.HuntUIState

class EditHuntViewModel(
    repository: HuntsRepository = HuntRepositoryProvider.repository
) : BaseHuntViewModel(repository) {

    private var huntId: String? = null

    var mainImageUri: Uri? = null

    suspend fun load(id: String) {
        try {
            val hunt = repository.getHunt(id)
            _uiState.value = _uiState.value.copy(
                title = hunt.title,
                description = hunt.description,
                points = listOf(hunt.start) + hunt.middlePoints + listOf(hunt.end),
                time = hunt.time.toString(),
                distance = hunt.distance.toString(),
                difficulty = hunt.difficulty,
                status = hunt.status,
                mainImageUrl = hunt.mainImageUrl,
                reviewRate = hunt.reviewRate
            )
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
            otherImagesUrls = emptyList(),
            reviewRate = state.reviewRate
        )
    }

    override suspend fun persist(hunt: Hunt) {
        if (mainImageUri != null) {
            repository.addHunt(
                hunt = hunt,
                mainImageUri = mainImageUri,
                otherImageUris = emptyList()
            )
        } else {
            repository.editHunt(hunt.uid, hunt)
        }
    }
}
