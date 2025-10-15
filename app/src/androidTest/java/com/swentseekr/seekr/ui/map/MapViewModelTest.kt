package com.swentseekr.seekr.ui.map

import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

private class FakeRepoSuccess(private val hunts: List<Hunt>) : HuntsRepository {
  override suspend fun addHunt(hunt: Hunt) = Unit

  override suspend fun getAllHunts(): List<Hunt> = hunts

  override suspend fun getHunt(uid: String): Hunt = hunts.first { it.uid == uid }

  override suspend fun editHunt(uid: String, updatedHunt: Hunt) = Unit

  override suspend fun deleteHunt(uid: String) = Unit

  override fun getNewUid(): String = "fake"
}

private class FakeRepoEmpty : HuntsRepository {
  override suspend fun addHunt(hunt: Hunt) = Unit

  override suspend fun getAllHunts(): List<Hunt> = emptyList()

  override suspend fun getHunt(uid: String): Hunt = error("nope")

  override suspend fun editHunt(uid: String, updatedHunt: Hunt) = Unit

  override suspend fun deleteHunt(uid: String) = Unit

  override fun getNewUid(): String = "fake"
}

private class FakeRepoThrows(private val message: String) : HuntsRepository {
  override suspend fun addHunt(hunt: Hunt) = Unit

  override suspend fun getAllHunts(): List<Hunt> = throw IllegalStateException(message)

  override suspend fun getHunt(uid: String): Hunt = throw IllegalStateException(message)

  override suspend fun editHunt(uid: String, updatedHunt: Hunt) = Unit

  override suspend fun deleteHunt(uid: String) = Unit

  override fun getNewUid(): String = "fake"
}

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {
  private fun sample(
      author: String = "A",
      uid: String = "1",
      lat: Double = 10.0,
      lng: Double = 20.0
  ) =
      Hunt(
          uid = uid,
          start = Location(lat, lng, "Start"),
          end = Location(lat + 0.5, lng + 0.5, "End"),
          middlePoints =
              listOf(Location(lat + 0.1, lng + 0.1, "M1"), Location(lat + 0.2, lng + 0.2, "M2")),
          status = HuntStatus.FUN,
          title = "Hunt $uid",
          description = "desc",
          time = 1.0,
          distance = 2.0,
          difficulty = Difficulty.EASY,
          authorId = "A",
          image = 0,
          reviewRate = 4.2)

  @Test
  fun initialStateLoadsHuntsAndTargetsFirstStart() = runTest {
    val hunts = listOf(sample(uid = "1", lat = 46.5, lng = 6.6), sample(uid = "2"))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    val state = vm.uiState.value
    assertEquals(2, state.hunts.size)
    // target must be the first hunt's start
    assertEquals(46.5, state.target.latitude, 0.0001)
    assertEquals(6.6, state.target.longitude, 0.0001)
    assertNull(state.errorMsg)
  }

  @Test
  fun initialStateWithEmptyRepoUsesLausanneFallback() = runTest {
    val vm = MapViewModel(repository = FakeRepoEmpty())
    val state = vm.uiState.value
    // Lausanne fallback from MapViewModel default path
    assertEquals(46.519962, state.target.latitude, 0.0001)
    assertEquals(6.633597, state.target.longitude, 0.0001)
    assertTrue(state.hunts.isEmpty())
  }

  @Test
  fun initialStateFailureSetsErrorMessage() = runTest {
    val vm = MapViewModel(repository = FakeRepoThrows("boom"))
    val state = vm.uiState.value
    assertNotNull(state.errorMsg)
    assertTrue(state.errorMsg!!.contains("Failed to load hunts"))
  }

  @Test
  fun clearErrorMsgRemovesError() = runTest {
    val vm = MapViewModel(repository = FakeRepoThrows("boom"))
    assertNotNull(vm.uiState.value.errorMsg)
    vm.clearErrorMsg()
    assertNull(vm.uiState.value.errorMsg)
  }

  @Test
  fun onMarkerClickSelectsHuntAndIsNotFocused() = runTest {
    val hunts = listOf(sample(uid = "1"), sample(uid = "2"))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))
    vm.onMarkerClick(hunts[1])

    val state = vm.uiState.value
    assertEquals("2", state.selectedHunt?.uid)
    assertFalse(state.isFocused)
  }

  @Test
  fun onViewHuntClickSetsFocusedTrue() = runTest {
    val hunts = listOf(sample(uid = "1"))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))
    vm.onMarkerClick(hunts[0])
    vm.onViewHuntClick()

    val state = vm.uiState.value
    assertTrue(state.isFocused)
    assertEquals("1", state.selectedHunt?.uid)
  }

  @Test
  fun onBackToAllHuntsClearsSelectionAndFocus() = runTest {
    val hunts = listOf(sample(uid = "1"))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))
    vm.onMarkerClick(hunts[0])
    vm.onViewHuntClick()
    vm.onBackToAllHunts()

    val state = vm.uiState.value
    assertFalse(state.isFocused)
    assertNull(state.selectedHunt)
  }

  @Test
  fun refreshUIStateReloadsFromRepository() = runTest {
    val hunts = listOf(sample(uid = "1"))
    val repo = FakeRepoSuccess(hunts)
    val vm = MapViewModel(repository = repo)

    val before = vm.uiState.value.hunts.size
    vm.refreshUIState()
    val after = vm.uiState.value.hunts.size
    assertEquals(before, after)
  }
}
