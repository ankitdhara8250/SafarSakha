package com.safarsakha.data.mapper

import com.safarsakha.data.remote.firebase.TransactionDTO
import com.safarsakha.domain.model.PaymentStatus
import com.safarsakha.domain.model.Transaction
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object TransactionMapper {

    fun toDomain(dto: TransactionDTO): Transaction {
        // FIX: Instant.parse(dto.transactionDate) was not wrapped in runCatching.
        // If a Firestore document is missing the transactionDate field, TransactionDTO
        // fills it with "" (the String default). Instant.parse("") throws
        // DateTimeFormatException, which propagates out of the mapper. Although the
        // repository catches it and emits Resource.Error, on some coroutine/flow
        // execution paths in KMP the exception can escape the catch boundary and
        // crash the app. Wrapping in runCatching gives a safe fallback (current time)
        // and keeps the behaviour consistent with how bookingDate is handled in
        // the fixed BookingMapper.
        val transactionDate = runCatching { Instant.parse(dto.transactionDate) }
            .getOrElse { Clock.System.now() }

        return Transaction(
            transactionId = dto.transactionId,
            bookingId = dto.bookingId,
            userId = dto.userId,
            amount = dto.amount,
            paymentMethod = dto.paymentMethod,
            paymentStatus = runCatching { PaymentStatus.valueOf(dto.paymentStatus) }
                .getOrDefault(PaymentStatus.FAILED),
            transactionDate = transactionDate
        )
    }

    fun toDTO(domain: Transaction): TransactionDTO {
        return TransactionDTO(
            transactionId = domain.transactionId,
            bookingId = domain.bookingId,
            userId = domain.userId,
            amount = domain.amount,
            paymentMethod = domain.paymentMethod,
            paymentStatus = domain.paymentStatus.name,
            transactionDate = domain.transactionDate.toString()
        )
    }
}