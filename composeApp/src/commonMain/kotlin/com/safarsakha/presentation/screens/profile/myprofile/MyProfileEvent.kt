package com.safarsakha.presentation.screens.profile.myprofile

sealed class MyProfileEvent {
    data object Retry : MyProfileEvent()
}