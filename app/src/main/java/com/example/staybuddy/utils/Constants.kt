package com.example.staybuddy.utils

object Constants {
    // Firestore Collections
    const val USERS_COLLECTION = "users"
    const val PG_LISTINGS_COLLECTION = "pg_listings"
    const val ROOMMATE_POSTS_COLLECTION = "roommate_posts"
    const val CHATS_COLLECTION = "chats"
    const val MESSAGES_COLLECTION = "messages"
    const val FAVORITES_COLLECTION = "favorites"

    // Firebase Storage Paths
    const val PG_IMAGES_PATH = "pg_images"
    const val PROFILE_IMAGES_PATH = "profile_images"

    // User Roles
    const val ROLE_STUDENT = "student"
    const val ROLE_OWNER = "owner"

    // Room Types
    const val ROOM_SINGLE = "Single"
    const val ROOM_DOUBLE = "Double"
    const val ROOM_TRIPLE = "Triple"
    const val ROOM_DORM = "Dorm"

    // Gender
    const val GENDER_MALE = "Male"
    const val GENDER_FEMALE = "Female"
    const val GENDER_ANY = "Any"

    // DataStore Keys
    const val DATASTORE_NAME = "staybuddy_preferences"
    const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    const val KEY_USER_ROLE = "user_role"

    // Amenities
    val AMENITIES_LIST = listOf(
        "WiFi", "AC", "Laundry", "Food", "Parking",
        "TV", "Geyser", "Power Backup", "Security", "CCTV"
    )
}
