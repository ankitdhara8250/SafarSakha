package com.safarsakha.presentation.screens.admin.tourpackage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.domain.model.TourPackage
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.repository.TourPackageRepository
import com.safarsakha.domain.usecase.tourpackage.CreateTourPackageUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class CreateTourPackageViewModel(
    private val createTourPackageUseCase: CreateTourPackageUseCase,
    private val tourPackageRepository: TourPackageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTourPackageUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun handleEvent(event: CreateTourPackageEvent) {
        when (event) {
            is CreateTourPackageEvent.TitleChanged -> updateTitle(event.title)
            is CreateTourPackageEvent.DescriptionChanged -> updateDescription(event.description)
            is CreateTourPackageEvent.LocationChanged -> updateLocation(event.location)
            is CreateTourPackageEvent.DurationChanged -> updateDuration(event.duration)
            is CreateTourPackageEvent.PriceChanged -> updatePrice(event.price)
            is CreateTourPackageEvent.IncludedServicesChanged -> updateIncludedServices(event.services)
            is CreateTourPackageEvent.ImageSelected -> updateSelectedImage(event.imageBytes, event.fileName)
            is CreateTourPackageEvent.SavePackage -> savePackage()
            is CreateTourPackageEvent.ResetSuccess -> resetSuccess()
        }
    }

    private fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title, errors = _uiState.value.errors - "title")
    }

    private fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description, errors = _uiState.value.errors - "description")
    }

    private fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location, errors = _uiState.value.errors - "location")
    }

    private fun updateDuration(duration: String) {
        _uiState.value = _uiState.value.copy(duration = duration, errors = _uiState.value.errors - "duration")
    }

    private fun updatePrice(price: String) {
        _uiState.value = _uiState.value.copy(price = price, errors = _uiState.value.errors - "price")
    }

    private fun updateIncludedServices(services: String) {
        _uiState.value = _uiState.value.copy(includedServices = services, errors = _uiState.value.errors - "includedServices")
    }

    private fun updateSelectedImage(imageBytes: ByteArray, fileName: String) {
        _uiState.value = _uiState.value.copy(
            selectedImageBytes = imageBytes,
            imageUrl = fileName, // We use this as a temporary holder for the filename or local path
            errors = _uiState.value.errors - "image"
        )
    }

    private fun resetSuccess() {
        _uiState.value = CreateTourPackageUiState()
    }

    private fun savePackage() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            var uploadedImageUrl: String? = null

            // 1. Upload image if selected
            _uiState.value.selectedImageBytes?.let { bytes ->
                val fileName = "tour_${Clock.System.now().toEpochMilliseconds()}.jpg"
                val uploadResult = tourPackageRepository.uploadPackageImage(bytes, fileName)
                when (uploadResult) {
                    is Resource.Success -> {
                        uploadedImageUrl = uploadResult.data
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errors = mapOf("form" to (uploadResult.message ?: "Failed to upload image"))
                        )
                        return@launch
                    }
                    else -> {}
                }
            }

            // 2. Prepare package data
            val servicesList = _uiState.value.includedServices
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val priceValue = _uiState.value.price.toDoubleOrNull() ?: 0.0

            val tourPackage = TourPackage(
                id = "",
                title = _uiState.value.title,
                description = _uiState.value.description,
                location = _uiState.value.location,
                duration = _uiState.value.duration,
                price = priceValue,
                imageUrl = uploadedImageUrl,
                includedServices = servicesList,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
                isActive = true
            )

            // 3. Save package
            val result = createTourPackageUseCase.invoke(tourPackage)
            when (result) {
                is Resource.Success<*> -> {
                    _uiState.value = CreateTourPackageUiState()
                    _navigationEvent.emit(Unit)
                }
                is Resource.Error -> {
                    val errorMessage = result.message
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errors = mapOf("form" to (errorMessage ?: "Failed to save package"))
                    )
                }
                else -> {}
            }
        }
    }
}