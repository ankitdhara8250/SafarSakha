package com.safarsakha.presentation.screens.profile.tours

import com.safarsakha.domain.model.TourPackage

sealed class UserTourListUiState {
    object Loading : UserTourListUiState()
    data class Success(val packages: List<TourPackage>) : UserTourListUiState()
    data class Error(val message: String) : UserTourListUiState()
    object Empty : UserTourListUiState()
}