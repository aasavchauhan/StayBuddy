package com.example.staybuddy.ui.screens.home

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
import javax.inject.Inject

data class HomeUiState(
    val recommendedListings: List<PgListing> = emptyList(),
    val nearbyListings: List<PgListing> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val listingRepository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
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
                    // For now, split the listings between recommended and nearby
                    // In a real app, nearby would be based on geolocation
                    val recommended = listings.take(3)
                    val nearby = if (listings.size > 3) listings.drop(3) else listings

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recommendedListings = recommended,
                        nearbyListings = nearby
                    )
                }
        }
    }
    
    fun toggleFavorite(listingId: String) {
        // TODO: Implement favorites logic when FavoritesRepository is ready
    }
}
