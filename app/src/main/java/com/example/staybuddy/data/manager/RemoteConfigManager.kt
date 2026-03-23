package com.example.staybuddy.data.manager

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor() {

    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    companion object {
        private const val TAG = "RemoteConfigManager"

        // Feature flags
        const val KEY_FORCE_UPDATE_BELOW_VERSION = "force_update_below_version"
        const val KEY_MAINTENANCE_MODE = "maintenance_mode"
        const val KEY_FEATURE_ROOMMATE_MATCH = "feature_roommate_match"
        const val KEY_LATEST_VERSION_NAME = "latest_version_name"
        const val KEY_UPDATE_MESSAGE = "update_message"

        // Cache interval
        private const val FETCH_INTERVAL_SECONDS = 43200L // 12 hours
    }

    private val defaults = mapOf(
        KEY_FORCE_UPDATE_BELOW_VERSION to 0L,
        KEY_MAINTENANCE_MODE to false,
        KEY_FEATURE_ROOMMATE_MATCH to false,
        KEY_LATEST_VERSION_NAME to "",
        KEY_UPDATE_MESSAGE to "A new version of StayBuddy is available! Update for the best experience."
    )

    fun init() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = FETCH_INTERVAL_SECONDS
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(defaults)
        fetchAndActivate()
    }

    private fun fetchAndActivate() {
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                Log.d(TAG, "Remote Config fetched and activated")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Remote Config fetch failed, using defaults", e)
            }
    }

    fun getBoolean(key: String): Boolean = remoteConfig.getBoolean(key)

    fun getLong(key: String): Long = remoteConfig.getLong(key)

    fun getString(key: String): String = remoteConfig.getString(key)

    val isMaintenanceMode: Boolean
        get() = getBoolean(KEY_MAINTENANCE_MODE)

    val isRoommateMatchEnabled: Boolean
        get() = getBoolean(KEY_FEATURE_ROOMMATE_MATCH)

    val forceUpdateBelowVersion: Long
        get() = getLong(KEY_FORCE_UPDATE_BELOW_VERSION)

    val latestVersionName: String
        get() = getString(KEY_LATEST_VERSION_NAME)

    val updateMessage: String
        get() = getString(KEY_UPDATE_MESSAGE)
}
