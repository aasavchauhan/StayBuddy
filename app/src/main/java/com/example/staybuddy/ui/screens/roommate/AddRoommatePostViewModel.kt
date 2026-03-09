package com.example.staybuddy.ui.screens.roommate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.RoommatePost
import com.example.staybuddy.data.repository.RoommateRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.staybuddy.data.model.RoommatePostType

data class AddRoommatePostUiState(
    val city: String = "Vadodara",
    val location: String = "",
    val description: String = "",
    val priceShare: String = "",
    val availableBeds: String = "",
    val totalBeds: String = "",
    val roomType: String = "Shared",
    val postType: RoommatePostType = RoommatePostType.OFFER,
    val preferences: Map<String, String> = emptyMap(),
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isEditing: Boolean = false
)

@HiltViewModel
class AddRoommatePostViewModel @Inject constructor(
    private val roommateRepository: RoommateRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRoommatePostUiState())
    val uiState: StateFlow<AddRoommatePostUiState> = _uiState.asStateFlow()

    private val postId: String? = savedStateHandle.get<String>("postId")?.takeIf { it != "new" }

    init {
        if (postId != null) {
            _uiState.value = _uiState.value.copy(isEditing = true, isLoading = true)
            viewModelScope.launch {
                roommateRepository.getRoommatePostById(postId).onSuccess { post ->
                    if (post != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            city = post.city,
                            location = post.location,
                            description = post.description,
                            priceShare = post.priceShare.toString(),
                            availableBeds = post.availableBeds.toString(),
                            totalBeds = post.totalBeds.toString(),
                            roomType = post.roomType,
                            postType = post.postType,
                            preferences = post.preferences,
                            address = post.address,
                            latitude = post.latitude,
                            longitude = post.longitude
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Post not found")
                    }
                }.onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to fetch post")
                }
            }
        }
    }

    fun updateField(field: String, value: String) {
        _uiState.value = when (field) {
            "city" -> _uiState.value.copy(city = value)
            "location" -> _uiState.value.copy(location = value)
            "description" -> _uiState.value.copy(description = value)
            "priceShare" -> _uiState.value.copy(priceShare = value)
            "availableBeds" -> _uiState.value.copy(availableBeds = value)
            "totalBeds" -> _uiState.value.copy(totalBeds = value)
            "roomType" -> _uiState.value.copy(roomType = value)
            "address" -> _uiState.value.copy(address = value)
            else -> _uiState.value
        }
    }

    fun setPostType(type: RoommatePostType) {
        _uiState.value = _uiState.value.copy(postType = type)
    }
    
    fun updatePreference(key: String, value: String) {
        val currentPrefs = _uiState.value.preferences.toMutableMap()
        if (value.isBlank()) {
            currentPrefs.remove(key)
        } else {
            currentPrefs[key] = value
        }
        _uiState.value = _uiState.value.copy(preferences = currentPrefs)
    }

    fun submitPost() {
        val state = _uiState.value
        val userId = auth.currentUser?.uid
        
        if (userId == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }
        
        if (state.city.isBlank() || state.location.isBlank() || state.priceShare.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all required fields")
            return
        }

        val price = state.priceShare.toIntOrNull()
        val availableBeds = state.availableBeds.toIntOrNull() ?: 0
        val totalBeds = state.totalBeds.toIntOrNull() ?: 0
        
        if (price == null) {
            _uiState.value = _uiState.value.copy(error = "Price must be a valid number")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val post = RoommatePost(
                postId = postId ?: "",
                userId = userId,
                city = state.city,
                location = state.location,
                description = state.description,
                priceShare = price,
                availableBeds = availableBeds,
                totalBeds = totalBeds,
                roomType = state.roomType,
                postType = state.postType,
                preferences = state.preferences,
                address = state.address,
                latitude = state.latitude,
                longitude = state.longitude
            )
            
            val result = if (postId != null) {
                roommateRepository.updateRoommatePost(post)
            } else {
                roommateRepository.addRoommatePost(post).map { Unit }
            }
            
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save post"
                )
            }
        }
    }
    
    fun updateLocation(lat: Double, lon: Double, address: String) {
        _uiState.value = _uiState.value.copy(
            latitude = lat,
            longitude = lon,
            address = address
        )
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
