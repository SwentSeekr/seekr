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

const val PROFILE_NAME = "profile_cache"
const val PROFILE_KEY = "profile_json"
internal val Context.profileDataStore by preferencesDataStore(name = PROFILE_NAME)

object ProfileCache {

  internal val PROFILE_JSON = stringPreferencesKey(PROFILE_KEY)

  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  suspend fun saveProfile(context: Context, profile: Profile) {
    val jsonString = json.encodeToString(profile)
    context.profileDataStore.edit { prefs -> prefs[PROFILE_JSON] = jsonString }
  }

  fun observeProfile(context: Context): Flow<Profile?> {
    return context.profileDataStore.data.map { prefs ->
      val jsonString = prefs[PROFILE_JSON] ?: return@map null
      runCatching { json.decodeFromString<Profile>(jsonString) }.getOrNull()
    }
  }

  suspend fun clear(context: Context) {
    context.profileDataStore.edit { prefs -> prefs.remove(PROFILE_JSON) }
  }
}
