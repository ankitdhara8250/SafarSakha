package com.safarsakha.presentation.screens.admin.tourpackage


import com.safarsakha.domain.model.TourPackage

sealed class AdminTourPackageListUiState {
    object Loading : AdminTourPackageListUiState()
    data class Success(val packages: List<TourPackage>) : AdminTourPackageListUiState()
    data class Error(val message: String) : AdminTourPackageListUiState()
    object Empty : AdminTourPackageListUiState()
}