package com.safarsakha.presentation.screens.profile.userlogin

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.usecase.auth.LoginUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val loginUserUseCase: LoginUserUseCase
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    fun onEvent(event: UserProfileEvent) {
        when (event) {
            is UserProfileEvent.OnEmailChanged -> {
                _uiState.update { it.copy(email = event.email, error = null) }
            }
            is UserProfileEvent.OnPasswordChanged -> {
                _uiState.update { it.copy(password = event.password, error = null) }
            }
            is UserProfileEvent.OnLoginClick -> {
                handleLogin()
            }
            is UserProfileEvent.OnRegisterClick -> {
                // Navigation handled in screen
            }
            is UserProfileEvent.OnAdminLoginClick -> {
                // Navigation handled in screen
            }
            is UserProfileEvent.OnErrorShown -> {
                _uiState.update { it.copy(error = null) }
            }
            is UserProfileEvent.OnResetSuccess -> {
                _uiState.update { it.copy(isLoginSuccess = false, isLoading = false) }
            }
        }
    }

    private fun handleLogin() {
        val currentState = _uiState.value

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(error = "Email cannot be empty") }
            return
        }

        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(error = "Password cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = loginUserUseCase.invoke(
                email = currentState.email.trim(),
                password = currentState.password
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, isLoginSuccess = true, error = null)
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }
}

