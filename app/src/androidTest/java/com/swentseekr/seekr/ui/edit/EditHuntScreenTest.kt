package com.swentseekr.seekr.ui.edit

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestore
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.HuntScreenTestTags
import com.swentseekr.seekr.ui.hunt.add.AddHuntViewModel
import com.swentseekr.seekr.ui.hunt.edit.EditHuntScreen
import com.swentseekr.seekr.ui.hunt.edit.EditHuntViewModel
import com.swentseekr.seekr.utils.FakeHuntsImageRepository
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import com.swentseekr.seekr.utils.FirebaseTestEnvironment.clearEmulatorData
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditHuntScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var repository: HuntsRepository
  private lateinit var addVM: AddHuntViewModel

  @Before
  fun setUp() = runBlocking {
    FirebaseTestEnvironment.setup()
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      clearEmulatorData()
    }

    FirebaseAuth.getInstance().signInAnonymously().await()

    // Using fake to avoid Firebase Storage calls
    val db = FirebaseFirestore.getInstance()
    val fakeImageRepo = FakeHuntsImageRepository()
    HuntRepositoryProvider.repository = HuntsRepositoryFirestore(db, fakeImageRepo)

    repository = HuntRepositoryProvider.repository
    addVM = AddHuntViewModel(repository)
  }

  @After
  fun tearDown() = runBlocking {
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      clearEmulatorData()
    }
    FirebaseAuth.getInstance().signOut()
  }

  private fun setContent(
      huntId: String,
      vm: EditHuntViewModel,
      onGoBack: () -> Unit = {},
      onDone: () -> Unit = {}
  ) {
    composeRule.setContent {
      MaterialTheme {
        EditHuntScreen(
            huntId = huntId, editHuntViewModel = vm, onGoBack = onGoBack, onDone = onDone)
      }
    }
  }

  @Test
  fun back_navigates_from_edit_screen() = runBlocking {
    createHunt()
    val all = repository.getAllHunts()
    assertTrue(all.isNotEmpty())
    val id = all.first().uid
    val vm = EditHuntViewModel(repository)
    var backCalled = false
    setContent(id, vm, onGoBack = { backCalled = true })

    // Wait for the hunt to be loaded
    composeRule.waitUntil(timeoutMillis = 5_000) { vm.uiState.value.title.isNotEmpty() }

    composeRule.onNodeWithContentDescription("Back").performClick()
    assertTrue(backCalled)
  }

  @Test
  fun load_populates_fields_from_firebase_and_enables_save() = runBlocking {
    createHunt(
        title = "T",
        description = "D",
        time = "1.5",
        distance = "2.0",
        difficulty = Difficulty.EASY,
        status = HuntStatus.FUN)

    val all = repository.getAllHunts()
    assertTrue(all.isNotEmpty())
    val id = all.first().uid
    val vm = EditHuntViewModel(repository)
    setContent(id, vm)

    composeRule.waitUntil(timeoutMillis = 5_000) { vm.uiState.value.title == "T" }

    composeRule
        .onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TITLE)
        .assert(
            SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("T")))
    composeRule
        .onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_DESCRIPTION)
        .assert(
            SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("D")))
    composeRule
        .onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TIME)
        .assert(
            SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("1.5")))
    composeRule
        .onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_DISTANCE)
        .assert(
            SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("2.0")))

    composeRule.onNodeWithTag(HuntScreenTestTags.HUNT_SAVE).assertIsEnabled()
    assertNull(vm.uiState.value.errorMsg)
  }

  @Test
  fun save_updates_repository_and_calls_onDone() = runBlocking {
    createHunt(title = "Old", description = "Desc", time = "1.0", distance = "1.0")

    val all = repository.getAllHunts()
    assertTrue(all.isNotEmpty())
    val id = all.first().uid

    val vm = EditHuntViewModel(repository)
    var doneCalled = false
    setContent(id, vm, onDone = { doneCalled = true })

    composeRule.waitUntil(timeoutMillis = 5_000) { vm.uiState.value.title == "Old" }

    composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TITLE).apply {
      performTextClearance()
      performTextInput("New Title")
    }
    composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_DESCRIPTION).apply {
      performTextClearance()
      performTextInput("New Desc")
    }
    composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TIME).apply {
      performTextClearance()
      performTextInput("2.5")
    }
    composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_DISTANCE).apply {
      performTextClearance()
      performTextInput("4.2")
    }

    // click save
    composeRule.onNodeWithTag(HuntScreenTestTags.HUNT_SAVE).performClick()

    composeRule.runOnIdle { runBlocking { vm.submit() } }

    // wait for UI to pick up saveSuccessful -> onDone() call
    composeRule.waitUntil(timeoutMillis = 5_000) { doneCalled }

    val updated = repository.getHunt(id)
    assertEquals("New Title", updated.title)
    assertEquals("New Desc", updated.description)
    assertEquals(2.5, updated.time, 0.0)
    assertEquals(4.2, updated.distance, 0.0)
    assertEquals(FirebaseAuth.getInstance().currentUser?.uid, updated.authorId)

    assertTrue(doneCalled)
    assertNull(vm.uiState.value.errorMsg)
  }

  @Test
  fun delete_via_button_deletes_hunt_and_calls_onGoBack() = runBlocking {
    // 1) Create a hunt to be edited/deleted
    createHunt(title = "To Delete", description = "Will be removed")
    val allBefore = repository.getAllHunts()
    assertTrue(allBefore.isNotEmpty())
    val id = allBefore.first().uid

    val vm = EditHuntViewModel(repository)
    var backCalled = false

    // 2) Set content
    setContent(
        huntId = id,
        vm = vm,
        onGoBack = { backCalled = true },
    )

    // 3) Wait until the hunt is loaded into the view model (UI ready)
    composeRule.waitUntil(timeoutMillis = 5_000) { vm.uiState.value.title.isNotEmpty() }

    // 4) Scroll to the delete button (it's at the bottom)
    composeRule.onNodeWithTag(HuntScreenTestTags.BUTTON_DELETE_HUNT).performScrollTo()

    // 5) Now it should be visible and clickable
    composeRule.onNodeWithTag(HuntScreenTestTags.BUTTON_DELETE_HUNT).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntScreenTestTags.BUTTON_DELETE_HUNT).assertIsEnabled()

    // 6) Click delete (triggers deleteCurrentHunt() then onGoBack())
    composeRule.onNodeWithTag(HuntScreenTestTags.BUTTON_DELETE_HUNT).performClick()

    // 7) Wait until navigation callback is invoked
    composeRule.waitUntil(timeoutMillis = 5_000) { backCalled }

    // 8) Verify it’s gone in repo
    val after = repository.getAllHunts()
    assertTrue(after.none { it.uid == id })
  }

  // Helpers

  private fun createHunt(
      title: String = "T",
      description: String = "D",
      time: String = "1.5",
      distance: String = "2.0",
      difficulty: Difficulty = Difficulty.EASY,
      status: HuntStatus = HuntStatus.FUN,
      imageUri: Uri = Uri.parse("file://test-image.jpg")
  ) {
    val a = Location(0.0, 0.0, "Start")
    val m = Location(0.5, 0.5, "Mid")
    val b = Location(1.0, 1.0, "End")

    addVM.setTitle(title)
    addVM.setDescription(description)
    addVM.setTime(time)
    addVM.setDistance(distance)
    addVM.setDifficulty(difficulty)
    addVM.setStatus(status)
    addVM.updateMainImageUri(imageUri)
    addVM.setPoints(listOf(a, m, b))
    val ok = addVM.submit()
    assertTrue(ok)
  }
}
