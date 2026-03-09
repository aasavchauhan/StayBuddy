package com.example.staybuddy.ui.screens.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.PgListing
import com.example.staybuddy.data.repository.ListingRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddListingUiState(
    val currentStep: Int = 1,
    val name: String = "",
    val type: String = "PG", // PG or Hostel
    val city: String = "",
    val location: String = "",
    val address: String = "",
    
    val description: String = "",
    val monthlyRent: String = "",
    val depositAmount: String = "",
    
    val amenities: Set<String> = emptySet(),
    val imageUrls: String = "", // comma-separated optional
    
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AddListingViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddListingUiState())
    val uiState: StateFlow<AddListingUiState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.value = when (field) {
            "name" -> _uiState.value.copy(name = value)
            "type" -> _uiState.value.copy(type = value)
            "city" -> _uiState.value.copy(city = value)
            "location" -> _uiState.value.copy(location = value)
            "address" -> _uiState.value.copy(address = value)
            "description" -> _uiState.value.copy(description = value)
            "monthlyRent" -> _uiState.value.copy(monthlyRent = value)
            "depositAmount" -> _uiState.value.copy(depositAmount = value)
            "imageUrls" -> _uiState.value.copy(imageUrls = value)
            else -> _uiState.value
        }
    }

    fun toggleAmenity(amenity: String) {
        val currentAmenities = _uiState.value.amenities.toMutableSet()
        if (currentAmenities.contains(amenity)) {
            currentAmenities.remove(amenity)
        } else {
            currentAmenities.add(amenity)
        }
        _uiState.value = _uiState.value.copy(amenities = currentAmenities)
    }

    fun nextStep() {
        val state = _uiState.value
        if (state.currentStep == 1) {
            if (state.name.isBlank() || state.city.isBlank() || state.location.isBlank() || state.address.isBlank()) {
                _uiState.value = state.copy(error = "Please fill all fields")
                return
            }
        } else if (state.currentStep == 2) {
            if (state.description.isBlank() || state.monthlyRent.isBlank() || state.depositAmount.isBlank()) {
                _uiState.value = state.copy(error = "Please fill all fields")
                return
            }
            if (state.monthlyRent.toIntOrNull() == null || state.depositAmount.toIntOrNull() == null) {
                _uiState.value = state.copy(error = "Rent and Deposit must be valid numbers")
                return
            }
        }
        
        _uiState.value = state.copy(currentStep = state.currentStep + 1, error = null)
    }

    fun previousStep() {
        val state = _uiState.value
        if (state.currentStep > 1) {
            _uiState.value = state.copy(currentStep = state.currentStep - 1, error = null)
        }
    }

    fun submitListing() {
        val state = _uiState.value
        val userId = auth.currentUser?.uid
        
        if (userId == null) {
            _uiState.value = state.copy(error = "User not authenticated")
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val urls = state.imageUrls.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val placeholderUrls = if (urls.isEmpty()) {
                listOf("https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&q=80&w=800")
            } else urls
            
            val listing = PgListing(
                ownerId = userId,
                title = state.name,
                roomType = state.type,
                area = "${state.location}, ${state.address}",
                city = state.city,
                description = state.description,
                price = state.monthlyRent.toIntOrNull() ?: 0,
                deposit = state.depositAmount.toIntOrNull() ?: 0,
                amenities = state.amenities.toList(),
                images = placeholderUrls,
                isActive = true
            )
            
            val result = listingRepository.addListing(listing)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to add listing"
                )
            }
        }
    }
    
    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
