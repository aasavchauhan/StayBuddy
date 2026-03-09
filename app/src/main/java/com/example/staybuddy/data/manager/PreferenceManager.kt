package com.example.staybuddy.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.staybuddy.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey(Constants.KEY_ONBOARDING_COMPLETED)] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(Constants.KEY_ONBOARDING_COMPLETED)] = completed
        }
    }

    val userRole: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_USER_ROLE)] ?: Constants.ROLE_STUDENT
        }

    suspend fun setUserRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_USER_ROLE)] = role
        }
    }

    suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
