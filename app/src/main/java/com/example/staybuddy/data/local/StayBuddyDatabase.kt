package com.example.staybuddy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PgListingEntity::class, SearchHistoryEntity::class, FavoriteEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StayBuddyDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
    abstract fun searchDao(): SearchDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val DATABASE_NAME = "staybuddy_db"
    }
}
