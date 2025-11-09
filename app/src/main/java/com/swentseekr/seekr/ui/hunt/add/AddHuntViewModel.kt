package com.swentseekr.seekr.ui.hunt.add

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.ui.hunt.BaseHuntViewModel
import com.swentseekr.seekr.ui.hunt.HuntUIState

class AddHuntViewModel(repository: HuntsRepository = HuntRepositoryProvider.repository) :
    BaseHuntViewModel(repository) {

    var mainImageUri: Uri? = null
    var otherImagesUris: List<Uri> = emptyList()

    override fun buildHunt(state: HuntUIState): Hunt {
        val uid = repository.getNewUid()
        val authorId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

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
            mainImageUrl = "", // L'URL sera mis à jour après l'upload de l'image
            otherImagesUrls = emptyList(),
            reviewRate = state.reviewRate
        )
    }

    override suspend fun persist(hunt: Hunt) {
        // On envoie le Hunt + les images à Firebase
        repository.addHunt(
            hunt = hunt,
            mainImageUri = mainImageUri,
            otherImageUris = otherImagesUris
        )
    }
}
