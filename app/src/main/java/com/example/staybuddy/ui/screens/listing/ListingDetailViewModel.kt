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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.staybuddy.data.model.Inquiry
import com.example.staybuddy.data.repository.FavoriteRepository
import com.example.staybuddy.data.repository.InquiryRepository
import com.example.staybuddy.data.repository.ReportRepository
import com.google.firebase.auth.FirebaseAuth

data class ListingDetailUiState(
    val listing: PgListing? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val isInquirySent: Boolean = false,
    val isReportSent: Boolean = false,
    val reportError: String? = null,
    val hasAlreadyReported: Boolean = false
)

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val favoriteRepository: FavoriteRepository,
    private val inquiryRepository: InquiryRepository,
    private val reportRepository: ReportRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {



    private val listingId: String = checkNotNull(savedStateHandle["listingId"])

    private val _uiState = MutableStateFlow(ListingDetailUiState())
    val uiState: StateFlow<ListingDetailUiState> = _uiState.asStateFlow()

    init {
        loadListing()
        observeFavoriteStatus()
        checkReportStatus()
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
    
    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            favoriteRepository.getFavoriteListingIds()
                .catch { /* ignore */ }
                .collect { ids ->
                    _uiState.value = _uiState.value.copy(isFavorite = ids.contains(listingId))
                }
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            // Optimistically toggle locally to reduce visual lag
            val wasFavorite = _uiState.value.isFavorite
            _uiState.value = _uiState.value.copy(isFavorite = !wasFavorite)

            val result = if (wasFavorite) {
                favoriteRepository.removeFavorite(listingId)
            } else {
                favoriteRepository.addFavorite(listingId)
            }
            
            if (result.isFailure) {
                // Revert if failed
                _uiState.value = _uiState.value.copy(isFavorite = wasFavorite)
            }
        }
    }

    fun sendInquiry(moveInDate: Long, roomType: String, message: String) {
        val currentListing = _uiState.value.listing ?: return
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val inquiry = Inquiry(
                listingId = currentListing.listingId,
                userId = userId,
                hostId = currentListing.ownerId,
                moveInDate = moveInDate,
                roomType = roomType,
                message = message
            )
            val result = inquiryRepository.sendInquiry(inquiry)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isInquirySent = true, error = null)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to send inquiry")
            }
        }
    }

    private fun checkReportStatus() {
        viewModelScope.launch {
            val reported = reportRepository.hasUserReported(listingId)
            _uiState.value = _uiState.value.copy(hasAlreadyReported = reported)
        }
    }

    fun reportListing(reason: String) {
        viewModelScope.launch {
            val result = reportRepository.reportListing(listingId, reason)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isReportSent = true,
                    hasAlreadyReported = true,
                    reportError = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    reportError = e.message ?: "Failed to submit report"
                )
            }
        }
    }
}
