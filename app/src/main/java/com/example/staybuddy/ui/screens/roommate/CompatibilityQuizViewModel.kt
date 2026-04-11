package com.example.staybuddy.ui.screens.roommate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>
)

data class CompatibilityQuizUiState(
    val questions: List<QuizQuestion> = listOf(
        QuizQuestion("smoker", "Do you smoke?", listOf("Yes", "No", "Occasionally")),
        QuizQuestion("drinking", "How often do you drink?", listOf("Regularly", "Occasionally", "Never")),
        QuizQuestion("pets", "Are you okay with pets?", listOf("Yes", "No", "Only small pets")),
        QuizQuestion("food", "Food preference?", listOf("Veg", "Non-Veg", "Vegan")),
        QuizQuestion("cleaning", "Cleaning frequency?", listOf("Daily", "Weekly", "Monthly")),
        QuizQuestion("sleep", "Sleep schedule?", listOf("Early Bird", "Night Owl", "Flexible")),
        QuizQuestion("social", "Social preference at home?", listOf("Quiet", "Friendly", "Party Person"))
    ),
    val currentQuestionIndex: Int = 0,
    val answers: MutableMap<String, Int> = mutableMapOf(),
    val isSaving: Boolean = false,
    val quizCompleted: Boolean = false
)

@HiltViewModel
class CompatibilityQuizViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompatibilityQuizUiState())
    val uiState: StateFlow<CompatibilityQuizUiState> = _uiState.asStateFlow()

    fun onAnswerSelected(questionId: String, answerIndex: Int) {
        val currentAnswers = _uiState.value.answers.toMutableMap()
        currentAnswers[questionId] = answerIndex
        _uiState.value = _uiState.value.copy(answers = currentAnswers)
        
        if (_uiState.value.currentQuestionIndex < _uiState.value.questions.size - 1) {
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = _uiState.value.currentQuestionIndex + 1
            )
        } else {
            saveQuizResults()
        }
    }

    fun onPreviousQuestion() {
        if (_uiState.value.currentQuestionIndex > 0) {
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = _uiState.value.currentQuestionIndex - 1
            )
        }
    }

    private fun saveQuizResults() {
        val uid = auth.currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(isSaving = true)
        
        viewModelScope.launch {
            authRepository.getUserFromFirestore(uid).onSuccess { user ->
                user?.let {
                    // Map quiz answers back to individual User fields
                    val answers = _uiState.value.answers
                    val updatedUser = it.copy(
                        sleepSchedule = mapAnswer("sleep", answers),
                        cleanlinessLevel = mapAnswer("cleaning", answers),
                        foodPreference = mapAnswer("food", answers),
                        smokingDrinking = mapAnswer("smoker", answers),
                        guestsVisitors = mapAnswer("social", answers)
                    )
                    authRepository.updateUser(updatedUser).onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            quizCompleted = true
                        )
                    }
                }
            }
        }
    }

    private fun mapAnswer(key: String, answers: Map<String, Int>): String {
        val index = answers[key] ?: return ""
        return when (key) {
            "sleep" -> when (index) { 0 -> "early_bird"; 1 -> "night_owl"; else -> "flexible" }
            "cleaning" -> when (index) { 0 -> "obsessive"; 1 -> "average"; else -> "relaxed" }
            "food" -> when (index) { 0 -> "veg"; 1 -> "non_veg"; else -> "flexible" }
            "smoker" -> when (index) { 0 -> "yes"; 1 -> "no"; else -> "outside" }
            "social" -> when (index) { 0 -> "no_guests"; 1 -> "day_only"; else -> "anytime" }
            else -> ""
        }
    }
}
