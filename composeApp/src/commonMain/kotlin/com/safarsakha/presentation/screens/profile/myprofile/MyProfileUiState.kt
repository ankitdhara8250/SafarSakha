package com.safarsakha.presentation.screens.profile.myprofile

import com.safarsakha.domain.model.User


sealed class MyProfileUiState {
    data object Loading : MyProfileUiState()
    data class Success(val user: User) : MyProfileUiState()
    data class Error(val message: String) : MyProfileUiState()

    /** Terminal state — logout completed; navigate to login and clear back stack. */
    data object LoggedOut : MyProfileUiState()
}