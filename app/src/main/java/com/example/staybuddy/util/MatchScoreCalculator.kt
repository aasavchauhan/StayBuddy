package com.example.staybuddy.util

import com.example.staybuddy.data.model.User
import com.example.staybuddy.data.model.RoommatePost

object MatchScoreCalculator {

    /**
     * Calculates compatibility score (0-100) between a viewing user and a roommate post.
     */
    fun calculateMatchScore(currentUser: User, post: RoommatePost): Int {
        var score = 0.0

        // 1. Gender Match (15%) - Critical for most
        if (post.genderPreference.isBlank() || 
            post.genderPreference.equals(currentUser.gender, ignoreCase = true) ||
            post.genderPreference.equals("Any", ignoreCase = true)) {
            score += 15.0
        }

        // 2. City Match (25%) - Usually a hard requirement
        if (post.city.equals(currentUser.city, ignoreCase = true)) {
            score += 25.0
        }

        // 3. Budget (35%) - Heavily weighted
        // Simple logic: if post price is within ±20% of user preference or simply available
        // For now, if priceShare > 0, we give partial if it's "reasonable"
        // Better: Compare with user's stored budget (if we had it)
        // For MVP: 35% if city matches (soft budget match for now)
        score += 35.0 

        // 4. Lifestyle (25% total - 5% each)
        
        // Sleep Schedule
        if (currentUser.sleepSchedule.isNotBlank() && post.sleepSchedule.isNotBlank()) {
            if (currentUser.sleepSchedule == post.sleepSchedule) score += 5.0
            else if (currentUser.sleepSchedule == "flexible" || post.sleepSchedule == "flexible") score += 3.0
        } else score += 2.5 // Half points if one is missing

        // Cleanliness
        if (currentUser.cleanlinessLevel.isNotBlank() && post.cleanlinessLevel.isNotBlank()) {
            if (currentUser.cleanlinessLevel == post.cleanlinessLevel) score += 5.0
            else if (currentUser.cleanlinessLevel == "average") score += 3.0
        } else score += 2.5

        // Food
        if (currentUser.foodPreference.isNotBlank() && post.foodPreference.isNotBlank()) {
            if (currentUser.foodPreference == post.foodPreference || currentUser.foodPreference == "flexible") score += 5.0
        } else score += 2.5

        // Smoking/Drinking
        if (currentUser.smokingDrinking.isNotBlank() && post.smokingDrinking.isNotBlank()) {
            if (currentUser.smokingDrinking == post.smokingDrinking) score += 5.0
        } else score += 2.5

        // Guests
        if (currentUser.guestsVisitors.isNotBlank() && post.guestsVisitors.isNotBlank()) {
            if (currentUser.guestsVisitors == post.guestsVisitors) score += 5.0
        } else score += 2.5

        return score.toInt().coerceIn(0, 100)
    }
}
