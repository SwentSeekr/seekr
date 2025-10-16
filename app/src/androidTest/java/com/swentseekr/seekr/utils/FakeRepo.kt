package com.swentseekr.seekr.utils

import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntsRepository

class FakeRepoSuccess(private val hunts: List<Hunt>) : HuntsRepository {
    override suspend fun addHunt(hunt: Hunt) = Unit

    override suspend fun getAllHunts(): List<Hunt> = hunts

    override suspend fun getHunt(uid: String): Hunt = hunts.first { it.uid == uid }

    override suspend fun editHunt(uid: String, updatedHunt: Hunt) = Unit

    override suspend fun deleteHunt(uid: String) = Unit

    override fun getNewUid(): String = "fake"
}

class FakeRepoEmpty : HuntsRepository {
    override suspend fun addHunt(hunt: Hunt) = Unit

    override suspend fun getAllHunts(): List<Hunt> = emptyList()

    override suspend fun getHunt(uid: String): Hunt = error("nope")

    override suspend fun editHunt(uid: String, updatedHunt: Hunt) = Unit

    override suspend fun deleteHunt(uid: String) = Unit

    override fun getNewUid(): String = "fake"
}

class FakeRepoThrows(private val message: String) : HuntsRepository {
    override suspend fun addHunt(hunt: Hunt) = Unit

    override suspend fun getAllHunts(): List<Hunt> = throw IllegalStateException(message)

    override suspend fun getHunt(uid: String): Hunt = throw IllegalStateException(message)

    override suspend fun editHunt(uid: String, updatedHunt: Hunt) = Unit

    override suspend fun deleteHunt(uid: String) = Unit

    override fun getNewUid(): String = "fake"
}