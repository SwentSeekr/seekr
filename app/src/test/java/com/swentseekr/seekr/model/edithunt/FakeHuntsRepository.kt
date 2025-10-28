package com.swentseekr.seekr.model.edithunt

import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntsRepository

class FakeHuntsRepository : HuntsRepository {
    private val hunts = mutableListOf<Hunt>()

    override fun getNewUid(): String = (hunts.size + 1).toString()

    override suspend fun getAllHunts(): List<Hunt> = hunts.toList()

    override suspend fun getHunt(huntID: String): Hunt =
        hunts.find { it.uid == huntID } ?: throw Exception("Hunt not found")

    override suspend fun addHunt(hunt: Hunt) {
        hunts.add(hunt)
    }

    override suspend fun editHunt(huntID: String, newValue: Hunt) {
        val index = hunts.indexOfFirst { it.uid == huntID }
        if (index != -1) hunts[index] = newValue else throw Exception("Hunt not found")
    }

    override suspend fun deleteHunt(huntID: String) {
        hunts.removeIf { it.uid == huntID }
    }
}
