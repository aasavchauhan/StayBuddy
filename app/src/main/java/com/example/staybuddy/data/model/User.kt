package com.example.staybuddy.data.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "", // "student" or "owner"
    val gender: String = "",
    val city: String = "",
    val college: String = "",
    val profileImage: String = "",
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    // Premium & verification hooks (future use)
    val isPremiumUser: Boolean = false,
    val subscriptionTier: String = "free",
    val isOwnerVerified: Boolean = false,

    // Lifestyle Preferences (USP Sprint 4)
    val sleepSchedule: String = "", // "early_bird", "night_owl", "flexible"
    val cleanlinessLevel: String = "", // "obsessive", "average", "relaxed"
    val foodPreference: String = "", // "veg", "non_veg", "flexible"
    val smokingDrinking: String = "", // "no", "yes", "outside"
    val guestsVisitors: String = "" // "no_guests", "day_only", "anytime"
) {
    /** Derived map used by roommate compatibility scoring. */
    val quizResults: Map<String, Int>
        get() {
            if (sleepSchedule.isBlank()) return emptyMap()
            return mapOf(
                "sleepSchedule" to answerToScore(sleepSchedule),
                "cleanlinessLevel" to answerToScore(cleanlinessLevel),
                "foodPreference" to answerToScore(foodPreference),
                "smokingDrinking" to answerToScore(smokingDrinking),
                "guestsVisitors" to answerToScore(guestsVisitors)
            )
        }

    private fun answerToScore(answer: String): Int = when (answer) {
        // Sleep
        "early_bird" -> 1; "night_owl" -> 2; "flexible" -> 3
        // Cleanliness
        "obsessive" -> 1; "average" -> 2; "relaxed" -> 3
        // Food
        "veg" -> 1; "non_veg" -> 2
        // Smoking
        "no" -> 1; "outside" -> 2; "yes" -> 3
        // Guests
        "no_guests" -> 1; "day_only" -> 2; "anytime" -> 3
        else -> 0
    }
}
