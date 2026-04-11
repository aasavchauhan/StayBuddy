package com.example.staybuddy.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(searchQuery: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE placeId = :placeId")
    suspend fun deleteSearchQuery(placeId: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}
