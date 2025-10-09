package com.swentseekr.seekr.model.hunt

/** Represents a repository that manages a local list of hunts. */
class HuntsRepositoryLocal : HuntsRepository {
  private val hunts = mutableListOf<Hunt>()
  private var id = 0

  override fun getNewUid(): String {
    return (id++).toString()
  }

  override suspend fun getAllHunts(): List<Hunt> {
    return hunts.toList()
  }

  override suspend fun getHunt(huntID: String): Hunt {
    for (i in hunts.indices) {
      if (hunts[i].uid == huntID) {
        return hunts[i]
      }
    }
    throw IllegalArgumentException("Hunt with ID $huntID not found")
  }

  override suspend fun addHunt(hunt: Hunt) {
    hunts.add(hunt)
  }

  override suspend fun editHunt(huntID: String, newValue: Hunt) {
    for (i in hunts.indices) {
      if (hunts[i].uid == huntID) {
        hunts[i] = newValue
        return
      }
    }
    throw IllegalArgumentException("Hunt with ID $huntID not found")
  }

  override suspend fun deleteHunt(huntID: String) {
    val wasRemoved = hunts.removeIf { it.uid == huntID }
    if (!wasRemoved) {
      throw IllegalArgumentException("Hunt with ID $huntID not found")
    }
  }
}
