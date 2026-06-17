package com.safarsakha.presentation.screens.tours

import com.safarsakha.domain.model.TourPackage

data class TourDetailUiState(
    val isLoading: Boolean = true,
    val tourPackage: TourPackage? = null,
    val errorMessage: String? = null
)