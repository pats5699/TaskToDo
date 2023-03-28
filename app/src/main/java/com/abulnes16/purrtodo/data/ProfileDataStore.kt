package com.abulnes16.purrtodo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val PROFILE_PREFERENCES = "profile_preferences"

// Preferences Values
private val PROFILE_NAME = stringPreferencesKey("profile_name")
private val PROFILE_PICTURE = stringPreferencesKey("profile_picture")

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PROFILE_PREFERENCES
)

class ProfileDataStore(context: Context) {

    val profilePreferences: Flow<User> = context.dataStore.data.catch {
        if (it is IOException) {
            it.printStackTrace()
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        val name = preferences[PROFILE_NAME] ?: "Friend"
        val picture = preferences[PROFILE_PICTURE] ?: ""
        User(name, picture)
    }

    suspend fun saveProfilePreferences(name: String, profilePicture: String, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PROFILE_NAME] = name
            preferences[PROFILE_PICTURE] = profilePicture
        }

    }
}