package com.swentseekr.seekr.model.hunt

import android.net.Uri

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

  override suspend fun getAllMyHunts(authorID: String): List<Hunt> {
    return hunts.filter { it.authorId == authorID }
  }

  override suspend fun getHunt(huntID: String): Hunt {
    return hunts.find { it.uid == huntID }
        ?: throw IllegalArgumentException("Hunt with ID $huntID is not found")
  }

  override suspend fun addHunt(hunt: Hunt, mainImageUri: Uri?, otherImageUris: List<Uri>) {
    // Ici, comme c’est local, on ne gère pas les images.
    hunts.add(hunt)
  }

  override suspend fun editHunt(huntID: String, newValue: Hunt) {
    val index = hunts.indexOfFirst { it.uid == huntID }
    if (index != -1) {
      hunts[index] = newValue
    } else {
      throw IllegalArgumentException("Hunt with ID $huntID is not found")
    }
  }

  override suspend fun deleteHunt(huntID: String) {
    val wasRemoved = hunts.removeIf { it.uid == huntID }
    if (!wasRemoved) {
      throw IllegalArgumentException("Hunt with ID $huntID is not found")
    }
  }
}
