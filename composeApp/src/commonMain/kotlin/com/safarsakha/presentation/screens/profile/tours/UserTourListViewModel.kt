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

    /**
     * Pure function — reused for both initial filter and refresh re-application.
     *
     * Matching strategy (all comparisons are case-insensitive):
     *  1. Normalise both sides: trim outer whitespace, collapse inner runs of
     *     whitespace/commas to a single space.
     *  2. Check if the normalised location *contains* the normalised query.
     *     → "Darjeeling, West Bengal" contains "darj"       ✅
     *     → "Darjeeling, West Bengal" contains "west bengal" ✅
     *     → "Bengaluru, Karnataka"    contains "karnataka"   ✅
     *  3. Null or blank location values never match a non-empty query.
     *  4. Empty / blank query is never passed here (filterByCity guards that).
     */
    private fun applyFilter(
        allPackages: List<com.safarsakha.domain.model.TourPackage>,
        city: String
    ): List<com.safarsakha.domain.model.TourPackage> {
        val normalisedQuery = city.normaliseLocationString()
        return allPackages.filter { tour ->
            val normalisedLocation = tour.location.normaliseLocationString()
            normalisedLocation.isNotEmpty() &&
                    normalisedLocation.contains(normalisedQuery, ignoreCase = true)
        }
    }

    /**
     * Normalises a location string for comparison:
     * - Trim outer whitespace.
     * - Replace commas (with optional surrounding spaces) with a single space.
     * - Collapse any run of whitespace into a single space.
     * - Lower-case (done at call-site via ignoreCase = true, but kept consistent).
     *
     * Examples:
     *   "Darjeeling,  West Bengal" → "darjeeling west bengal"
     *   "  Bengaluru , Karnataka " → "bengaluru karnataka"
     */
    private fun String?.normaliseLocationString(): String =
        this
            ?.trim()
            ?.replace(Regex("""\s*,\s*"""), " ")   // commas → space
            ?.replace(Regex("""\s+"""), " ")         // collapse whitespace
            ?.trim()
            ?.lowercase()
            ?: ""
}