package com.swentseekr.seekr.ui.hunt.add

import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.ui.hunt.BaseHuntViewModel
import com.swentseekr.seekr.ui.hunt.HuntUIState

class AddHuntViewModel(repository: HuntsRepository = HuntRepositoryProvider.repository) :
    BaseHuntViewModel(repository) {

  override fun buildHunt(state: HuntUIState): Hunt {

    val uid = repository.getNewUid()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
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
        userId = userId,
        image = state.image,
        reviewRate = state.reviewRate)
  }

  override suspend fun persist(hunt: Hunt) {
    repository.addHunt(hunt)
  }
}
