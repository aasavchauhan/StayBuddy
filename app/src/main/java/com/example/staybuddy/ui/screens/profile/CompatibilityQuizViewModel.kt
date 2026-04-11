package com.example.staybuddy.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizUiState(
    val currentStep: Int = 0,
    val totalSteps: Int = 5,
    val sleepSchedule: String = "",
    val cleanlinessLevel: String = "",
    val foodPreference: String = "",
    val smokingDrinking: String = "",
    val guestsVisitors: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class CompatibilityQuizViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState = _uiState.asStateFlow()

    fun onAnswerSelected(answer: String) {
        val currentState = _uiState.value
        when (currentState.currentStep) {
            0 -> _uiState.value = currentState.copy(sleepSchedule = answer)
            1 -> _uiState.value = currentState.copy(cleanlinessLevel = answer)
            2 -> _uiState.value = currentState.copy(foodPreference = answer)
            3 -> _uiState.value = currentState.copy(smokingDrinking = answer)
            4 -> _uiState.value = currentState.copy(guestsVisitors = answer)
        }
        nextStep()
    }

    private fun nextStep() {
        if (_uiState.value.currentStep < _uiState.value.totalSteps - 1) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
        } else {
            submitQuiz()
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 0) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep - 1)
        }
    }

    private fun submitQuiz() {
        viewModelScope.launch {
            val user = authRepository.getUserFromFirestore(authRepository.currentUser?.uid ?: return@launch).getOrNull() ?: return@launch
            
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            
            val updatedUser = user.copy(
                sleepSchedule = _uiState.value.sleepSchedule,
                cleanlinessLevel = _uiState.value.cleanlinessLevel,
                foodPreference = _uiState.value.foodPreference,
                smokingDrinking = _uiState.value.smokingDrinking,
                guestsVisitors = _uiState.value.guestsVisitors
            )
            
            val result = authRepository.updateUser(updatedUser)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, isComplete = true)
            } else {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Failed to save results")
            }
        }
    }
}
