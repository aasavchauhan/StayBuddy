package com.example.staybuddy.data.manager

import com.example.staybuddy.data.local.SearchDao
import com.example.staybuddy.data.local.toDomainModel
import com.example.staybuddy.data.local.toEntity
import com.example.staybuddy.domain.model.AutocompletePrediction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryManager @Inject constructor(
    private val searchDao: SearchDao
) {
    val getSearchHistory: Flow<List<AutocompletePrediction>> =
        searchDao.getRecentSearchHistory().map { entities ->
            entities.map { it.toDomainModel() }
        }

    suspend fun addSearchPrediction(prediction: AutocompletePrediction) {
        searchDao.insertSearchQuery(prediction.toEntity())
    }

    suspend fun clearHistory() {
        searchDao.clearHistory()
    }

    suspend fun removeSearchPrediction(prediction: AutocompletePrediction) {
        searchDao.deleteSearchQuery(prediction.placeId)
    }
}
