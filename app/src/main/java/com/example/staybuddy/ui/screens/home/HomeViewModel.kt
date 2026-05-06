package com.example.staybuddy.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.PgListing
import com.example.staybuddy.data.manager.PreferenceManager
import com.example.staybuddy.data.manager.UpdateChecker
import com.example.staybuddy.data.manager.UpdateInfo
import com.example.staybuddy.data.repository.ListingRepository
import com.example.staybuddy.util.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*
import com.example.staybuddy.data.repository.FavoriteRepository
import com.example.staybuddy.data.repository.AuthRepository
import com.example.staybuddy.data.model.User
import kotlinx.coroutines.flow.update
import com.example.staybuddy.utils.Constants
import com.example.staybuddy.data.repository.LocationRepository
import com.example.staybuddy.domain.model.AutocompletePrediction
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

data class HomeUiState(
    val recommendedListings: List<PgListing> = emptyList(),
    val nearbyListings: List<PgListing> = emptyList(),
    val listingsWithDistance: List<Pair<PgListing, Double?>> = emptyList(),
    val selectedCity: String = "Vadodara",
    val selectedUniversity: String? = null,
    val userLocation: Pair<Double, Double>? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val favoriteIds: Set<String> = emptySet(),
    val userName: String = "",
    val userRole: String = Constants.ROLE_STUDENT,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val searchResults: List<AutocompletePrediction> = emptyList(),
    val isSearching: Boolean = false
)

val CITIES = listOf("Vadodara", "Ahmedabad", "Surat", "Rajkot", "Gandhinagar", "Mumbai", "Pune", "Bangalore")
val UNIVERSITIES = listOf(
    "MS University", "Parul University", "Navrachana University", 
    "IIT Bombay", "Pune University", "DA-IICT", "Nirma University"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val preferenceManager: PreferenceManager,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
    private val updateChecker: UpdateChecker,
    private val analyticsHelper: AnalyticsHelper,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _searchQueryFlow = MutableStateFlow("")

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val updateInfo: StateFlow<UpdateInfo> = updateChecker.updateInfo

    init {
        observePreferences()
        observeFavorites()
        fetchUserName()
        checkForUpdates()
        observeSearchQuery()
        analyticsHelper.logScreenView("home")
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            updateChecker.checkForUpdate()
        }
    }

    fun onUpdateClicked() {
        updateChecker.openDownloadUrl()
    }

    fun dismissUpdate() {
        // No-op: the dialog just closes. Could track dismissal in analytics.
        analyticsHelper.logEvent("update_dismissed")
    }

    private fun fetchUserName() {
        viewModelScope.launch {
            authRepository.currentUser?.uid?.let { uid ->
                authRepository.getUserFromFirestore(uid).onSuccess { user ->
                    user?.let {
                        _uiState.update { it.copy(
                            userName = user.name,
                            userRole = user.role.ifEmpty { Constants.ROLE_STUDENT }
                        ) }
                    }
                }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                preferenceManager.selectedCity,
                preferenceManager.selectedUniversity,
                preferenceManager.lastLocation
            ) { city, university, location ->
                Triple(city, university, location)
            }.collect { (city, university, location) ->
                _uiState.value = _uiState.value.copy(
                    selectedCity = city ?: "Vadodara",
                    selectedUniversity = university
                )
                // Refresh data if location/city changed significantly if needed
                loadData()
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            try {
                favoriteRepository.getFavoriteListingIds()
                    .catch { /* Handle Firestore permission errors gracefully */ }
                    .collect { ids ->
                        _uiState.value = _uiState.value.copy(favoriteIds = ids.toSet())
                    }
            } catch (e: Exception) {
                // Ignore favorites loading error - non-critical feature
            }
        }
    }

    fun loadData() {
        val currentState = _uiState.value
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
                    val userLocation = currentState.userLocation ?: currentState.selectedUniversity?.let { 
                        // Mock University coordinates
                        when(it) {
                            "MS University" -> Pair(22.3106, 73.1812)
                            "Parul University" -> Pair(22.2894, 73.3643)
                            else -> Pair(22.3072, 73.1812)
                        }
                    }

                    val listingsWithDist = listings.map { listing ->
                        val distance = if (listing.latitude != 0.0 && listing.longitude != 0.0 && userLocation != null) {
                            calculateDistance(
                                listing.latitude, listing.longitude,
                                userLocation.first, userLocation.second
                            )
                        } else null
                        listing to distance
                    }.sortedBy { it.second ?: Double.MAX_VALUE }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recommendedListings = listings.take(3),
                        nearbyListings = listings.filter { 
                            it.city.contains(currentState.selectedCity, ignoreCase = true) &&
                            (currentState.selectedCategory == "All" || it.roomType.equals(currentState.selectedCategory, ignoreCase = true))
                        },
                        listingsWithDistance = listingsWithDist,
                        userLocation = userLocation
                    )
                }
        }
    }

    fun updateCity(city: String) {
        viewModelScope.launch {
            preferenceManager.setSelectedCity(city)
        }
    }

    fun updateUniversity(uni: String) {
        viewModelScope.launch {
            preferenceManager.setSelectedUniversity(uni)
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
    
    fun toggleFavorite(listingId: String) {
        viewModelScope.launch {
            if (_uiState.value.favoriteIds.contains(listingId)) {
                favoriteRepository.removeFavorite(listingId)
            } else {
                favoriteRepository.addFavorite(listingId)
            }
        }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadData()
    }


    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQueryFlow
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                        return@collectLatest
                    }
                    _uiState.update { it.copy(isSearching = true) }
                    val result = locationRepository.searchLocation(query)
                    if (result.isSuccess) {
                        _uiState.update { 
                            it.copy(
                                searchResults = result.getOrNull() ?: emptyList(),
                                isSearching = false
                            ) 
                        }
                    } else {
                        _uiState.update { it.copy(isSearching = false) }
                    }
                }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        _searchQueryFlow.value = newQuery
    }
}
