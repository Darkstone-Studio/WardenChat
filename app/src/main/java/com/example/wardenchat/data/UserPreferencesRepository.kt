package com.example.wardenchat.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "warden_user_prefs")

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.applicationContext.dataStore

    companion object {
        private val MY_ID_KEY = stringPreferencesKey("my_user_id")
        private val DISPLAY_NAME_KEY = stringPreferencesKey("display_name")
        private val SELF_DESTRUCT_KEY = booleanPreferencesKey("self_destruct_enabled")
    }

    val myIdFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[MY_ID_KEY]
        }

    val displayNameFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[DISPLAY_NAME_KEY]
        }

    val selfDestructFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[SELF_DESTRUCT_KEY] ?: false
        }

    suspend fun saveMyId(id: String) {
        dataStore.edit { preferences ->
            preferences[MY_ID_KEY] = id
        }
    }

    suspend fun saveDisplayName(name: String) {
        dataStore.edit { preferences ->
            preferences[DISPLAY_NAME_KEY] = name
        }
    }

    suspend fun saveSelfDestruct(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SELF_DESTRUCT_KEY] = enabled
        }
    }
}
