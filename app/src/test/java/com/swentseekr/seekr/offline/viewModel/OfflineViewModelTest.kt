package com.swentseekr.seekr.ui.offline

import com.swentseekr.seekr.model.profile.mockProfileData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineViewModelTest {

  @Test
  fun `initial state uses provided profile and defaults to MY_HUNTS`() {
    // Given
    val profile = mockProfileData()

    // When
    val viewModel = OfflineViewModel(profile)

    // Then
    assertEquals(profile, viewModel.profile)
    assertEquals(OfflineProfileTab.MY_HUNTS, viewModel.selectedTab)

    // MY_HUNTS should be shown by default
    assertEquals(profile.myHunts, viewModel.huntsToDisplay)

    // Counts and ratings should match the profile
    assertEquals(profile.doneHunts.size, viewModel.doneHuntsCount)
    assertEquals(profile.author.reviewRate, viewModel.reviewRate, OfflineConstants.DEFAULT_DOUBLE)
    assertEquals(profile.author.sportRate, viewModel.sportRate, OfflineConstants.DEFAULT_DOUBLE)
  }

  @Test
  fun `selectTab switches huntsToDisplay between my, done, and liked hunts`() {
    // Given
    val profile = mockProfileData()
    val viewModel = OfflineViewModel(profile)

    // When & Then – MY_HUNTS
    viewModel.selectTab(OfflineProfileTab.MY_HUNTS)
    assertEquals(profile.myHunts, viewModel.huntsToDisplay)

    // When & Then – DONE_HUNTS
    viewModel.selectTab(OfflineProfileTab.DONE_HUNTS)
    assertEquals(profile.doneHunts, viewModel.huntsToDisplay)

    // When & Then – LIKED_HUNTS
    viewModel.selectTab(OfflineProfileTab.LIKED_HUNTS)
    assertEquals(profile.likedHunts, viewModel.huntsToDisplay)
  }

  @Test
  fun `null initial profile exposes empty-safe defaults`() {
    // Given
    val viewModel = OfflineViewModel(initialProfile = null)

    // Then
    assertNull(viewModel.profile)
    assertEquals(OfflineProfileTab.MY_HUNTS, viewModel.selectedTab)

    // All derived values should be safe defaults
    assertEquals(OfflineConstants.DEFAULT_INT, viewModel.doneHuntsCount)
    assertEquals(
        OfflineConstants.DEFAULT_DOUBLE, viewModel.reviewRate, OfflineConstants.DEFAULT_DOUBLE)
    assertEquals(
        OfflineConstants.DEFAULT_DOUBLE, viewModel.sportRate, OfflineConstants.DEFAULT_DOUBLE)
    assertTrue(viewModel.huntsToDisplay.isEmpty())
  }

  @Test
  fun `selectTab still updates selectedTab even with null profile`() {
    // Given
    val viewModel = OfflineViewModel(initialProfile = null)

    // When
    viewModel.selectTab(OfflineProfileTab.LIKED_HUNTS)

    // Then
    assertEquals(OfflineProfileTab.LIKED_HUNTS, viewModel.selectedTab)
    // No profile -> still no hunts
    assertTrue(viewModel.huntsToDisplay.isEmpty())
  }
}
