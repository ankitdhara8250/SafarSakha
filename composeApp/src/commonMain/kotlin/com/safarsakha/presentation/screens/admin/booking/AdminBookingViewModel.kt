package com.safarsakha.presentation.screens.admin.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.usecase.booking.GetAllBookingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backend ViewModel for Admin Booking Management. Handles:
 *  - Get All Bookings (live observe, across every user)
 */
class AdminBookingViewModel(
    private val getAllBookingsUseCase: GetAllBookingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminBookingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeAllBookings()
    }

    fun handleEvent(event: AdminBookingEvent) {
        when (event) {
            is AdminBookingEvent.LoadAllBookings -> observeAllBookings()
        }
    }

    private fun observeAllBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getAllBookingsUseCase.observe().collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, bookings = result.data ?: emptyList(), errorMessage = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message ?: "Failed to load bookings")
                    }
                    else -> {}
                }
            }
        }
    }
}