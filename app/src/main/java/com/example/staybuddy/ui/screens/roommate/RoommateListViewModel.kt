package com.example.staybuddy.ui.screens.roommate

import com.example.staybuddy.data.model.User
import com.example.staybuddy.data.repository.AuthRepository
import com.example.staybuddy.data.repository.RoommateRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.staybuddy.data.model.RoommatePostType
import com.example.staybuddy.data.model.RoommatePost

data class RoommateListUiState(
    val posts: List<RoommatePost> = emptyList(),
    val filteredPosts: List<RoommatePost> = emptyList(),
    val matchScores: Map<String, Int> = emptyMap(), // Maps userId to match percentage
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val maxBudget: Float = 50000f,
    val genderPreference: String = "Any",
    val sortByMatch: Boolean = true,
    val currentUserId: String? = null,
    val currentUserProfile: User? = null
)

@HiltViewModel
class RoommateListViewModel @Inject constructor(
    private val roommateRepository: RoommateRepository,
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoommateListUiState())
    val uiState: StateFlow<RoommateListUiState> = _uiState.asStateFlow()

    init {
        val uid = auth.currentUser?.uid
        _uiState.value = _uiState.value.copy(currentUserId = uid)
        fetchCurrentUserProfile(uid)
        loadPosts()
    }

    private fun fetchCurrentUserProfile(uid: String?) {
        if (uid == null) return
        viewModelScope.launch {
            authRepository.getUserFromFirestore(uid).onSuccess { user ->
                _uiState.value = _uiState.value.copy(currentUserProfile = user)
                calculateMatchScores()
            }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch {
            roommateRepository.getRoommatePosts()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load posts"
                    )
                }
                .collect { posts ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        posts = posts,
                        error = null
                    )
                    calculateMatchScores()
                    applyFilters()
                }
        }
    }

    private fun calculateMatchScores() {
        val currentUser = _uiState.value.currentUserProfile ?: return
        val currentUserAnswers = currentUser.quizResults
        if (currentUserAnswers.isEmpty()) return

        val posts = _uiState.value.posts
        val scores = mutableMapOf<String, Int>()

        viewModelScope.launch {
            posts.forEach { post ->
                if (post.userId != currentUser.userId && !scores.containsKey(post.userId)) {
                    authRepository.getUserFromFirestore(post.userId).onSuccess { creator ->
                        creator?.let {
                            val score = calculateCompatibility(currentUserAnswers, it.quizResults)
                            scores[post.userId] = score
                            _uiState.value = _uiState.value.copy(matchScores = scores.toMap())
                        }
                    }
                }
            }
        }
    }

    private fun calculateCompatibility(user1Quiz: Map<String, Int>, user2Quiz: Map<String, Int>): Int {
        if (user1Quiz.isEmpty() || user2Quiz.isEmpty()) return 0
        
        var matchCount = 0
        var totalCount = 0
        
        user1Quiz.forEach { (questionId, answerIndex) ->
            user2Quiz[questionId]?.let { otherAnswerIndex ->
                totalCount++
                if (answerIndex == otherAnswerIndex) {
                    matchCount++
                }
            }
        }
        
        return if (totalCount > 0) (matchCount * 100) / totalCount else 0
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onMaxBudgetChanged(maxBudget: Float) {
        _uiState.value = _uiState.value.copy(maxBudget = maxBudget)
        applyFilters()
    }

    fun onGenderPreferenceChanged(gender: String) {
        _uiState.value = _uiState.value.copy(genderPreference = gender)
        applyFilters()
    }

    fun onSortByMatchChanged(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(sortByMatch = enabled)
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = state.posts.filter { post ->
            val isOffer = post.postType == RoommatePostType.OFFER
            
            val matchesSearch = if (state.searchQuery.isNotBlank()) {
                post.location.contains(state.searchQuery, ignoreCase = true) ||
                post.city.contains(state.searchQuery, ignoreCase = true)
            } else true

            val matchesBudget = post.priceShare <= state.maxBudget

            val matchesGender = if (state.genderPreference != "Any") {
                val postGender = post.preferences["Gender"] ?: "Any"
                postGender == state.genderPreference || postGender == "Any"
            } else true

            isOffer && matchesSearch && matchesBudget && matchesGender
        }

        val sorted = if (state.sortByMatch) {
            filtered.sortedByDescending { state.matchScores[it.userId] ?: 0 }
        } else filtered

        _uiState.value = state.copy(filteredPosts = sorted)
    }
}
