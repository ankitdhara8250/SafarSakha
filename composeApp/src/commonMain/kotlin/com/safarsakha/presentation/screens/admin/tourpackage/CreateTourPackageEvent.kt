package com.safarsakha.presentation.screens.admin.tourpackage

sealed class CreateTourPackageEvent {
    data class TitleChanged(val title: String) : CreateTourPackageEvent()
    data class DescriptionChanged(val description: String) : CreateTourPackageEvent()
    data class LocationChanged(val location: String) : CreateTourPackageEvent()
    data class DurationChanged(val duration: String) : CreateTourPackageEvent()
    data class PriceChanged(val price: String) : CreateTourPackageEvent()
    data class IncludedServicesChanged(val services: String) : CreateTourPackageEvent()
    data class ImageSelected(val imageBytes: ByteArray, val fileName: String) : CreateTourPackageEvent()
    object SavePackage : CreateTourPackageEvent()
    object ResetSuccess : CreateTourPackageEvent()
}