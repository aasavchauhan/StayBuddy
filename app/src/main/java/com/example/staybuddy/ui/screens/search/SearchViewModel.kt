package com.example.staybuddy.ui.screens.search

import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.PgListing
import com.example.staybuddy.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.staybuddy.data.repository.FavoriteRepository
import com.example.staybuddy.domain.model.AutocompletePrediction
import com.example.staybuddy.data.manager.SearchHistoryManager
import com.example.staybuddy.data.manager.PreferenceManager
import com.example.staybuddy.data.repository.LocationRepository
import com.example.staybuddy.utils.Constants
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class SearchUiState(
    val query: String = "",
    val allListings: List<PgListing> = emptyList(),
    val filteredListings: List<PgListing> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Location Search
    val locationPredictions: List<AutocompletePrediction> = emptyList(),
    val selectedLocationContext: AutocompletePrediction? = null,
    val recentSearches: List<AutocompletePrediction> = emptyList(),
    
    // Filters
    val priceRange: ClosedFloatingPointRange<Float> = 500f..30000f,
    val selectedRoomTypes: Set<String> = emptySet(),
    val selectedGender: String = "Any",
    val selectedAmenities: Set<String> = emptySet(),
    val maxDistanceKm: Float = 10f,
    val sortOption: SortOption = SortOption.NEWEST,
    val isOffline: Boolean = false,
    
    // UI State
    val isFilterSheetVisible: Boolean = false,
    val isMapView: Boolean = false,
    val selectedListing: PgListing? = null
)

