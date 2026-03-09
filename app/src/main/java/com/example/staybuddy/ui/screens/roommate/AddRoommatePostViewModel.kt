package com.example.staybuddy.ui.screens.roommate

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

data class AddRoommatePostUiState(
    val city: String = "",
    val location: String = "",
    val priceShare: String = "",
    val availableBeds: String = "",
    val preferences: Map<String, String> = emptyMap(),
    
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AddRoommatePostViewModel @Inject constructor(
    private val roommateRepository: RoommateRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRoommatePostUiState())
    val uiState: StateFlow<AddRoommatePostUiState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.value = when (field) {
            "city" -> _uiState.value.copy(city = value)
            "location" -> _uiState.value.copy(location = value)
            "priceShare" -> _uiState.value.copy(priceShare = value)
            "availableBeds" -> _uiState.value.copy(availableBeds = value)
            else -> _uiState.value
        }
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
        
        if (state.city.isBlank() || state.location.isBlank() || state.priceShare.isBlank() || state.availableBeds.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all required fields")
            return
        }

        val price = state.priceShare.toIntOrNull()
        val beds = state.availableBeds.toIntOrNull()
        
        if (price == null || beds == null) {
            _uiState.value = _uiState.value.copy(error = "Price and beds must be valid numbers")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val post = RoommatePost(
                userId = userId,
                city = state.city,
                location = state.location,
                priceShare = price,
                availableBeds = beds,
                preferences = state.preferences
            )
            
            val result = roommateRepository.addRoommatePost(post)
            
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create post"
                )
            }
        }
    }
    
    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
