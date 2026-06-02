package com.safarsakha.presentation.screens.admin.tourpackage

import com.safarsakha.domain.model.TourPackage

data class EditTourPackageUiState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val success: Boolean = false,
    val tourPackage: TourPackage? = null,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val duration: String = "",
    val price: String = "",
    val includedServices: String = "",
    val imageUrl: String? = null,
    val selectedImageBytes: ByteArray? = null,
    val errors: Map<String, String> = emptyMap(),
    val errorMessage: String? = null
)
