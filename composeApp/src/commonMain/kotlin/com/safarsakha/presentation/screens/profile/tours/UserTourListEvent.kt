package com.safarsakha.presentation.screens.profile.tours

sealed class UserTourListEvent {
    object LoadPackages : UserTourListEvent()
    object RefreshPackages : UserTourListEvent()
}