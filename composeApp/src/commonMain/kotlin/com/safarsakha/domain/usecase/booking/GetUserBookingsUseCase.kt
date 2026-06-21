package com.safarsakha.domain.usecase.booking

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow

class GetUserBookingsUseCase(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(userId: String): Resource<List<Booking>> {
        return repository.getBookingsByUserId(userId)
    }

    fun observe(userId: String): Flow<Resource<List<Booking>>> {
        return repository.observeBookingsByUserId(userId)
    }
}