package com.example.staybuddy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.repository.AuthRepository
import com.example.staybuddy.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val userRole: String = Constants.ROLE_STUDENT,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshSession()
    }

    fun refreshSession() {
        val currentUser = authRepository.currentUser
        if (currentUser == null) {
            _uiState.value = MainUiState(isLoading = false, isLoggedIn = false)
            return
        }

        viewModelScope.launch {
            val result = authRepository.getUserFromFirestore(currentUser.uid)
            result.onSuccess { user ->
                _uiState.value = MainUiState(
                    userRole = user?.role ?: Constants.ROLE_STUDENT,
                    isLoggedIn = true,
                    isLoading = false
                )
            }.onFailure {
                _uiState.value = MainUiState(
                    userRole = Constants.ROLE_STUDENT,
                    isLoggedIn = true,
                    isLoading = false
                )
            }
        }
    }
}
