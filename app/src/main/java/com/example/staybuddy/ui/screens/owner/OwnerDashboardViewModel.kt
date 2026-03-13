package com.example.staybuddy.ui.screens.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.PgListing
import com.example.staybuddy.data.repository.ImageStorageRepository
import com.example.staybuddy.data.repository.ListingRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.example.staybuddy.data.model.Inquiry
import com.example.staybuddy.data.repository.InquiryRepository
import javax.inject.Inject

data class OwnerDashboardUiState(
    val listings: List<PgListing> = emptyList(),
    val inquiries: List<Inquiry> = emptyList(),
    val totalListings: Int = 0,
    val activeListings: Int = 0,
    val messagesCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class OwnerDashboardViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val imageStorageRepository: ImageStorageRepository,
    private val inquiryRepository: InquiryRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerDashboardUiState())
    val uiState: StateFlow<OwnerDashboardUiState> = _uiState.asStateFlow()

    init {
        loadOwnerListings()
        loadOwnerInquiries()
    }

    private fun loadOwnerInquiries() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            inquiryRepository.getInquiriesForHost(userId)
                .catch { e ->
                    // Handle error if needed
                }
                .collect { inquiries ->
                    _uiState.value = _uiState.value.copy(inquiries = inquiries)
                }
        }
    }

    private fun loadOwnerListings() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "User not authenticated"
            )
            return
        }

        viewModelScope.launch {
            listingRepository.getListingsByOwner(userId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load listings"
                    )
                }
                .collect { listings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        listings = listings,
                        totalListings = listings.size,
                        activeListings = listings.count { it.isActive },
                        error = null
                    )
                }
        }
    }

    fun toggleListingActiveStatus(listing: PgListing) {
        viewModelScope.launch {
            val updatedListing = listing.copy(isActive = !listing.isActive)
            val result = listingRepository.updateListing(updatedListing)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(error = "Failed to update listing status")
            }
        }
    }

    fun deleteListing(listingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true) // Optional, but good UX
            
            // Delete associated images first
            val storageResult = imageStorageRepository.deleteListingImages(listingId)
            
            // Delete the document regardless to ensure it's removed
            val dbResult = listingRepository.deleteListing(listingId)
            
            if (dbResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete listing"
                )
            } else {
                 _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            }
        }
    }

    fun updateInquiryStatus(inquiryId: String, status: String) {
        viewModelScope.launch {
            val result = inquiryRepository.updateInquiryStatus(inquiryId, status)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(error = "Failed to update inquiry status")
            }
        }
    }
}
