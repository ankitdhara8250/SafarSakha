package com.safarsakha.presentation.screens.admin.login

import com.safarsakha.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminLoginViewModel(
    private val authRepository: AuthRepository
) {


    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _uiState = MutableStateFlow(AdminLoginUiState())
    val uiState: StateFlow<AdminLoginUiState> = _uiState

    fun onEvent(event: AdminLoginEvent) {
        when (event) {
            is AdminLoginEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(
                    email = event.email,
                    errorMessage = null
                )
            }

            is AdminLoginEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(
                    password = event.password,
                    errorMessage = null
                )
            }

            AdminLoginEvent.LoginClicked -> {
                loginAdmin()
            }

            AdminLoginEvent.NavigationDone -> {
                _uiState.value = _uiState.value.copy(
                    isLoginSuccess = false
                )
            }
        }
    }

    private fun loginAdmin() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter email and password"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = authRepository.loginAdmin(email, password)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoginSuccess = true,
                    errorMessage = null
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Login failed"
                )
            }
        }
    }
}