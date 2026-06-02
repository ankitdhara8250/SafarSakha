package com.safarsakha.presentation.screens.admin.tourpackage


data class CreateTourPackageUiState(
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val duration: String = "",
    val price: String = "",
    val includedServices: String = "",
    val imageUrl: String? = null,
    val selectedImageBytes: ByteArray? = null,
    val isLoading: Boolean = false,
    val isUploadingImage: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val success: Boolean = false
)