package com.example.staybuddy.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT * FROM pg_listings ORDER BY createdAt DESC")
    fun getAllPropertyListings(): Flow<List<PgListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPropertyListings(listings: List<PgListingEntity>)

    @Query("DELETE FROM pg_listings")
    suspend fun clearAllPropertyListings()

    @Query("SELECT * FROM pg_listings WHERE listingId = :id")
    suspend fun getPropertyListingById(id: String): PgListingEntity?

    @Query("SELECT * FROM pg_listings WHERE title LIKE '%' || :query || '%' OR city LIKE '%' || :query || '%' OR area LIKE '%' || :query || '%'")
    fun searchPropertyListings(query: String): Flow<List<PgListingEntity>>
}
