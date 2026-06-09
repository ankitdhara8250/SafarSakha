package com.safarsakha.presentation.screens.profile.registration

sealed class UserRegisterEvent {
    data class OnNameChanged(val name: String) : UserRegisterEvent()
    data class OnEmailChanged(val email: String) : UserRegisterEvent()
    data class OnPhoneNumberChanged(val phoneNumber: String) : UserRegisterEvent()
    data class OnPasswordChanged(val password: String) : UserRegisterEvent()
    data class OnConfirmPasswordChanged(val confirmPassword: String) : UserRegisterEvent()
    data object OnRegisterClick : UserRegisterEvent()
    data object OnBackToLogin : UserRegisterEvent()
    data object OnErrorShown : UserRegisterEvent()
    data object OnResetSuccess : UserRegisterEvent()
}

