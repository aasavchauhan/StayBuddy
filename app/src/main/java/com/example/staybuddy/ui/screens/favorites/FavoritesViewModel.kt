package com.example.staybuddy.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.PgListing
import com.example.staybuddy.data.repository.FavoriteRepository
import com.example.staybuddy.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favoriteListings: List<PgListing> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val listingRepository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                // Combine all listings and favorite IDs
                combine(
                    listingRepository.getListings().catch { emit(emptyList()) },
                    favoriteRepository.getFavoriteListingIds().catch { emit(emptyList()) }
                ) { allListings, favoriteIds ->
                    val favorites = allListings.filter { it.listingId in favoriteIds }
                    _uiState.value.copy(
                        favoriteListings = favorites,
                        favoriteIds = favoriteIds.toSet(),
                        isLoading = false
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load favorites"
                )
            }
        }
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
}
