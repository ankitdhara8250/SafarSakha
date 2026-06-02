package com.safarsakha.presentation.screens.admin.tourpackage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.usecase.tourpackage.DeleteTourPackageUseCase
import com.safarsakha.domain.usecase.tourpackage.GetTourPackagesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminTourPackageListViewModel(
    private val getTourPackagesUseCase: GetTourPackagesUseCase,
    private val deleteTourPackageUseCase: DeleteTourPackageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminTourPackageListUiState>(AdminTourPackageListUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _showSnackbar = MutableSharedFlow<String>()
    val showSnackbar = _showSnackbar.asSharedFlow()

    private var observeJob: Job? = null

    init {
        observePackages()
    }

    fun handleEvent(event: AdminTourPackageListEvent) {
        when (event) {
            is AdminTourPackageListEvent.LoadPackages -> observePackages()
            is AdminTourPackageListEvent.DeletePackage -> deletePackage(event.id)
            is AdminTourPackageListEvent.RefreshPackages -> observePackages()
        }
    }

    private fun observePackages() {
        // Cancel existing listener if any before starting a new one
        observeJob?.cancel()
        
        observeJob = viewModelScope.launch {
            // Only show loading if we don't have data yet
            if (_uiState.value !is AdminTourPackageListUiState.Success) {
                _uiState.value = AdminTourPackageListUiState.Loading
            }
            
            getTourPackagesUseCase.observe().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val packages = result.data ?: emptyList()
                        if (packages.isEmpty()) {
                            _uiState.value = AdminTourPackageListUiState.Empty
                        } else {
                            _uiState.value = AdminTourPackageListUiState.Success(packages)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = AdminTourPackageListUiState.Error(result.message ?: "Unknown error")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun deletePackage(id: String) {
        viewModelScope.launch {
            when (val result = deleteTourPackageUseCase(id)) {
                is Resource.Success -> {
                    _showSnackbar.emit("Package deleted successfully")
                    // Note: No need to reload, the observer handles the list update
                }
                is Resource.Error -> {
                    _showSnackbar.emit(result.message ?: "Failed to delete package")
                }
                else -> {}
            }
        }
    }
}