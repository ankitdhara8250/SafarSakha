package com.safarsakha.presentation.screens.admin.booking

import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.model.BookingStatus

data class AdminBookingUiState(
    val isLoading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val errorMessage: String? = null
) {
    val upcomingBookings: List<Booking>
        get() = bookings.filter { it.bookingStatus == BookingStatus.UPCOMING }.sortedByDescending { it.bookingDate }

    val previousBookings: List<Booking>
        get() = bookings.filter { it.bookingStatus == BookingStatus.COMPLETED }.sortedByDescending { it.bookingDate }

    val cancelledBookings: List<Booking>
        get() = bookings.filter { it.bookingStatus == BookingStatus.CANCELLED }.sortedByDescending { it.bookingDate }
}