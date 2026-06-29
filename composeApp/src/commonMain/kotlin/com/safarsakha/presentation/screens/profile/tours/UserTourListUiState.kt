package com.safarsakha.presentation.screens.profile.tours

import com.safarsakha.domain.model.TourPackage

sealed class UserTourListUiState {
    object Loading : UserTourListUiState()
    /**
     * @param packages      The currently visible list (already filtered if [cityFilter] is set).
     * @param allPackages   The full unfiltered list; kept so filtering is purely local.
     * @param cityFilter    The active city filter text, or empty string when no filter is applied.
     */
    data class Success(
        val packages: List<TourPackage>,
        val allPackages: List<TourPackage> = packages,
        val cityFilter: String = ""
    ) : UserTourListUiState()
    data class Error(val message: String) : UserTourListUiState()
    object Empty : UserTourListUiState()
}