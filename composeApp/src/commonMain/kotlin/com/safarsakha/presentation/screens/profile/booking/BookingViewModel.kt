package com.safarsakha.presentation.screens.profile.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.usecase.booking.CreateBookingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backend-only ViewModel for creating a Booking.
 *
 * UI (travel-date selection, payment form) is intentionally NOT part of this
 * phase. A future BookingScreen will build a fully-formed [Booking] object
 * (after the user picks dates and completes payment) and dispatch
 * [BookingEvent.CreateBooking] to this ViewModel — no changes to this class
 * should be required when that screen is added.
 */
class BookingViewModel(
    private val createBookingUseCase: CreateBookingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState = _uiState.asStateFlow()

    fun handleEvent(event: BookingEvent) {
        when (event) {
            is BookingEvent.CreateBooking -> createBooking(event.booking)
            is BookingEvent.ResetState -> _uiState.update { BookingUiState() }
        }
    }

    private fun createBooking(booking: Booking) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = createBookingUseCase(booking)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isSubmitting = false, createdBooking = result.data, errorMessage = null)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.message ?: "Failed to create booking")
                }
                else -> {}
            }
        }
    }
}