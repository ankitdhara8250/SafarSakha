package com.safarsakha.presentation.screens.admin.tourpackage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.domain.model.TourPackage
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.usecase.tourpackage.CreateTourPackageUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class CreateTourPackageViewModel(
    private val createTourPackageUseCase: CreateTourPackageUseCase
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
            is CreateTourPackageEvent.SavePackage -> savePackage()
            is CreateTourPackageEvent.ResetSuccess -> resetSuccess()
            else -> {}
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

    private fun resetSuccess() {
        _uiState.value = CreateTourPackageUiState()
    }

    private fun savePackage() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

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
                imageUrl = null,
                includedServices = servicesList,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
                isActive = true
            )

            val result = createTourPackageUseCase.invoke(tourPackage)
            when (result) {
                is Resource.Success<*> -> {
                    // Reset the form data completely on success
                    _uiState.value = CreateTourPackageUiState()
                    _navigationEvent.emit(Unit)
                }
                is Resource.Error -> {
                    val errorMessage = (result as Resource.Error).message
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
