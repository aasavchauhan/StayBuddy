package com.example.staybuddy.ui.screens.search

import androidx.compose.ui.util.fastFilter
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val allListings: List<PgListing> = emptyList(),
    val filteredListings: List<PgListing> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Filters
    val priceRange: ClosedFloatingPointRange<Float> = 500f..30000f,
    val selectedRoomTypes: Set<String> = emptySet(),
    val selectedGender: String = "Any",
    val selectedAmenities: Set<String> = emptySet(),
    val maxDistanceKm: Float = 10f,
    
    // UI State
    val isFilterSheetVisible: Boolean = false,
    val isMapView: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val listingRepository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            listingRepository.getListings()
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { listings ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            allListings = listings,
                            // Apply current filters to new data
                        ).applyFilters(listings)
                    }
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery).applyFilters() }
    }

    fun toggleFilterSheet(isVisible: Boolean) {
        _uiState.update { it.copy(isFilterSheetVisible = isVisible) }
    }

    fun toggleMapView(isMap: Boolean) {
        _uiState.update { it.copy(isMapView = isMap) }
    }

    fun updatePriceRange(range: ClosedFloatingPointRange<Float>) {
        _uiState.update { it.copy(priceRange = range).applyFilters() }
    }

    fun toggleRoomType(type: String) {
        _uiState.update { state ->
            val set = state.selectedRoomTypes.toMutableSet()
            if (!set.add(type)) set.remove(type)
            state.copy(selectedRoomTypes = set).applyFilters()
        }
    }

    fun setGenderFilter(gender: String) {
        _uiState.update { it.copy(selectedGender = gender).applyFilters() }
    }

    fun toggleAmenity(amenity: String) {
        _uiState.update { state ->
            val set = state.selectedAmenities.toMutableSet()
            if (!set.add(amenity)) set.remove(amenity)
            state.copy(selectedAmenities = set).applyFilters()
        }
    }

    fun updateMaxDistance(distance: Float) {
        _uiState.update { it.copy(maxDistanceKm = distance).applyFilters() }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                query = "",
                priceRange = 500f..30000f,
                selectedRoomTypes = emptySet(),
                selectedGender = "Any",
                selectedAmenities = emptySet(),
                maxDistanceKm = 10f
            ).applyFilters()
        }
    }

    // Helper to keep logic clean and avoid duplicating filter loop
    private fun SearchUiState.applyFilters(
        listingsToFilter: List<PgListing> = this.allListings
    ): SearchUiState {
        val q = query.lowercase().trim()
        val filtered = listingsToFilter.filter { listing ->
            val matchesQuery = q.isEmpty() || 
                listing.title.lowercase().contains(q) || 
                listing.area.lowercase().contains(q) || 
                listing.city.lowercase().contains(q)
                
            val matchesPrice = listing.price.toFloat() in priceRange
            
            val matchesRoomType = selectedRoomTypes.isEmpty() || 
                selectedRoomTypes.contains(listing.roomType)
                
            val matchesGender = selectedGender == "Any" || 
                listing.genderAllowed.contains(selectedGender, ignoreCase = true)
                
            val matchesAmenities = selectedAmenities.isEmpty() || 
                listing.amenities.containsAll(selectedAmenities)
                
            // Note: Location filtering is a stub, normally uses Haversine formula based on actual user lat/lng
            // val matchesDistance = distanceKm(userLat, userLng, listing.lat, listing.lng) <= maxDistanceKm

            matchesQuery && matchesPrice && matchesRoomType && matchesGender && matchesAmenities
        }
        
        return this.copy(filteredListings = filtered)
    }

    fun toggleFavorite(listingId: String) {
        // TODO
    }
}
