package com.example.staybuddy.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.User
import com.example.staybuddy.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FinishRegistrationUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "student",
    val gender: String = "Male",
    val city: String = "",
    val college: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class FinishRegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinishRegistrationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Pre-fill from Firebase
        authRepository.currentUser?.let { firebaseUser ->
            _uiState.value = _uiState.value.copy(
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: ""
            )
        }
    }

    fun onNameChange(name: String) = updateState { it.copy(name = name) }
    fun onPhoneChange(phone: String) = updateState { it.copy(phone = phone) }
    fun onRoleChange(role: String) = updateState { it.copy(role = role) }
    fun onGenderChange(gender: String) = updateState { it.copy(gender = gender) }
    fun onCityChange(city: String) = updateState { it.copy(city = city) }
    fun onCollegeChange(college: String) = updateState { it.copy(college = college) }

    private fun updateState(update: (FinishRegistrationUiState) -> FinishRegistrationUiState) {
        _uiState.value = update(_uiState.value)
    }

    fun finishRegistration() {
        val state = _uiState.value
        if (state.name.isBlank() || state.phone.isBlank() || state.city.isBlank()) {
            updateState { it.copy(errorMessage = "Please fill in all required fields") }
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            val firebaseUser = authRepository.currentUser
            if (firebaseUser == null) {
                updateState { it.copy(isLoading = false, errorMessage = "User not authenticated") }
                return@launch
            }

            val user = User(
                userId = firebaseUser.uid,
                name = state.name,
                email = state.email,
                phone = state.phone,
                role = state.role,
                gender = state.gender,
                city = state.city,
                college = if (state.role == "student") state.college else ""
            )

            val result = authRepository.saveUserToFirestore(user)
            result.fold(
                onSuccess = {
                    updateState { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { e ->
                    updateState { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to save profile") }
                }
            )
        }
    }
}
