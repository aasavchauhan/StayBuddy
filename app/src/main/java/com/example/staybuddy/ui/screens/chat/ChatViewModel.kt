package com.example.staybuddy.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.model.Message
import com.example.staybuddy.data.model.RoommatePost
import com.example.staybuddy.data.repository.ChatRepository
import com.example.staybuddy.data.repository.RoommateRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val currentUserId: String = "",
    val isMatchConfirmed: Boolean = false,
    val confirmedBy: List<String> = emptyList(),
    val roommatePostId: String = "",
    val roommatePost: RoommatePost? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSending: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val roommateRepository: RoommateRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadRoomDetails()
        loadMessages()
    }

    private fun loadRoomDetails() {
        viewModelScope.launch {
            chatRepository.getChatRoomFlow(chatId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                .collect { room ->
                    if (room != null) {
                        _uiState.value = _uiState.value.copy(
                            isMatchConfirmed = room.isMatchConfirmed,
                            confirmedBy = room.confirmedBy,
                            roommatePostId = room.roommatePostId
                        )
                        fetchPostDetails(room.roommatePostId)
                    }
                }
        }
    }

    private fun loadMessages() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "User not authenticated"
            )
            return
        }

        _uiState.value = _uiState.value.copy(currentUserId = userId)

        viewModelScope.launch {
            chatRepository.getMessagesForRoom(chatId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load messages"
                    )
                }
                .collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        messages = messages,
                        error = null
                    )
                }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val userId = auth.currentUser?.uid ?: return
        
        _uiState.value = _uiState.value.copy(isSending = true)
        
        viewModelScope.launch {
            val result = chatRepository.sendMessage(chatId, userId, text)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isSending = false, error = null)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = e.message ?: "Failed to send message"
                )
            }
        }
    }

    fun confirmMatch() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            val result = chatRepository.confirmMatch(chatId, userId)
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to confirm match")
            }
        }
    }

    private fun fetchPostDetails(postId: String) {
        if (postId.isBlank()) return
        viewModelScope.launch {
            roommateRepository.getRoommatePostById(postId).onSuccess { post ->
                _uiState.value = _uiState.value.copy(roommatePost = post)
            }
        }
    }
}
