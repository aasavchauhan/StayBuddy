package com.example.staybuddy.ui.screens.roommate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.RoommatePost
import com.example.staybuddy.data.repository.RoommateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoommateListUiState(
    val posts: List<RoommatePost> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class RoommateListViewModel @Inject constructor(
    private val roommateRepository: RoommateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoommateListUiState())
    val uiState: StateFlow<RoommateListUiState> = _uiState.asStateFlow()

    init {
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
                }
        }
    }
}
