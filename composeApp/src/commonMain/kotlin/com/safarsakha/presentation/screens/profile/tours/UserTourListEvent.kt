package com.safarsakha.presentation.screens.profile.tours

sealed class UserTourListEvent {
    object LoadPackages : UserTourListEvent()
    object RefreshPackages : UserTourListEvent()
    data class FilterByCity(val city: String) : UserTourListEvent()
    object ClearCityFilter : UserTourListEvent()
}