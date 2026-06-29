package com.safarsakha.data.mapper

import com.safarsakha.data.remote.firebase.BookingDTO
import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.model.BookingStatus
import com.safarsakha.domain.model.PaymentStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

object BookingMapper {

    fun toDomain(dto: BookingDTO): Booking {
        // FIX: Wrap ALL date/time parsing in runCatching so that a single malformed
        // Firestore document (e.g. empty startDate="", missing fields, or a legacy
        // format) does not throw an unhandled exception that propagates out of the
        // mapper and ultimately crashes the screen.
        // Sensible defaults are used when parsing fails so the booking is still
        // visible in the UI (with "unknown" dates) rather than silently dropped or
        // crashing the app.
        val startDate = runCatching { LocalDate.parse(dto.startDate) }
            .getOrElse { LocalDate(1970, 1, 1) }

        val endDate = runCatching { LocalDate.parse(dto.endDate) }
            .getOrElse { LocalDate(1970, 1, 1) }

        val bookingDate = runCatching { Instant.parse(dto.bookingDate) }
            .getOrElse { Clock.System.now() }

        return Booking(
            bookingId = dto.bookingId,
            userId = dto.userId,
            userName = dto.userName,
            packageId = dto.packageId,
            packageName = dto.packageName,
            packagePrice = dto.packagePrice,
            startDate = startDate,
            endDate = endDate,
            bookingDate = bookingDate,
            bookingStatus = runCatching { BookingStatus.valueOf(dto.bookingStatus) }
                .getOrDefault(BookingStatus.UPCOMING),
            paymentStatus = runCatching { PaymentStatus.valueOf(dto.paymentStatus) }
                .getOrDefault(PaymentStatus.FAILED),
            totalAmount = dto.totalAmount,
            cancellationDate = dto.cancellationDate?.let {
                runCatching { Instant.parse(it) }.getOrNull()
            }
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