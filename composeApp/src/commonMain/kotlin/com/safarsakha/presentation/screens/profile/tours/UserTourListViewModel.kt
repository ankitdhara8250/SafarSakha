package com.safarsakha.presentation.screens.profile.tours

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.usecase.tourpackage.GetActiveTourPackagesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserTourListViewModel(
    private val getActiveTourPackagesUseCase: GetActiveTourPackagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserTourListUiState>(UserTourListUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadPackages()
    }

    fun handleEvent(event: UserTourListEvent) {
        when (event) {
            is UserTourListEvent.LoadPackages -> loadPackages()
            is UserTourListEvent.RefreshPackages -> loadPackages()
            is UserTourListEvent.FilterByCity -> filterByCity(event.city)
            is UserTourListEvent.ClearCityFilter -> clearCityFilter()
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun loadPackages() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (_uiState.value !is UserTourListUiState.Success) {
                _uiState.value = UserTourListUiState.Loading
            }

            when (val result = getActiveTourPackagesUseCase()) {
                is Resource.Success -> {
                    val packages = result.data ?: emptyList()
                    _uiState.value = if (packages.isEmpty()) {
                        UserTourListUiState.Empty
                    } else {
                        // Preserve any city filter that was already active before a refresh
                        val existingFilter = (_uiState.value as? UserTourListUiState.Success)
                            ?.cityFilter.orEmpty()
                        if (existingFilter.isBlank()) {
                            UserTourListUiState.Success(
                                packages = packages,
                                allPackages = packages,
                                cityFilter = ""
                            )
                        } else {
                            // Re-apply the filter against the freshly loaded data
                            val filtered = applyFilter(packages, existingFilter)
                            UserTourListUiState.Success(
                                packages = filtered,
                                allPackages = packages,
                                cityFilter = existingFilter
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.value = UserTourListUiState.Error(result.message ?: "Unknown error")
                }
                else -> {}
            }
        }
    }

    /**
     * Filters the already-loaded list by [city].
     * - Case-insensitive comparison.
     * - Trims leading/trailing spaces on both sides.
     * - Handles null/empty location gracefully.
     * - An empty or blank [city] clears the filter.
     * Does NOT make a new network call.
     */
    private fun filterByCity(city: String) {
        val currentSuccess = _uiState.value as? UserTourListUiState.Success ?: return
        val trimmed = city.trim()

        if (trimmed.isBlank()) {
            // Empty input → restore the full list
            _uiState.value = currentSuccess.copy(
                packages = currentSuccess.allPackages,
                cityFilter = ""
            )
            return
        }

        val filtered = applyFilter(currentSuccess.allPackages, trimmed)
        _uiState.value = currentSuccess.copy(
            packages = filtered,
            cityFilter = trimmed
        )
    }

    private fun clearCityFilter() {
        val currentSuccess = _uiState.value as? UserTourListUiState.Success ?: return
        _uiState.value = currentSuccess.copy(
            packages = currentSuccess.allPackages,
            cityFilter = ""
        )
    }

    /** Pure function so it can be reused for both initial filter and refresh re-application. */
    private fun applyFilter(
        allPackages: List<com.safarsakha.domain.model.TourPackage>,
        city: String
    ): List<com.safarsakha.domain.model.TourPackage> =
        allPackages.filter { tour ->
            tour.location.trim().equals(city.trim(), ignoreCase = true)
        }
}