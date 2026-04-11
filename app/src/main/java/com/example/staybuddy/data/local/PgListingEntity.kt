package com.example.staybuddy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import com.example.staybuddy.data.model.PgListing
import com.example.staybuddy.data.model.LifestylePreferences

@Entity(tableName = "pg_listings")
data class PgListingEntity(
    @PrimaryKey val listingId: String,
    val ownerId: String,
    val title: String,
    val description: String,
    val city: String,
    val area: String,
    val latitude: Double,
    val longitude: Double,
    val price: Int,
    val deposit: Int,
    val roomType: String,
    val genderAllowed: String,
    val amenities: List<String>,
    val images: List<String>,
    val availableBeds: Int,
    val isActive: Boolean,
    val rating: Float,
    val ownerName: String,
    val ownerProfileImage: String,
    val ownerPhone: String,
    val createdAt: Long,
    val isVerified: Boolean,
    val reportCount: Int,
    val isPremium: Boolean,
    val boostExpiresAt: Long?,
    val featuredUntil: Long?,
    val viewCount: Int,
    @Embedded(prefix = "lifestyle_") val lifestylePreferences: LifestylePreferences = LifestylePreferences()
)

fun PgListingEntity.toDomainModel(): PgListing {
    return PgListing(
        listingId = listingId,
        ownerId = ownerId,
        title = title,
        description = description,
        city = city,
        area = area,
        latitude = latitude,
        longitude = longitude,
        price = price,
        deposit = deposit,
        roomType = roomType,
        genderAllowed = genderAllowed,
        amenities = amenities,
        images = images,
        availableBeds = availableBeds,
        isActive = isActive,
        rating = rating,
        ownerName = ownerName,
        ownerProfileImage = ownerProfileImage,
        ownerPhone = ownerPhone,
        createdAt = createdAt,
        isVerified = isVerified,
        reportCount = reportCount,
        isPremium = isPremium,
        boostExpiresAt = boostExpiresAt,
        featuredUntil = featuredUntil,
        viewCount = viewCount,
        lifestylePreferences = lifestylePreferences
    )
}

fun PgListing.toEntity(): PgListingEntity {
    return PgListingEntity(
        listingId = listingId,
        ownerId = ownerId,
        title = title,
        description = description,
        city = city,
        area = area,
        latitude = latitude,
        longitude = longitude,
        price = price,
        deposit = deposit,
        roomType = roomType,
        genderAllowed = genderAllowed,
        amenities = amenities,
        images = images,
        availableBeds = availableBeds,
        isActive = isActive,
        rating = rating,
        ownerName = ownerName,
        ownerProfileImage = ownerProfileImage,
        ownerPhone = ownerPhone,
        createdAt = createdAt,
        isVerified = isVerified,
        reportCount = reportCount,
        isPremium = isPremium,
        boostExpiresAt = boostExpiresAt,
        featuredUntil = featuredUntil,
        viewCount = viewCount,
        lifestylePreferences = lifestylePreferences
    )
}
