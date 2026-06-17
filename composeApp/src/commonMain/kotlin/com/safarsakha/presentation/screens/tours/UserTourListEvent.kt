package com.safarsakha.presentation.screens.tours

sealed class UserTourListEvent {
    object LoadPackages : UserTourListEvent()
    object RefreshPackages : UserTourListEvent()
}