package com.safarsakha.presentation.screens.profile.userlogin

data class UserProfileUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false
)

