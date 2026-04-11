package com.example.staybuddy.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT listingId FROM favorites WHERE userId = :userId")
    fun getFavoriteIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE listingId = :listingId AND userId = :userId")
    suspend fun deleteFavorite(listingId: String, userId: String)

    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun clearFavorites(userId: String)
}
