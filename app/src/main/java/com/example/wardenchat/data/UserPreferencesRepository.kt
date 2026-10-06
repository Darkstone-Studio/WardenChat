package com.example.wardenchat.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
    }

    val myIdFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[MY_ID_KEY]
        }

    suspend fun saveMyId(id: String) {
        dataStore.edit { preferences ->
            preferences[MY_ID_KEY] = id
        }
    }
}
