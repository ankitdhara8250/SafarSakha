package com.safarsakha.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Domain model representing a Tour Booking made by a user.
 *
 * Firestore collection: "bookings"
 */
data class Booking(
    val bookingId: String = "",
    val userId: String = "",
    val userName: String = "",
    val packageId: String = "",
    val packageName: String = "",
    val packagePrice: Double = 0.0,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val bookingDate: Instant = Clock.System.now(),
    val bookingStatus: BookingStatus = BookingStatus.UPCOMING,
    val paymentStatus: PaymentStatus = PaymentStatus.FAILED,
    val totalAmount: Double = 0.0,
    val cancellationDate: Instant? = null
)

enum class BookingStatus {
    UPCOMING, COMPLETED, CANCELLED
}

enum class PaymentStatus {
    SUCCESS, FAILED
}