package com.safarsakha.data.repository.impl

import com.safarsakha.core.utils.Resource
import com.safarsakha.data.mapper.BookingMapper
import com.safarsakha.data.remote.firebase.FirebaseBookingDataSource
import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class BookingRepositoryImpl(
    private val dataSource: FirebaseBookingDataSource
) : BookingRepository {

    override suspend fun createBooking(booking: Booking): Resource<Booking> {
        return try {
            val dto = BookingMapper.toDTO(booking)
            val id = dataSource.createBooking(dto)
            Resource.Success(booking.copy(bookingId = id))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create booking")
        }
    }

    override suspend fun getBookingsByUserId(userId: String): Resource<List<Booking>> {
        return try {
            val dtos = dataSource.getBookingsByUserId(userId)
            Resource.Success(dtos.map { BookingMapper.toDomain(it) })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch your bookings")
        }
    }

    override fun observeBookingsByUserId(userId: String): Flow<Resource<List<Booking>>> {
        return dataSource.observeBookingsByUserId(userId).map { dtos ->
            try {
                Resource.Success(dtos.map { BookingMapper.toDomain(it) })
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Mapping error occurred")
            }
        }.catch { e ->
            emit(Resource.Error(e.message ?: "Firestore observer error"))
        }
    }

    override suspend fun getAllBookings(): Resource<List<Booking>> {
        return try {
            val dtos = dataSource.getAllBookings()
            Resource.Success(dtos.map { BookingMapper.toDomain(it) })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch bookings")
        }
    }

    override fun observeAllBookings(): Flow<Resource<List<Booking>>> {
        return dataSource.observeAllBookings().map { dtos ->
            try {
                Resource.Success(dtos.map { BookingMapper.toDomain(it) })
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Mapping error occurred")
            }
        }.catch { e ->
            emit(Resource.Error(e.message ?: "Firestore observer error"))
        }
    }

    override suspend fun getBookingById(bookingId: String): Resource<Booking> {
        return try {
            val dto = dataSource.getBookingById(bookingId)
            if (dto != null) {
                Resource.Success(BookingMapper.toDomain(dto))
            } else {
                Resource.Error("Booking not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch booking")
        }
    }

    override suspend fun cancelBooking(bookingId: String): Resource<Unit> {
        return try {
            dataSource.updateBookingStatus(
                bookingId = bookingId,
                status = "CANCELLED",
                cancellationDate = Clock.System.now().toString()
            )
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to cancel booking")
        }
    }
}