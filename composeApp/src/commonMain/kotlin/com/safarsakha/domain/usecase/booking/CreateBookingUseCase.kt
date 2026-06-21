package com.safarsakha.domain.usecase.booking

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.repository.BookingRepository

class CreateBookingUseCase(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(booking: Booking): Resource<Booking> {
        if (booking.userId.isBlank()) {
            return Resource.Error("You must be logged in to book a tour")
        }
        if (booking.packageId.isBlank()) {
            return Resource.Error("Invalid tour package")
        }
        if (booking.endDate < booking.startDate) {
            return Resource.Error("End date cannot be before start date")
        }
        if (booking.totalAmount <= 0) {
            return Resource.Error("Invalid booking amount")
        }

        return try {
            repository.createBooking(booking)
        } catch (e: Exception) {
            Resource.Error("Failed to create booking: ${e.message}")
        }
    }
}