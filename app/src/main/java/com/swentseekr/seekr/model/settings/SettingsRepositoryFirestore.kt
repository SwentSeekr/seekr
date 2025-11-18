package com.swentseekr.seekr.model.settings

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.ui.settings.SettingsScreenStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

/**
 * Data class representing the user settings in the app.
 *
 * @property notificationsEnabled Whether push notifications are enabled.
 * @property picturesEnabled Whether pictures are enabled in the app.
 * @property localisationEnabled Whether location/localisation features are enabled.
 */
data class UserSettings(
    val notificationsEnabled: Boolean = false,
    val picturesEnabled: Boolean = false,
    val localisationEnabled: Boolean = false
)

/**
 * Repository class that manages reading and updating user settings in Firestore.
 *
 * Uses [MutableStateFlow] to keep the current settings in memory and allow UI to observe changes
 * reactively.
 *
 * @param firestore The Firestore instance used for database operations.
 * @param auth The FirebaseAuth instance used to determine the current user.
 */
class SettingsRepositoryFirestore(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

  private val _settingsFlow = MutableStateFlow(UserSettings())
  val settingsFlow: StateFlow<UserSettings> = _settingsFlow.asStateFlow()

  private fun getSettingsDoc() =
      firestore
          .collection(SettingsScreenStrings.USER_SETTINGS)
          .document(
              auth.currentUser?.uid
                  ?: throw IllegalStateException(SettingsScreenStrings.SIGN_IN_ERROR))

  suspend fun loadSettings() {
    val doc = getSettingsDoc().get().await()
    val settings =
        if (doc.exists()) {
          UserSettings(
              notificationsEnabled =
                  doc.getBoolean(SettingsScreenStrings.NOTIFICATION_FIELD) ?: false,
              picturesEnabled = doc.getBoolean(SettingsScreenStrings.PICTURES_FIELD) ?: false,
              localisationEnabled =
                  doc.getBoolean(SettingsScreenStrings.LOCALISATION_FIELD) ?: false)
        } else {
          val default = UserSettings()
          setSettings(default)
          default
        }
    _settingsFlow.value = settings
  }

  suspend fun setSettings(settings: UserSettings) {
    getSettingsDoc().set(settings).await()
    _settingsFlow.value = settings
  }

  suspend fun updateField(field: String, value: Boolean) {
    getSettingsDoc().update(field, value).await()
    _settingsFlow.update { current ->
      when (field) {
        SettingsScreenStrings.NOTIFICATION_FIELD -> current.copy(notificationsEnabled = value)
        SettingsScreenStrings.PICTURES_FIELD -> current.copy(picturesEnabled = value)
        SettingsScreenStrings.LOCALISATION_FIELD -> current.copy(localisationEnabled = value)
        else -> current
      }
    }
  }
}
