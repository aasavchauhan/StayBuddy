package com.example.staybuddy.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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

    val selectedCity: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[stringPreferencesKey(Constants.KEY_SELECTED_CITY)] }

    suspend fun setSelectedCity(city: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_SELECTED_CITY)] = city
        }
    }

    val selectedUniversity: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[stringPreferencesKey(Constants.KEY_SELECTED_UNIVERSITY)] }

    suspend fun setSelectedUniversity(university: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_SELECTED_UNIVERSITY)] = university
        }
    }

    val lastLocation: Flow<Pair<Double, Double>?> = context.dataStore.data
        .map { preferences ->
            val lat = preferences[doublePreferencesKey(Constants.KEY_LATITUDE)]
            val lon = preferences[doublePreferencesKey(Constants.KEY_LONGITUDE)]
            if (lat != null && lon != null) Pair(lat, lon) else null
        }

    suspend fun setLocation(lat: Double, lon: Double) {
        context.dataStore.edit { preferences ->
            preferences[doublePreferencesKey(Constants.KEY_LATITUDE)] = lat
            preferences[doublePreferencesKey(Constants.KEY_LONGITUDE)] = lon
        }
    }

    // Search Filters
    val filterPriceRange: Flow<Pair<Float, Float>> = context.dataStore.data
        .map { preferences ->
            val min = preferences[floatPreferencesKey(Constants.KEY_FILTER_PRICE_MIN)] ?: 500f
            val max = preferences[floatPreferencesKey(Constants.KEY_FILTER_PRICE_MAX)] ?: 30000f
            Pair(min, max)
        }

    suspend fun setFilterPriceRange(min: Float, max: Float) {
        context.dataStore.edit { preferences ->
            preferences[floatPreferencesKey(Constants.KEY_FILTER_PRICE_MIN)] = min
            preferences[floatPreferencesKey(Constants.KEY_FILTER_PRICE_MAX)] = max
        }
    }

    val selectedRoomTypes: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[stringSetPreferencesKey(Constants.KEY_FILTER_ROOM_TYPES)] ?: emptySet()
        }

    suspend fun setSelectedRoomTypes(types: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[stringSetPreferencesKey(Constants.KEY_FILTER_ROOM_TYPES)] = types
        }
    }

    val selectedGender: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_FILTER_GENDER)] ?: Constants.GENDER_ANY
        }

    suspend fun setSelectedGender(gender: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_FILTER_GENDER)] = gender
        }
    }

    val selectedAmenities: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[stringSetPreferencesKey(Constants.KEY_FILTER_AMENITIES)] ?: emptySet()
        }

    suspend fun setSelectedAmenities(amenities: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[stringSetPreferencesKey(Constants.KEY_FILTER_AMENITIES)] = amenities
        }
    }

    val sortOption: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_SORT_OPTION)] ?: "NEWEST"
        }

    suspend fun setSortOption(option: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_SORT_OPTION)] = option
        }
    }

    suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
