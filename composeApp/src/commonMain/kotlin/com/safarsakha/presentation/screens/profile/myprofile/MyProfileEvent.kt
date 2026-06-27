package com.safarsakha.presentation.screens.profile.myprofile

sealed class MyProfileEvent {
    data object Retry : MyProfileEvent()
    data object Logout : MyProfileEvent()
    data object ResetAfterLogout : MyProfileEvent()
}