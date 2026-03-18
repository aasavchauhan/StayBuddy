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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.staybuddy.data.model.RoommatePostType

data class RoommateListUiState(
    val posts: List<RoommatePost> = emptyList(),
    val filteredPosts: List<RoommatePost> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val maxBudget: Float = 50000f,
    val genderPreference: String = "Any",
    val currentUserId: String? = null
)

@HiltViewModel
class RoommateListViewModel @Inject constructor(
    private val roommateRepository: RoommateRepository,
    private val auth: FirebaseAuth
) : ViewModel() {



    private val _uiState = MutableStateFlow(RoommateListUiState())
    val uiState: StateFlow<RoommateListUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(currentUserId = auth.currentUser?.uid)
        loadPosts()
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
                    applyFilters()
                }
        }
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

        _uiState.value = state.copy(filteredPosts = filtered)
    }
}
