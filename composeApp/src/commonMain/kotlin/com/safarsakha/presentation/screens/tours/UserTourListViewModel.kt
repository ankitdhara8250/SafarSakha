package com.safarsakha.presentation.screens.tours

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
        }
    }

    private fun loadPackages() {
        // Cancel any in-flight load before starting a new one
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            // Only show the loading state if we don't already have data on screen
            if (_uiState.value !is UserTourListUiState.Success) {
                _uiState.value = UserTourListUiState.Loading
            }

            when (val result = getActiveTourPackagesUseCase()) {
                is Resource.Success -> {
                    val packages = result.data ?: emptyList()
                    _uiState.value = if (packages.isEmpty()) {
                        UserTourListUiState.Empty
                    } else {
                        UserTourListUiState.Success(packages)
                    }
                }
                is Resource.Error -> {
                    _uiState.value = UserTourListUiState.Error(result.message ?: "Unknown error")
                }
                else -> {}
            }
        }
    }
}