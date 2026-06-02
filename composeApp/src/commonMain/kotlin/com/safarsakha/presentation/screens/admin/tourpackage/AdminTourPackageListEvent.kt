package com.safarsakha.presentation.screens.admin.tourpackage


sealed class AdminTourPackageListEvent {
    object LoadPackages : AdminTourPackageListEvent()
    data class DeletePackage(val id: String) : AdminTourPackageListEvent()
    object RefreshPackages : AdminTourPackageListEvent()
}