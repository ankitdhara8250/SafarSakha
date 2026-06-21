package com.safarsakha.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Domain model representing a payment Transaction linked to a Booking.
 *
 * Firestore collection: "transactions"
 */
data class Transaction(
    val transactionId: String = "",
    val bookingId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "Card",
    val paymentStatus: PaymentStatus = PaymentStatus.FAILED,
    val transactionDate: Instant = Clock.System.now()
)