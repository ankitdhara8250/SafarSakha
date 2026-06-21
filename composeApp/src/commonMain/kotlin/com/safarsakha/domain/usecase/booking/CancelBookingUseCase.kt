package com.safarsakha.domain.usecase.booking

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.repository.BookingRepository

class CancelBookingUseCase(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(bookingId: String): Resource<Unit> {
        if (bookingId.isBlank()) {
            return Resource.Error("Invalid booking")
        }
        return try {
            repository.cancelBooking(bookingId)
        } catch (e: Exception) {
            Resource.Error("Failed to cancel booking: ${e.message}")
        }
    }
}