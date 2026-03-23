package com.example.staybuddy.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun logScreenView(screenName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        })
    }

    fun logListingViewed(listingId: String, title: String, price: Int) {
        firebaseAnalytics.logEvent("listing_viewed", Bundle().apply {
            putString("listing_id", listingId)
            putString("listing_title", title)
            putInt("listing_price", price)
        })
    }

    fun logListingFavorited(listingId: String, isFavorite: Boolean) {
        firebaseAnalytics.logEvent("listing_favorited", Bundle().apply {
            putString("listing_id", listingId)
            putBoolean("is_favorite", isFavorite)
        })
    }

    fun logChatStarted(listingId: String, targetUserId: String) {
        firebaseAnalytics.logEvent("chat_started", Bundle().apply {
            putString("listing_id", listingId)
            putString("target_user_id", targetUserId)
        })
    }

    fun logSearchPerformed(query: String, resultCount: Int) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
            putInt("result_count", resultCount)
        })
    }

    fun logRoommateMatched(matchScore: Int, targetUserId: String) {
        firebaseAnalytics.logEvent("roommate_matched", Bundle().apply {
            putInt("match_score", matchScore)
            putString("target_user_id", targetUserId)
        })
    }

    fun logUpdatePrompted(currentVersion: String, latestVersion: String) {
        firebaseAnalytics.logEvent("app_update_prompted", Bundle().apply {
            putString("current_version", currentVersion)
            putString("latest_version", latestVersion)
        })
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        firebaseAnalytics.logEvent(eventName, Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        })
    }
}
