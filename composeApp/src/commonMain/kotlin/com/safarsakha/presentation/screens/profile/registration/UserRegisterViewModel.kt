package com.safarsakha.presentation.screens.profile.registration

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.usecase.auth.RegisterUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserRegisterViewModel(
    private val registerUserUseCase: RegisterUserUseCase
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(UserRegisterUiState())
    val uiState: StateFlow<UserRegisterUiState> = _uiState.asStateFlow()

    fun onEvent(event: UserRegisterEvent) {
        when (event) {
            is UserRegisterEvent.OnNameChanged -> {
                _uiState.update { it.copy(name = event.name, error = null) }
            }
            is UserRegisterEvent.OnEmailChanged -> {
                _uiState.update { it.copy(email = event.email, error = null) }
            }
            is UserRegisterEvent.OnPhoneNumberChanged -> {
                _uiState.update { it.copy(phoneNumber = event.phoneNumber, error = null) }
            }
            is UserRegisterEvent.OnPasswordChanged -> {
                _uiState.update { it.copy(password = event.password, error = null) }
            }
            is UserRegisterEvent.OnConfirmPasswordChanged -> {
                _uiState.update { it.copy(confirmPassword = event.confirmPassword, error = null) }
            }
            is UserRegisterEvent.OnRegisterClick -> {
                handleRegistration()
            }
            is UserRegisterEvent.OnBackToLogin -> {
                // Navigation handled in screen
            }
            is UserRegisterEvent.OnErrorShown -> {
                _uiState.update { it.copy(error = null) }
            }
            is UserRegisterEvent.OnResetSuccess -> {
                _uiState.update { it.copy(isRegistrationSuccess = false, isLoading = false) }
            }
        }
    }

    private fun handleRegistration() {
        val currentState = _uiState.value

        // Validation
        when {
            currentState.name.isBlank() -> {
                _uiState.update { it.copy(error = "Full name is required") }
                return
            }
            currentState.email.isBlank() -> {
                _uiState.update { it.copy(error = "Email is required") }
                return
            }
            currentState.phoneNumber.isBlank() -> {
                _uiState.update { it.copy(error = "Phone number is required") }
                return
            }
            currentState.password.isBlank() -> {
                _uiState.update { it.copy(error = "Password is required") }
                return
            }
            currentState.password.length < 6 -> {
                _uiState.update { it.copy(error = "Password must be at least 6 characters") }
                return
            }
            currentState.password != currentState.confirmPassword -> {
                _uiState.update { it.copy(error = "Passwords do not match") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = registerUserUseCase.invoke(
                name = currentState.name.trim(),
                email = currentState.email.trim(),
                phoneNumber = currentState.phoneNumber.trim(),
                password = currentState.password
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, isRegistrationSuccess = true, error = null)
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

