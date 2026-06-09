package com.safarsakha.presentation.screens.profile.userlogin

sealed class UserProfileEvent {
    data class OnEmailChanged(val email: String) : UserProfileEvent()
    data class OnPasswordChanged(val password: String) : UserProfileEvent()
    data object OnLoginClick : UserProfileEvent()
    data object OnRegisterClick : UserProfileEvent()
    data object OnAdminLoginClick : UserProfileEvent()
    data object OnErrorShown : UserProfileEvent()
    data object OnResetSuccess : UserProfileEvent()
}

