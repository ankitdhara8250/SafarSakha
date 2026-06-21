package com.safarsakha.presentation.screens.profile.mybooking

import com.safarsakha.domain.model.Booking

sealed class MyBookingEvent {
    object LoadBookings : MyBookingEvent()
    data class RequestCancelBooking(val booking: Booking) : MyBookingEvent()
    object DismissCancelDialog : MyBookingEvent()
    object ConfirmCancelBooking : MyBookingEvent()
}