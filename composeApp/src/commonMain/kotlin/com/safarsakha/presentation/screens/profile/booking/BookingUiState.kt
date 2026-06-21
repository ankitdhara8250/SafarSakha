package com.safarsakha.presentation.screens.profile.booking

import com.safarsakha.domain.model.Booking

data class BookingUiState(
    val isSubmitting: Boolean = false,
    val createdBooking: Booking? = null,
    val errorMessage: String? = null
)