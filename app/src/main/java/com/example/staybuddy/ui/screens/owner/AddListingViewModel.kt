package com.example.staybuddy.ui.screens.owner

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.PgListing
import com.example.staybuddy.data.repository.AuthRepository
import com.example.staybuddy.data.repository.ImageStorageRepository
import com.example.staybuddy.data.repository.ListingRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddListingUiState(
    val currentStep: Int = 1,
    
    // Step 1
    val name: String = "",
    val description: String = "",
    val city: String = "Vadodara",
    val area: String = "",
    
    // Step 2
    val monthlyRent: String = "",
    val depositAmount: String = "",
    val roomType: String = "Single",
    val genderAllowed: String = "Any",
    
    // Step 3
    val amenities: Set<String> = emptySet(),
    
    // Step 4
    val imageUris: List<Uri> = emptyList(),
    val existingImageUrls: List<String> = emptyList(), // For editing
    
    // Step 5
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isEditing: Boolean = false
)

@HiltViewModel
class AddListingViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val imageStorageRepository: ImageStorageRepository,
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddListingUiState())
    val uiState: StateFlow<AddListingUiState> = _uiState.asStateFlow()

    private val listingId: String? = savedStateHandle.get<String>("listingId")?.takeIf { it != "new" }

    init {
        if (listingId != null) {
            _uiState.value = _uiState.value.copy(isEditing = true, isLoading = true)
            viewModelScope.launch {
                listingRepository.getListingById(listingId).onSuccess { listing ->
                    if (listing != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            name = listing.title,
                            description = listing.description,
                            city = listing.city,
                            area = listing.area,
                            monthlyRent = listing.price.toString(),
                            depositAmount = listing.deposit.toString(),
                            roomType = listing.roomType,
                            genderAllowed = listing.genderAllowed,
                            amenities = listing.amenities.toSet(),
                            existingImageUrls = listing.images,
                            latitude = listing.latitude,
                            longitude = listing.longitude
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Listing not found")
                    }
                }.onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to fetch listing")
                }
            }
        }
    }

    fun updateField(field: String, value: String) {
        _uiState.value = when (field) {
            "name" -> _uiState.value.copy(name = value)
            "description" -> _uiState.value.copy(description = value)
            "city" -> _uiState.value.copy(city = value)
            "area" -> _uiState.value.copy(area = value)
            "monthlyRent" -> _uiState.value.copy(monthlyRent = value)
            "depositAmount" -> _uiState.value.copy(depositAmount = value)
            "roomType" -> _uiState.value.copy(roomType = value)
            "genderAllowed" -> _uiState.value.copy(genderAllowed = value)
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

    fun addImages(uris: List<Uri>) {
        val current = _uiState.value.imageUris.toMutableList()
        current.addAll(uris)
        _uiState.value = _uiState.value.copy(imageUris = current)
    }

    fun removeImage(uri: Uri) {
        val current = _uiState.value.imageUris.toMutableList()
        current.remove(uri)
        _uiState.value = _uiState.value.copy(imageUris = current)
    }

    fun removeExistingImage(url: String) {
         val current = _uiState.value.existingImageUrls.toMutableList()
         current.remove(url)
         _uiState.value = _uiState.value.copy(existingImageUrls = current)
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(latitude = latitude, longitude = longitude)
    }

    fun nextStep() {
        val state = _uiState.value
        if (state.currentStep == 1) {
            if (state.name.isBlank() || state.description.isBlank() || state.city.isBlank() || state.area.isBlank()) {
                _uiState.value = state.copy(error = "Please fill all fields in Step 1")
                return
            }
        } else if (state.currentStep == 2) {
            if (state.monthlyRent.isBlank() || state.depositAmount.isBlank()) {
                _uiState.value = state.copy(error = "Please fill all pricing fields")
                return
            }
            if (state.monthlyRent.toIntOrNull() == null || state.monthlyRent.toInt() <= 0) {
                _uiState.value = state.copy(error = "Monthly rent must be a valid positive number")
                return
            }
            if (state.depositAmount.toIntOrNull() == null || state.depositAmount.toInt() < 0) {
                _uiState.value = state.copy(error = "Deposit must be a valid non-negative number")
                return
            }
        } else if (state.currentStep == 4) {
            val totalImages = state.imageUris.size + state.existingImageUrls.size
            if (totalImages < 3) {
                _uiState.value = state.copy(error = "Please select at least 3 images")
                return
            }
        } else if (state.currentStep == 5) {
            if (state.latitude == 0.0 || state.longitude == 0.0) {
               _uiState.value = state.copy(error = "Please explicitly pin the location on the map")
               return
            }
        }
        
        if (state.currentStep < 5) {
            _uiState.value = state.copy(currentStep = state.currentStep + 1, error = null)
        }
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
            // Determine listing ID early because storage needs it
            val targetListingId = listingId ?: UUID.randomUUID().toString()
            
            // Upload new images to Cloudinary
            val uploadedUrls = mutableListOf<String>()
            if (state.imageUris.isNotEmpty()) {
                val uploadResult = imageStorageRepository.uploadListingImages(state.imageUris, targetListingId)
                uploadResult.onSuccess { urls ->
                    uploadedUrls.addAll(urls)
                }.onFailure { e ->
                    _uiState.value = state.copy(isLoading = false, error = "Cloudinary upload failed: ${e.message}")
                    return@launch
                }
            }
            
            val finalImageUrls = state.existingImageUrls + uploadedUrls
            
            val ownerProfile = authRepository.getUserFromFirestore(userId).getOrNull()
            
            val listing = PgListing(
                listingId = targetListingId,
                ownerId = userId,
                title = state.name,
                description = state.description,
                city = state.city,
                area = state.area,
                latitude = state.latitude,
                longitude = state.longitude,
                price = state.monthlyRent.toIntOrNull() ?: 0,
                deposit = state.depositAmount.toIntOrNull() ?: 0,
                roomType = state.roomType,
                genderAllowed = state.genderAllowed,
                amenities = state.amenities.toList(),
                images = finalImageUrls,
                ownerName = ownerProfile?.name ?: "Property Owner",
                ownerProfileImage = ownerProfile?.profileImage ?: "",
                ownerPhone = ownerProfile?.phone ?: "",
                isActive = true
            )
            
            // Upsert in Firestore
            val result = listingRepository.updateListing(listing)
            
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save listing"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
