package com.example.staybuddy.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.User
import com.example.staybuddy.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import com.example.staybuddy.data.repository.FavoriteRepository
import com.example.staybuddy.data.repository.InquiryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val favoritesCount: Int = 0,
    val inquiriesCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository,
    private val inquiryRepository: InquiryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        refreshUserProfile()
    }

    fun refreshUserProfile() {
        val currentUserId = authRepository.currentUser?.uid
        if (currentUserId == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "User not logged in"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Get user profile
            val userResult = authRepository.getUserFromFirestore(currentUserId)
            
            // Collect stats from repositories
            combine(
                favoriteRepository.getFavoriteListingIds(),
                inquiryRepository.getInquiriesForUser(currentUserId)
            ) { favorites, inquiries ->
                Pair(favorites.size, inquiries.size)
            }.collect { (favCount, inqCount) ->
                val user = userResult.getOrNull()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user,
                    favoritesCount = favCount,
                    inquiriesCount = inqCount,
                    error = if (user == null && userResult.isSuccess) "User profile not found" else userResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun logout() {
        authRepository.signOut()
    }
}
