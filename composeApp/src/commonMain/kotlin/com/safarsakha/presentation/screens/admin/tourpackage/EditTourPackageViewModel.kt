package com.safarsakha.presentation.screens.admin.tourpackage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.TourPackage
import com.safarsakha.domain.repository.TourPackageRepository
import com.safarsakha.domain.usecase.tourpackage.GetTourPackageByIdUseCase
import com.safarsakha.domain.usecase.tourpackage.UpdateTourPackageUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class EditTourPackageViewModel(
    private val getTourPackageByIdUseCase: GetTourPackageByIdUseCase,
    private val updateTourPackageUseCase: UpdateTourPackageUseCase,
    private val tourPackageRepository: TourPackageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditTourPackageUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun handleEvent(event: EditTourPackageEvent) {
        when (event) {
            is EditTourPackageEvent.LoadPackage -> loadPackage(event.id)
            is EditTourPackageEvent.TitleChanged -> _uiState.update { it.copy(title = event.title) }
            is EditTourPackageEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.description) }
            is EditTourPackageEvent.LocationChanged -> _uiState.update { it.copy(location = event.location) }
            is EditTourPackageEvent.DurationChanged -> _uiState.update { it.copy(duration = event.duration) }
            is EditTourPackageEvent.PriceChanged -> _uiState.update { it.copy(price = event.price) }
            is EditTourPackageEvent.IncludedServicesChanged -> _uiState.update { it.copy(includedServices = event.services) }
            is EditTourPackageEvent.ImageSelected -> _uiState.update { it.copy(selectedImageBytes = event.imageBytes) }
            is EditTourPackageEvent.UpdatePackage -> updatePackage()
            is EditTourPackageEvent.ResetSuccess -> _uiState.update { it.copy(success = false) }
        }
    }

    private fun loadPackage(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getTourPackageByIdUseCase(id)) {
                is Resource.Success -> {
                    val pkg = result.data!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tourPackage = pkg,
                            title = pkg.title,
                            description = pkg.description,
                            location = pkg.location,
                            duration = pkg.duration,
                            price = pkg.price.toString(),
                            imageUrl = pkg.imageUrl,
                            includedServices = pkg.includedServices.joinToString(", ")
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    private fun updatePackage() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentPkg = currentState.tourPackage ?: return@launch

            _uiState.update { it.copy(isUpdating = true, errors = emptyMap()) }

            var finalImageUrl = currentState.imageUrl

            // 1. Upload new image if selected
            currentState.selectedImageBytes?.let { bytes ->
                val fileName = "tour_${Clock.System.now().toEpochMilliseconds()}.jpg"
                val uploadResult = tourPackageRepository.uploadPackageImage(bytes, fileName)
                if (uploadResult is Resource.Success) {
                    finalImageUrl = uploadResult.data
                } else if (uploadResult is Resource.Error) {
                    _uiState.update { it.copy(isUpdating = false, errorMessage = "Image upload failed") }
                    return@launch
                }
            }

            // 2. Prepare updated package
            val updatedPkg = currentPkg.copy(
                title = currentState.title,
                description = currentState.description,
                location = currentState.location,
                duration = currentState.duration,
                price = currentState.price.toDoubleOrNull() ?: 0.0,
                imageUrl = finalImageUrl,
                includedServices = currentState.includedServices.split(",").map { it.trim() }.filter { it.isNotBlank() },
                updatedAt = Clock.System.now()
            )

            // 3. Save to Firestore
            when (val result = updateTourPackageUseCase(updatedPkg)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isUpdating = false, success = true) }
                    _navigationEvent.emit(Unit)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false, errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }
}
