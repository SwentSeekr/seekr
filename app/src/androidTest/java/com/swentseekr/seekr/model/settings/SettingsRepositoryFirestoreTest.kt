package com.swentseekr.seekr.model.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.ui.settings.SettingsContent
import com.swentseekr.seekr.ui.settings.SettingsScreenStrings
import com.swentseekr.seekr.ui.settings.SettingsScreenTestTags
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import com.swentseekr.seekr.utils.FirebaseTestEnvironment.clearEmulatorData
import junit.framework.TestCase.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryFirestoreTest {

  @get:Rule val composeRule = createComposeRule()

  private lateinit var repository: SettingsRepositoryFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var firestore: FirebaseFirestore

  @Before
  fun setup() = runTest {
    FirebaseTestEnvironment.setup()
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      clearEmulatorData()
    }

    auth = FirebaseAuth.getInstance()
    auth.signInAnonymously().await()
    firestore = FirebaseFirestore.getInstance()
    repository = SettingsRepositoryFirestore(firestore, auth)
  }

  @Test
  fun currentUser_isNotNull() = runTest {
    assertNotNull(SettingsScreenStrings.FIREBASE_TEST_ERROR, auth.currentUser)
  }

  @Test
  fun getSettings_returnsDefault_whenNoDocumentExists() = runTest {
    repository.loadSettings()
    val settings = repository.settingsFlow.first()
    assertEquals(false, settings.notificationsEnabled)
    assertEquals(false, settings.picturesEnabled)
    assertEquals(false, settings.localisationEnabled)
  }

  @Test
  fun setSettings_persistsSettingsCorrectly() = runTest {
    val newSettings =
        UserSettings(
            notificationsEnabled = true, picturesEnabled = true, localisationEnabled = false)

    repository.setSettings(newSettings)
    val retrieved = repository.settingsFlow.first()

    assertEquals(true, retrieved.notificationsEnabled)
    assertEquals(true, retrieved.picturesEnabled)
    assertEquals(false, retrieved.localisationEnabled)
  }

  @Test
  fun updateField_updatesSingleField() = runTest {
    repository.setSettings(UserSettings())
    repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, true)

    val retrieved = repository.settingsFlow.first()
    assertTrue(retrieved.notificationsEnabled)
    assertFalse(retrieved.picturesEnabled)
    assertFalse(retrieved.localisationEnabled)
  }

  @Test
  fun getSettings_autoCreatesDocumentIfMissing() = runTest {
    val uid = auth.currentUser!!.uid
    firestore.collection(SettingsScreenStrings.USER_SETTINGS).document(uid).delete().await()

    repository.loadSettings()
    val settings = repository.settingsFlow.first()
    assertNotNull(settings)

    val doc = firestore.collection(SettingsScreenStrings.USER_SETTINGS).document(uid).get().await()
    assertTrue(SettingsScreenStrings.DOCUMENT_TEST_TEXT, doc.exists())
  }

  @Test
  fun toggling_notifications_updatesFirestore() = runBlocking {
    val uid = auth.currentUser!!.uid
    repository.setSettings(UserSettings(notificationsEnabled = false))

    composeRule.setContent { TestSettingsContent(repository) }

    composeRule.onNodeWithTag(SettingsScreenTestTags.NOTIFICATIONS_TOGGLE).performClick()

    val doc = firestore.collection(SettingsScreenStrings.USER_SETTINGS).document(uid).get().await()
    assertEquals(true, doc.getBoolean(SettingsScreenStrings.NOTIFICATION_FIELD))
  }

  @Test
  fun toggling_pictures_updatesFirestore() = runBlocking {
    val uid = auth.currentUser!!.uid
    repository.setSettings(UserSettings(picturesEnabled = false))

    composeRule.setContent { TestSettingsContent(repository) }

    composeRule.onNodeWithTag(SettingsScreenTestTags.PICTURES_TOGGLE).performClick()

    val doc = firestore.collection(SettingsScreenStrings.USER_SETTINGS).document(uid).get().await()
    assertEquals(true, doc.getBoolean(SettingsScreenStrings.PICTURES_FIELD))
  }

  @Test
  fun toggling_localisation_updatesFirestore() = runBlocking {
    val uid = auth.currentUser!!.uid
    repository.setSettings(UserSettings(localisationEnabled = false))

    composeRule.setContent { TestSettingsContent(repository) }

    composeRule.onNodeWithTag(SettingsScreenTestTags.LOCALISATION_TOGGLE).performClick()

    val doc = firestore.collection(SettingsScreenStrings.USER_SETTINGS).document(uid).get().await()
    assertEquals(true, doc.getBoolean(SettingsScreenStrings.LOCALISATION_FIELD))
  }
}

@Composable
fun TestSettingsContent(repository: SettingsRepositoryFirestore) {
  val settings by repository.settingsFlow.collectAsState()
  val scope = rememberCoroutineScope()

  SettingsContent(
      appVersion = SettingsScreenStrings.APP_VERSION_1,
      uiState = settings,
      onNotificationsChange = { value ->
        scope.launch { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, value) }
      },
      onPicturesChange = { value ->
        scope.launch { repository.updateField(SettingsScreenStrings.PICTURES_FIELD, value) }
      },
      onLocalisationChange = { value ->
        scope.launch { repository.updateField(SettingsScreenStrings.LOCALISATION_FIELD, value) }
      },
      onEditProfileClick = {},
      onLogoutClick = {})
}
