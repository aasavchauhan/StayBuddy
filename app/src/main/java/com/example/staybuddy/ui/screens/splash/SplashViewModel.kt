package com.example.staybuddy.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staybuddy.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    data object Onboarding : SplashDestination()
    data object Login : SplashDestination()
    data object Home : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination = _destination.asStateFlow()

    fun checkAuthState(isFirstLaunch: Boolean) {
        viewModelScope.launch {
            _destination.value = when {
                isFirstLaunch -> SplashDestination.Onboarding
                authRepository.currentUser != null -> SplashDestination.Home
                else -> SplashDestination.Login
            }
        }
    }
}
