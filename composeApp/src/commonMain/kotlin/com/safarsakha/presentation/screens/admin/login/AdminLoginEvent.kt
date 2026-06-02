package com.safarsakha.presentation.screens.admin.login

sealed interface AdminLoginEvent {

    data class EmailChanged(val email: String) : AdminLoginEvent

    data class PasswordChanged(val password: String) : AdminLoginEvent

    data object LoginClicked : AdminLoginEvent

    data object NavigationDone : AdminLoginEvent
}