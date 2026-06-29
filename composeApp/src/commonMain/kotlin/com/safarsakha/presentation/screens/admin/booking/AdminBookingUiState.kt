package com.safarsakha.presentation.screens.admin.booking

import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.model.BookingStatus

data class AdminBookingUiState(
    val isLoading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val errorMessage: String? = null
) {
    // FIX: Filter out documents whose bookingId is blank.
    //
    // Root cause of "Key "" was already used" crash (LazyColumn):
    // The old two-step Firestore write (add() → then set() with the id) created a race
    // condition. The live snapshot listener could fire BETWEEN the two writes and emit a
    // document with bookingId = "". If this happened for two or more bookings at the same
    // time, the LazyColumn received multiple items whose key was the empty string, which
    // Compose disallows and throws:
    //   IllegalArgumentException: Key "" was already used.
    //
    // The atomic write in FirebaseBookingDataSource.createBooking() prevents this going
    // forward, but existing Firestore documents that already have bookingId = "" (written
    // before the fix was deployed) are still returned by the live observer.
    //
    // Filtering them out here is the correct second line of defence: a booking with no
    // id is semantically invalid and should never be shown or navigated into.
    private val validBookings: List<Booking>
        get() = bookings.filter { it.bookingId.isNotBlank() }

    val upcomingBookings: List<Booking>
        get() = validBookings.filter { it.bookingStatus == BookingStatus.UPCOMING }
            .sortedByDescending { it.bookingDate }

    val previousBookings: List<Booking>
        get() = validBookings.filter { it.bookingStatus == BookingStatus.COMPLETED }
            .sortedByDescending { it.bookingDate }

    val cancelledBookings: List<Booking>
        get() = validBookings.filter { it.bookingStatus == BookingStatus.CANCELLED }
            .sortedByDescending { it.bookingDate }
}