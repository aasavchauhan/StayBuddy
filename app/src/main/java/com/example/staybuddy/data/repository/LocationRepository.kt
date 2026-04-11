package com.example.staybuddy.data.repository

import com.example.staybuddy.data.api.NominatimService
import com.example.staybuddy.domain.model.AutocompletePrediction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val nominatimService: NominatimService
) {
    suspend fun searchLocation(query: String): Result<List<AutocompletePrediction>> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) {
                return@withContext Result.success(emptyList())
            }

            val responses = nominatimService.searchLocation(query = query)
            
            val predictions = responses.map { response ->
                val primary = response.address?.city 
                    ?: response.address?.town 
                    ?: response.address?.village 
                    ?: response.display_name.split(",").firstOrNull() ?: ""
                
                // Form a readable secondary text
                val secondaryParts = listOfNotNull(
                    response.address?.state_district,
                    response.address?.state,
                    response.address?.country
                )
                val secondary = secondaryParts.joinToString(", ")

                AutocompletePrediction(
                    placeId = response.place_id,
                    primaryText = primary.trim(),
                    secondaryText = secondary.trim(),
                    lat = response.lat.toDoubleOrNull() ?: 0.0,
                    lon = response.lon.toDoubleOrNull() ?: 0.0
                )
            }
            
            Result.success(predictions)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
