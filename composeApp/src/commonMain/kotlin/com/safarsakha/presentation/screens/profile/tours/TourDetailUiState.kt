package com.safarsakha.presentation.screens.profile.tours

import com.safarsakha.domain.model.TourPackage

data class TourDetailUiState(
    val isLoading: Boolean = true,
    val tourPackage: TourPackage? = null,
    val errorMessage: String? = null,
    val showEnquiryDialog: Boolean = false,
    val isSubmittingEnquiry: Boolean = false
)