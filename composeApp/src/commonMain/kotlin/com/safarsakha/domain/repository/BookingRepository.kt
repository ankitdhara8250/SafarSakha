package com.safarsakha.domain.repository

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.Booking
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    suspend fun createBooking(booking: Booking): Resource<Booking>
    suspend fun getBookingsByUserId(userId: String): Resource<List<Booking>>
    fun observeBookingsByUserId(userId: String): Flow<Resource<List<Booking>>>
    suspend fun getAllBookings(): Resource<List<Booking>>
    fun observeAllBookings(): Flow<Resource<List<Booking>>>
    suspend fun getBookingById(bookingId: String): Resource<Booking>
    suspend fun cancelBooking(bookingId: String): Resource<Unit>
}