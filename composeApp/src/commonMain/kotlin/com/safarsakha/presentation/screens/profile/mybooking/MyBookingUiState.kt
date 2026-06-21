package com.safarsakha.presentation.screens.profile.mybooking

import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.model.BookingStatus

data class MyBookingUiState(
    val isLoading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val errorMessage: String? = null,

    // Cancellation
    val bookingPendingCancellation: Booking? = null,
    val isCancelling: Boolean = false,
    val cancelErrorMessage: String? = null
) {
    val upcomingBookings: List<Booking>
        get() = bookings.filter { it.bookingStatus == BookingStatus.UPCOMING }.sortedByDescending { it.bookingDate }

    val completedBookings: List<Booking>
        get() = bookings.filter { it.bookingStatus == BookingStatus.COMPLETED }.sortedByDescending { it.bookingDate }

    val cancelledBookings: List<Booking>
        get() = bookings.filter { it.bookingStatus == BookingStatus.CANCELLED }.sortedByDescending { it.bookingDate }
}