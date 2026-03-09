package com.example.staybuddy.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.User
import com.example.staybuddy.data.repository.AuthRepository
import com.example.staybuddy.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val role: String = "student",
    val gender: String = "",
    val city: String = "",
    val college: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegisterSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(name: String) { _uiState.value = _uiState.value.copy(name = name, errorMessage = null) }
    fun onEmailChange(email: String) { _uiState.value = _uiState.value.copy(email = email, errorMessage = null) }
    fun onPhoneChange(phone: String) { _uiState.value = _uiState.value.copy(phone = phone, errorMessage = null) }
    fun onPasswordChange(password: String) { _uiState.value = _uiState.value.copy(password = password, errorMessage = null) }
    fun onRoleChange(role: String) { _uiState.value = _uiState.value.copy(role = role) }
    fun onGenderChange(gender: String) { _uiState.value = _uiState.value.copy(gender = gender) }
    fun onCityChange(city: String) { _uiState.value = _uiState.value.copy(city = city) }
    fun onCollegeChange(college: String) { _uiState.value = _uiState.value.copy(college = college) }

    fun register() {
        val state = _uiState.value

        // Validation
        when {
            !ValidationUtils.isValidName(state.name) -> {
                _uiState.value = state.copy(errorMessage = "Name must be at least 2 characters")
                return
            }
            !ValidationUtils.isValidEmail(state.email) -> {
                _uiState.value = state.copy(errorMessage = "Invalid email address")
                return
            }
            !ValidationUtils.isValidPhone(state.phone) -> {
                _uiState.value = state.copy(errorMessage = "Phone must be 10 digits")
                return
            }
            !ValidationUtils.isValidPassword(state.password) -> {
                _uiState.value = state.copy(errorMessage = "Password must be 6+ chars with at least 1 number")
                return
            }
            state.role.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "Please select a role")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.signUpWithEmail(state.email.trim(), state.password)
            result.fold(
                onSuccess = { firebaseUser ->
                    val user = User(
                        userId = firebaseUser.uid,
                        name = state.name.trim(),
                        email = state.email.trim(),
                        phone = state.phone.trim(),
                        role = state.role,
                        gender = state.gender,
                        city = state.city.trim(),
                        college = state.college.trim()
                    )
                    val saveResult = authRepository.saveUserToFirestore(user)
                    saveResult.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(isLoading = false, isRegisterSuccess = true)
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = e.message ?: "Failed to save profile"
                            )
                        }
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Registration failed"
                    )
                }
            )
        }
    }
}
