package com.example.staybuddy.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.PgListing
import com.example.staybuddy.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

data class MapUiState(
    val listings: List<PgListing> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedListing: PgListing? = null,
    val userLocation: GeoPoint? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val listingRepository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            listingRepository.getListings()
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true, error = null) }
                .catch { e -> 
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "An unknown error occurred"
                    )
                }
                .collect { listings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        listings = listings
                    )
                }
        }
    }

    fun selectListing(listing: PgListing?) {
        _uiState.value = _uiState.value.copy(selectedListing = listing)
    }

    fun updateUserLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(userLocation = GeoPoint(lat, lng))
    }
}
