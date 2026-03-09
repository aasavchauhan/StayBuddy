package com.example.staybuddy.ui.screens.listing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.PgListing
import com.example.staybuddy.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListingDetailUiState(
    val listing: PgListing? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false
)

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val listingId: String = checkNotNull(savedStateHandle["listingId"])

    private val _uiState = MutableStateFlow(ListingDetailUiState())
    val uiState: StateFlow<ListingDetailUiState> = _uiState.asStateFlow()

    init {
        loadListing()
    }

    private fun loadListing() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = listingRepository.getListingById(listingId)
            
            result.onSuccess { listing ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    listing = listing
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load listing"
                )
            }
        }
    }
    
    fun toggleFavorite() {
        // TODO: Implement favorites repository call
        _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite)
    }
}
