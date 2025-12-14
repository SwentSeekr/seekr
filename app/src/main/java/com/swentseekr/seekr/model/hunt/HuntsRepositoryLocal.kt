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
        ?: throw IllegalArgumentException(
            "${HuntsRepositoryLocalConstantsString.HUNT_ID} $huntID ${HuntsRepositoryLocalConstantsString.NOT_FOUND}")
  }

  override suspend fun addHunt(hunt: Hunt, mainImageUri: Uri?, otherImageUris: List<Uri>) {
    hunts.add(hunt)
  }

  override suspend fun editHunt(
      huntID: String,
      newValue: Hunt,
      _mainImageUri: Uri?,
      _addedOtherImages: List<Uri>,
      _removedOtherImages: List<String>,
      _removedMainImageUrl: String?
  ) {
    val index = hunts.indexOfFirst { it.uid == huntID }
    if (index != -1) {
      hunts[index] = newValue
    } else {
      throw IllegalArgumentException(
          "${HuntsRepositoryLocalConstantsString.HUNT_ID} $huntID ${HuntsRepositoryLocalConstantsString.NOT_FOUND}")
    }
  }

  override suspend fun deleteHunt(huntID: String) {
    val wasRemoved = hunts.removeIf { it.uid == huntID }
    if (!wasRemoved) {
      throw IllegalArgumentException(
          "${HuntsRepositoryLocalConstantsString.HUNT_ID} $huntID ${HuntsRepositoryLocalConstantsString.NOT_FOUND}")
    }
  }
}
