package com.safarsakha.presentation.screens.profile.booking

import com.safarsakha.domain.model.Booking

sealed class BookingEvent {
    data class CreateBooking(val booking: Booking) : BookingEvent()
    object ResetState : BookingEvent()
}