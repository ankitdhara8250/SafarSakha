package com.safarsakha.presentation.screens.admin.tourpackage

sealed class EditTourPackageEvent {
    data class LoadPackage(val id: String) : EditTourPackageEvent()
    data class TitleChanged(val title: String) : EditTourPackageEvent()
    data class DescriptionChanged(val description: String) : EditTourPackageEvent()
    data class LocationChanged(val location: String) : EditTourPackageEvent()
    data class DurationChanged(val duration: String) : EditTourPackageEvent()
    data class PriceChanged(val price: String) : EditTourPackageEvent()
    data class IncludedServicesChanged(val services: String) : EditTourPackageEvent()
    object UpdatePackage : EditTourPackageEvent()
    object ResetSuccess : EditTourPackageEvent()
}
