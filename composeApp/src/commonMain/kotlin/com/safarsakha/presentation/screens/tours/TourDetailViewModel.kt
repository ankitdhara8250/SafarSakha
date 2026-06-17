package com.safarsakha.presentation.screens.tours

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.usecase.tourpackage.GetTourPackageByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TourDetailViewModel(
    private val getTourPackageByIdUseCase: GetTourPackageByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TourDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var lastLoadedId: String? = null

    fun handleEvent(event: TourDetailEvent) {
        when (event) {
            is TourDetailEvent.LoadPackage -> loadPackage(event.id)
            is TourDetailEvent.Retry -> lastLoadedId?.let { loadPackage(it) }
        }
    }

    private fun loadPackage(id: String) {
        lastLoadedId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getTourPackageByIdUseCase(id)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tourPackage = result.data,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Failed to load tour package"
                        )
                    }
                }
                else -> {}
            }
        }
    }
}