enum class SortOption(val displayName: String) {
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    NEWEST("Newest First"),
    NEAREST("Nearest to Me")
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val locationRepository: LocationRepository,
    private val searchHistoryManager: SearchHistoryManager,
    private val preferenceManager: PreferenceManager,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadData()
        observeSearchHistory()
        observeFavorites()
        restoreFilters()
    }

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private fun observeFavorites() {
        viewModelScope.launch {
            favoriteRepository.getFavoriteListingIds()
                .catch { emit(emptyList()) }
                .collect { ids ->
                    _favoriteIds.value = ids.toSet()
                }
        }
    }

    private fun restoreFilters() {
        viewModelScope.launch {
            // Combine all filter flows from preferenceManager and update state once
            combine(
                preferenceManager.filterPriceRange,
                preferenceManager.selectedRoomTypes,
                preferenceManager.selectedGender,
                preferenceManager.selectedAmenities,
                preferenceManager.sortOption
            ) { priceRange, roomTypes, gender, amenities, sortOption ->
                _uiState.update { 
                    it.copy(
                        priceRange = priceRange.first..priceRange.second,
                        selectedRoomTypes = roomTypes,
                        selectedGender = gender,
                        selectedAmenities = amenities,
                        sortOption = try { SortOption.valueOf(sortOption) } catch (e: Exception) { SortOption.NEWEST }
                    ).applyFilters()
                }
            }.first() // Only restore once on init
        }
    }

    private fun observeSearchHistory() {
        viewModelScope.launch {
            searchHistoryManager.getSearchHistory.collect { history ->
                _uiState.update { it.copy(recentSearches = history) }
            }
        }
    }

    private fun checkOfflineStatus() {
        // Simple check: if list is empty or from cache only, we could set isOffline
        // For now, listingRepository.getListings() emits cache then network.
        // We can use a more robust network observer if needed, but Hilt makes it hard without context.
        // Let's assume repo handles fetching and we just display what we have.
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
        // Clear predictions and selected location when user types something new
        _uiState.update { 
            it.copy(
                query = newQuery, 
                locationPredictions = emptyList(),
                selectedLocationContext = null
            ).applyFilters() 
        }

        searchJob?.cancel()
        if (newQuery.length > 2) {
            searchJob = viewModelScope.launch {
                delay(300)
                locationRepository.searchLocation(newQuery).onSuccess { predictions ->
                    _uiState.update { it.copy(locationPredictions = predictions) }
                }
            }
        }
    }

    fun selectLocation(prediction: AutocompletePrediction) {
        // When a user selects a prediction, set the query to the primary text, clear predictions, and set selectedLocationContext
        _uiState.update { 
            it.copy(
                query = prediction.primaryText,
                locationPredictions = emptyList(),
                selectedLocationContext = prediction
            ).applyFilters()
        }
        
        // Save to history
        viewModelScope.launch {
            searchHistoryManager.addSearchPrediction(prediction)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryManager.clearHistory()
        }
    }

    fun removeFromHistory(prediction: AutocompletePrediction) {
        viewModelScope.launch {
            searchHistoryManager.removeSearchPrediction(prediction)
        }
    }

    fun toggleFilterSheet(isVisible: Boolean) {
        _uiState.update { it.copy(isFilterSheetVisible = isVisible) }
    }

    fun toggleMapView(isMap: Boolean) {
        _uiState.update { it.copy(isMapView = isMap, selectedListing = null) }
    }

    fun selectListing(listing: PgListing?) {
        _uiState.update { it.copy(selectedListing = listing) }
    }

    fun selectListingById(listingId: String?) {
        val listing = _uiState.value.filteredListings.find { it.listingId == listingId }
        _uiState.update { it.copy(selectedListing = listing) }
    }

    fun updatePriceRange(range: ClosedFloatingPointRange<Float>) {
        _uiState.update { it.copy(priceRange = range).applyFilters() }
        viewModelScope.launch {
            preferenceManager.setFilterPriceRange(range.start, range.endInclusive)
        }
    }

    fun toggleRoomType(type: String) {
        _uiState.update { state ->
            val set = state.selectedRoomTypes.toMutableSet()
            if (!set.add(type)) set.remove(type)
            val newState = state.copy(selectedRoomTypes = set).applyFilters()
            viewModelScope.launch {
                preferenceManager.setSelectedRoomTypes(set)
            }
            newState
        }
    }

    fun setGenderFilter(gender: String) {
        _uiState.update { it.copy(selectedGender = gender).applyFilters() }
        viewModelScope.launch {
            preferenceManager.setSelectedGender(gender)
        }
    }

    fun toggleAmenity(amenity: String) {
        _uiState.update { state ->
            val set = state.selectedAmenities.toMutableSet()
            if (!set.add(amenity)) set.remove(amenity)
            val newState = state.copy(selectedAmenities = set).applyFilters()
            viewModelScope.launch {
                preferenceManager.setSelectedAmenities(set)
            }
            newState
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
                maxDistanceKm = 10f,
                sortOption = SortOption.NEWEST
            ).applyFilters()
        }
        viewModelScope.launch {
            preferenceManager.setFilterPriceRange(500f, 30000f)
            preferenceManager.setSelectedRoomTypes(emptySet())
            preferenceManager.setSelectedGender("Any")
            preferenceManager.setSelectedAmenities(emptySet())
            preferenceManager.setSortOption(SortOption.NEWEST.name)
        }
    }

    fun updateSortOption(option: SortOption) {
        _uiState.update { it.copy(sortOption = option).applyFilters() }
        viewModelScope.launch {
            preferenceManager.setSortOption(option.name)
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
                
            // Location filtering based on selectedLocationContext
            val distanceMatch = selectedLocationContext?.let { loc ->
                distanceKm(loc.lat, loc.lon, listing.latitude, listing.longitude) <= maxDistanceKm
            } ?: true

            matchesQuery && matchesPrice && matchesRoomType && matchesGender && matchesAmenities && distanceMatch
        }
        
        // Sorting logic
        val sorted = when (sortOption) {
            SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.price }
            SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.price }
            SortOption.NEWEST -> filtered.sortedByDescending { it.createdAt } // Assuming createdAt exists
            SortOption.NEAREST -> {
                // If we have search location context, sort by distance to it
                selectedLocationContext?.let { loc ->
                    filtered.sortedBy { distanceKm(loc.lat, loc.lon, it.latitude, it.longitude) }
                } ?: filtered
            }
        }
        
        return this.copy(filteredListings = sorted)
    }


    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    fun toggleFavorite(listingId: String) {
        viewModelScope.launch {
            if (_favoriteIds.value.contains(listingId)) {
                favoriteRepository.removeFavorite(listingId)
            } else {
                favoriteRepository.addFavorite(listingId)
            }
        }
    }
}
