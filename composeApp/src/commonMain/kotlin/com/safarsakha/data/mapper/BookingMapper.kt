package com.safarsakha.data.mapper

import com.safarsakha.data.remote.firebase.BookingDTO
import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.model.BookingStatus
import com.safarsakha.domain.model.PaymentStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

object BookingMapper {

    fun toDomain(dto: BookingDTO): Booking {
        return Booking(
            bookingId = dto.bookingId,
            userId = dto.userId,
            userName = dto.userName,
            packageId = dto.packageId,
            packageName = dto.packageName,
            packagePrice = dto.packagePrice,
            startDate = LocalDate.parse(dto.startDate),
            endDate = LocalDate.parse(dto.endDate),
            bookingDate = Instant.parse(dto.bookingDate),
            bookingStatus = runCatching { BookingStatus.valueOf(dto.bookingStatus) }
                .getOrDefault(BookingStatus.UPCOMING),
            paymentStatus = runCatching { PaymentStatus.valueOf(dto.paymentStatus) }
                .getOrDefault(PaymentStatus.FAILED),
            totalAmount = dto.totalAmount,
            cancellationDate = dto.cancellationDate?.let { runCatching { Instant.parse(it) }.getOrNull() }
        )
    }

    fun toDTO(domain: Booking): BookingDTO {
        return BookingDTO(
            bookingId = domain.bookingId,
            userId = domain.userId,
            userName = domain.userName,
            packageId = domain.packageId,
            packageName = domain.packageName,
            packagePrice = domain.packagePrice,
            startDate = domain.startDate.toString(),
            endDate = domain.endDate.toString(),
            bookingDate = domain.bookingDate.toString(),
            bookingStatus = domain.bookingStatus.name,
            paymentStatus = domain.paymentStatus.name,
            totalAmount = domain.totalAmount,
            cancellationDate = domain.cancellationDate?.toString()
        )
    }
}