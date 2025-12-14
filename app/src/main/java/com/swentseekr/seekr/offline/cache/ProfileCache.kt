package com.swentseekr.seekr.offline.cache

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.swentseekr.seekr.ui.profile.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Name of the DataStore used to store profile data locally. */
const val PROFILE_NAME = "profile_cache"

/** Key used to store the serialized profile JSON in DataStore. */
const val PROFILE_KEY = "profile_json"

/** Provides a convenient property to access the profile cache from a [Context]. */
internal val Context.profileDataStore by preferencesDataStore(name = PROFILE_NAME)

/**
 * Singleton object that provides methods to save, observe, and clear a [Profile] from the local
 * cache using DataStore.
 */
object ProfileCache {

  internal val PROFILE_JSON = stringPreferencesKey(PROFILE_KEY)

  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  /**
   * Saves a [Profile] to the local cache.
   *
   * @param context Context used to access DataStore.
   * @param profile The [Profile] object to save.
   */
  suspend fun saveProfile(context: Context, profile: Profile) {
    val jsonString = json.encodeToString(profile)
    context.profileDataStore.edit { prefs -> prefs[PROFILE_JSON] = jsonString }
  }

  /**
   * Observes changes to the cached [Profile] as a [Flow]. Emits `null` if no profile is saved.
   *
   * @param context Context used to access DataStore.
   * @return A [Flow] emitting the cached [Profile] or null.
   */
  fun observeProfile(context: Context): Flow<Profile?> {
    return context.profileDataStore.data.map { prefs ->
      val jsonString = prefs[PROFILE_JSON] ?: return@map null
      runCatching { json.decodeFromString<Profile>(jsonString) }.getOrNull()
    }
  }

  /**
   * Clears the cached [Profile] from DataStore.
   *
   * @param context Context used to access DataStore.
   */
  suspend fun clear(context: Context) {
    context.profileDataStore.edit { prefs -> prefs.remove(PROFILE_JSON) }
  }
}
