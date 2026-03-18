package com.example.staybuddy.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.repository.AuthRepository
import com.example.staybuddy.data.manager.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    data object Onboarding : SplashDestination()
    data object Login : SplashDestination()
    data object Home : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination = _destination.asStateFlow()

    fun checkAuthState() {
        viewModelScope.launch {
            val isOnboardingCompleted = preferenceManager.isOnboardingCompleted.first()
            if (!isOnboardingCompleted) {
                _destination.value = SplashDestination.Onboarding
                return@launch
            }
            
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                android.util.Log.d("SplashViewModel", "checkAuthState: User logged in, fetching profile")
                // Ensure the user has a Firestore profile even if they logged in previously before the fix
                val profileResult = authRepository.getUserFromFirestore(currentUser.uid)
                if (profileResult.isSuccess && profileResult.getOrNull() == null) {
                    android.util.Log.d("SplashViewModel", "checkAuthState: Profile missing, creating default")
                    val newUser = com.example.staybuddy.data.model.User(
                        userId = currentUser.uid,
                        name = currentUser.displayName ?: "New User",
                        email = currentUser.email ?: "",
                        role = "student"
                    )
                    authRepository.saveUserToFirestore(newUser)
                } else if (profileResult.isFailure) {
                    android.util.Log.e("SplashViewModel", "checkAuthState: Profile fetch failed, proceeding to Home anyway")
                }
                android.util.Log.d("SplashViewModel", "checkAuthState: Navigating to Home")
                _destination.value = SplashDestination.Home
            } else {
                android.util.Log.d("SplashViewModel", "checkAuthState: No user, navigating to Login")
                _destination.value = SplashDestination.Login
            }
        }
    }
}
