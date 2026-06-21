package com.safarsakha.presentation.screens.admin.booking

sealed class AdminBookingEvent {
    object LoadAllBookings : AdminBookingEvent()
}