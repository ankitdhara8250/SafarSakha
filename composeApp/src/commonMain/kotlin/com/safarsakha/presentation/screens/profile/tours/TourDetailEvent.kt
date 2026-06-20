package com.safarsakha.presentation.screens.profile.tours

sealed class TourDetailEvent {
    data class LoadPackage(val id: String) : TourDetailEvent()
    object Retry : TourDetailEvent()
}