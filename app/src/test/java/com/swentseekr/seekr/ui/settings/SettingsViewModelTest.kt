package com.swentseekr.seekr.ui.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.swentseekr.seekr.model.authentication.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var authRepository: AuthRepository
  private lateinit var viewModel: SettingsViewModel
  private lateinit var context: Context

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    mockkStatic(NotificationManagerCompat::class)
    mockkStatic(ContextCompat::class)

    authRepository = mockk()
    context = mockk(relaxed = true)

    viewModel = SettingsViewModel(authRepository)
  }

  @After
  fun tearDown() {
    unmockkAll()
    Dispatchers.resetMain()
  }

  @Test
  fun `refreshPermissions - all granted sets flags and enabled true`() {
    val notificationManager = mockk<NotificationManagerCompat>()

    every { NotificationManagerCompat.from(context) } returns notificationManager
    every { notificationManager.areNotificationsEnabled() } returns true

    every { ContextCompat.checkSelfPermission(context, any()) } returns
        PackageManager.PERMISSION_GRANTED

    viewModel.refreshPermissions(context)

    val state = viewModel.uiState.value
    assertTrue(state.notificationPermissionGranted)
    assertTrue(state.galleryPermissionGranted)
    assertTrue(state.locationPermissionGranted)
    assertTrue(state.notificationsEnabled)
    assertTrue(state.picturesEnabled)
    assertTrue(state.localisationEnabled)
  }

  @Test
  fun `refreshPermissions - none granted sets flags and enabled false`() {
    val notificationManager = mockk<NotificationManagerCompat>()

    every { NotificationManagerCompat.from(context) } returns notificationManager
    every { notificationManager.areNotificationsEnabled() } returns false

    every { ContextCompat.checkSelfPermission(context, any()) } returns
        PackageManager.PERMISSION_DENIED

    viewModel.refreshPermissions(context)

    val state = viewModel.uiState.value
    assertFalse(state.notificationPermissionGranted)
    assertFalse(state.galleryPermissionGranted)
    assertFalse(state.locationPermissionGranted)
    assertFalse(state.notificationsEnabled)
    assertFalse(state.picturesEnabled)
    assertFalse(state.localisationEnabled)
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
  fun `onNotificationsToggleRequested - enabled and not granted emits RequestNotification event`() =
      runTest {
        val initial =
            viewModel.uiState.value.copy(
                notificationPermissionGranted = false, notificationsEnabled = false)
        setUiState(initial)

        val events = mutableListOf<PermissionEvent>()
        val job = launch { viewModel.permissionEvents.collect { event -> events.add(event) } }

        viewModel.onNotificationsToggleRequested(enabled = true, context = context)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        assertTrue(events.first() is PermissionEvent.RequestNotification)

        job.cancel()
      }

  @Test
  fun `onNotificationsToggleRequested - disabled opens settings`() {
    val initial = viewModel.uiState.value
    setUiState(initial)

    every { context.startActivity(any()) } just runs

    viewModel.onNotificationsToggleRequested(enabled = false, context = context)

    io.mockk.verify {
      context.startActivity(
          match { intent ->
            intent.action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS &&
                intent.data?.scheme == "package"
          })
    }
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
  fun `onNotificationPermissionResult - granted updates state and notificationsEnabled`() {
    val initial =
        viewModel.uiState.value.copy(
            notificationPermissionGranted = false, notificationsEnabled = false)
    setUiState(initial)

    viewModel.onNotificationPermissionResult(granted = true)

    val state = viewModel.uiState.value
    assertTrue(state.notificationPermissionGranted)
    assertTrue(state.notificationsEnabled)
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
  fun `onNotificationPermissionResult - denied keeps notifications disabled`() {
    val initial =
        viewModel.uiState.value.copy(
            notificationPermissionGranted = false, notificationsEnabled = false)
    setUiState(initial)

    viewModel.onNotificationPermissionResult(granted = false)

    val state = viewModel.uiState.value
    assertFalse(state.notificationPermissionGranted)
    assertFalse(state.notificationsEnabled)
  }

  @Test
  fun `onPicturesToggleRequested - enable when not granted emits RequestGallery event`() = runTest {
    val initial =
        viewModel.uiState.value.copy(galleryPermissionGranted = false, picturesEnabled = false)
    setUiState(initial)

    val events = mutableListOf<PermissionEvent>()
    val job = launch { viewModel.permissionEvents.collect { event -> events.add(event) } }

    viewModel.onPicturesToggleRequested(enabled = true, context = context)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(1, events.size)
    assertTrue(events.first() is PermissionEvent.RequestGallery)

    job.cancel()
  }

  @Test
  fun `onPicturesToggleRequested - disable opens settings`() {
    every { context.startActivity(any()) } just runs

    viewModel.onPicturesToggleRequested(enabled = false, context = context)

    io.mockk.verify { context.startActivity(any()) }
  }

  @Test
  fun `onGalleryPermissionResult - granted updates state and picturesEnabled`() {
    val initial =
        viewModel.uiState.value.copy(galleryPermissionGranted = false, picturesEnabled = false)
    setUiState(initial)

    viewModel.onGalleryPermissionResult(granted = true)

    val state = viewModel.uiState.value
    assertTrue(state.galleryPermissionGranted)
    assertTrue(state.picturesEnabled)
  }

  @Test
  fun `onGalleryPermissionResult - denied leaves pictures disabled`() {
    val initial =
        viewModel.uiState.value.copy(galleryPermissionGranted = false, picturesEnabled = false)
    setUiState(initial)

    viewModel.onGalleryPermissionResult(granted = false)

    val state = viewModel.uiState.value
    assertFalse(state.galleryPermissionGranted)
    assertFalse(state.picturesEnabled)
  }

  @Test
  fun `onLocalisationToggleRequested - enable when not granted emits RequestLocation event`() =
      runTest {
        val initial =
            viewModel.uiState.value.copy(
                locationPermissionGranted = false, localisationEnabled = false)
        setUiState(initial)

        val events = mutableListOf<PermissionEvent>()
        val job = launch { viewModel.permissionEvents.collect { event -> events.add(event) } }

        viewModel.onLocalisationToggleRequested(enabled = true, context = context)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        assertTrue(events.first() is PermissionEvent.RequestLocation)

        job.cancel()
      }

  @Test
  fun `onLocalisationToggleRequested - disable opens settings`() {
    every { context.startActivity(any()) } just runs

    viewModel.onLocalisationToggleRequested(enabled = false, context = context)

    io.mockk.verify { context.startActivity(any()) }
  }

  @Test
  fun `onLocationPermissionResult - granted updates state and localisationEnabled`() {
    val initial =
        viewModel.uiState.value.copy(locationPermissionGranted = false, localisationEnabled = false)
    setUiState(initial)

    viewModel.onLocationPermissionResult(granted = true)

    val state = viewModel.uiState.value
    assertTrue(state.locationPermissionGranted)
    assertTrue(state.localisationEnabled)
  }

  @Test
  fun `onLocationPermissionResult - denied leaves localisation disabled`() {
    val initial =
        viewModel.uiState.value.copy(locationPermissionGranted = false, localisationEnabled = false)
    setUiState(initial)

    viewModel.onLocationPermissionResult(granted = false)

    val state = viewModel.uiState.value
    assertFalse(state.locationPermissionGranted)
    assertFalse(state.localisationEnabled)
  }

  @Test
  fun `signOut success sets signedOut true and no error`() = runTest {
    val credentialManager = mockk<CredentialManager>()
    coEvery { authRepository.signOut() } returns Result.success(Unit)
    coEvery { credentialManager.clearCredentialState(any<ClearCredentialStateRequest>()) } just runs

    viewModel.signOut(credentialManager)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.signedOut)
    assertNull(state.errorMsg)

    coVerify { authRepository.signOut() }
    coVerify { credentialManager.clearCredentialState(any()) }
  }

  @Test
  fun `signOut failure sets errorMsg`() = runTest {
    val credentialManager = mockk<CredentialManager>()
    val error = RuntimeException("boom")
    coEvery { authRepository.signOut() } returns Result.failure(error)
    coEvery { credentialManager.clearCredentialState(any<ClearCredentialStateRequest>()) } just runs

    viewModel.signOut(credentialManager)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.signedOut)
    assertEquals("boom", state.errorMsg)

    coVerify { authRepository.signOut() }
    coVerify { credentialManager.clearCredentialState(any()) }
  }

  @Test
  fun `clearErrorMsg clears error`() {
    val initial = viewModel.uiState.value.copy(errorMsg = "error")
    setUiState(initial)

    viewModel.clearErrorMsg()

    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun `setAppVersion updates version`() {
    viewModel.setAppVersion("1.2.3")

    assertEquals("1.2.3", viewModel.uiState.value.appVersion)
  }

  private fun setUiState(newState: SettingsUIState) {
    val field = SettingsViewModel::class.java.getDeclaredField("_uiState")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val mutableStateFlow = field.get(viewModel) as MutableStateFlow<SettingsUIState>
    mutableStateFlow.value = newState
  }

  @Test
  fun updateNotifications_disabled_updates_field_only() = runTest {
    coEvery { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, false) } returns Unit

    viewModel.updateNotifications(false, null)
    advanceUntilIdle()

    coVerify { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, false) }
  }

  @Test
  fun updatePictures_updates_repository_field() = runTest {
    coEvery { repository.updateField(SettingsScreenStrings.PICTURES_FIELD, true) } returns Unit

    viewModel.updatePictures(true)
    advanceUntilIdle()

    coVerify { repository.updateField(SettingsScreenStrings.PICTURES_FIELD, true) }
  }

  @Test
  fun updateLocalisation_updates_repository_field() = runTest {
    coEvery { repository.updateField(SettingsScreenStrings.LOCALISATION_FIELD, false) } returns Unit

    viewModel.updateLocalisation(false)
    advanceUntilIdle()

    coVerify { repository.updateField(SettingsScreenStrings.LOCALISATION_FIELD, false) }
  }

  @Test
  fun updateNotifications_disabled_with_null_context_updates_repository_only() = runTest {
    coEvery { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, false) } returns Unit

    mockkObject(NotificationHelper)
    every { NotificationHelper.sendNotification(any(), any(), any()) } just Runs

    viewModel.updateNotifications(false, null)
    advanceUntilIdle()

    coVerify { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, false) }
    verify(exactly = TEST_VERIFICATION_TIMES_NULL) {
      NotificationHelper.sendNotification(any(), any(), any())
    }

    unmockkObject(NotificationHelper)
  }

  @Test
  fun updateNotifications_enabled_with_context_calls_repository_and_sends_notification() = runTest {
    val mockContext = mockk<Context>(relaxed = true)

    coEvery { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, true) } returns Unit

    mockkObject(NotificationHelper)
    every { NotificationHelper.sendNotification(any(), any(), any()) } just Runs

    viewModel.updateNotifications(true, mockContext)
    advanceUntilIdle()

    coVerify(exactly = TEST_VERIFICATION_TIMES_NOT_NULL) {
      repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, true)
    }

    verify(exactly = TEST_VERIFICATION_TIMES_NOT_NULL) {
      NotificationHelper.sendNotification(
          mockContext,
          SettingsScreenStrings.NOTIFICATION_FIELD_2,
          SettingsScreenStrings.NOTIFICATION_ACCEPT_MESSAGE)
    }

    unmockkObject(NotificationHelper)
  }

  @Test
  fun updateNotifications_enabled_without_context_calls_repository_only() = runTest {
    coEvery { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, true) } returns Unit

    mockkObject(NotificationHelper)
    every { NotificationHelper.sendNotification(any(), any(), any()) } just Runs

    viewModel.updateNotifications(true, null)
    advanceUntilIdle()

    coVerify(exactly = TEST_VERIFICATION_TIMES_NOT_NULL) {
      repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, true)
    }

    verify(exactly = TEST_VERIFICATION_TIMES_NULL) {
      NotificationHelper.sendNotification(any(), any(), any())
    }

    unmockkObject(NotificationHelper)
  }

  @Test
  fun updateNotifications_disabled_with_context_calls_repository_but_no_notification() = runTest {
    val mockContext = mockk<Context>(relaxed = true)

    coEvery { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, false) } returns Unit

    mockkObject(NotificationHelper)
    every { NotificationHelper.sendNotification(any(), any(), any()) } just Runs

    viewModel.updateNotifications(false, mockContext)
    advanceUntilIdle()

    coVerify(exactly = TEST_VERIFICATION_TIMES_NOT_NULL) {
      repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, false)
    }

    verify(exactly = TEST_VERIFICATION_TIMES_NULL) {
      NotificationHelper.sendNotification(any(), any(), any())
    }

    unmockkObject(NotificationHelper)
  }

  @Test
  fun updateNotifications_disabled_without_context_calls_repository_only() = runTest {
    coEvery { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, false) } returns Unit

    mockkObject(NotificationHelper)
    every { NotificationHelper.sendNotification(any(), any(), any()) } just Runs

    viewModel.updateNotifications(false, null)
    advanceUntilIdle()

    coVerify(exactly = TEST_VERIFICATION_TIMES_NOT_NULL) {
      repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, false)
    }

    verify(exactly = TEST_VERIFICATION_TIMES_NULL) {
      NotificationHelper.sendNotification(any(), any(), any())
    }

    unmockkObject(NotificationHelper)
  }

  @Test
  fun updateNotifications_with_activity_context_requests_permissions_on_tiramisu() = runTest {
    val mockActivity = mockk<Activity>(relaxed = true)

    coEvery { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, true) } returns Unit

    mockkStatic(ActivityCompat::class)
    every { ActivityCompat.requestPermissions(any(), any(), any()) } just Runs

    mockkObject(NotificationHelper)
    every { NotificationHelper.sendNotification(any(), any(), any()) } just Runs

    viewModel.updateNotifications(true, mockActivity)
    advanceUntilIdle()

    coVerify(exactly = TEST_VERIFICATION_TIMES_NOT_NULL) {
      repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, true)
    }

    verify(exactly = TEST_VERIFICATION_TIMES_NOT_NULL) {
      NotificationHelper.sendNotification(mockActivity, any(), any())
    }

    unmockkStatic(ActivityCompat::class)
    unmockkObject(NotificationHelper)
  }

  @Test
  fun updateNotifications_calls_repository_before_sending_notification() = runTest {
    val mockContext = mockk<Context>(relaxed = true)
    val callOrder = mutableListOf<String>()

    coEvery { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, true) } answers
        {
          callOrder.add(TEST_REPOSITORY)
        }

    mockkObject(NotificationHelper)
    every { NotificationHelper.sendNotification(any(), any(), any()) } answers
        {
          callOrder.add(TEST_NOTIFICATION)
        }

    viewModel.updateNotifications(true, mockContext)
    advanceUntilIdle()

    assertEquals(SettingsScreenDefaults.TEST_CALL_ORDER_SIZE, callOrder.size)
    assertEquals(TEST_REPOSITORY, callOrder[TEST_VERIFICATION_TIMES_NULL])
    assertEquals(TEST_NOTIFICATION, callOrder[TEST_VERIFICATION_TIMES_NOT_NULL])

    unmockkObject(NotificationHelper)
  }
}
