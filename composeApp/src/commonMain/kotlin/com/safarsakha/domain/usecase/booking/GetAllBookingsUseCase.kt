package com.safarsakha.domain.usecase.booking

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Used by Admin Booking Management to load every booking made by every user.
 */
class GetAllBookingsUseCase(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(): Resource<List<Booking>> {
        return repository.getAllBookings()
    }

    fun observe(): Flow<Resource<List<Booking>>> {
        return repository.observeAllBookings()
    }
}