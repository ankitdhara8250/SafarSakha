package com.safarsakha.data.mapper

import com.safarsakha.data.remote.firebase.TransactionDTO
import com.safarsakha.domain.model.PaymentStatus
import com.safarsakha.domain.model.Transaction
import kotlinx.datetime.Instant

object TransactionMapper {

    fun toDomain(dto: TransactionDTO): Transaction {
        return Transaction(
            transactionId = dto.transactionId,
            bookingId = dto.bookingId,
            userId = dto.userId,
            amount = dto.amount,
            paymentMethod = dto.paymentMethod,
            paymentStatus = runCatching { PaymentStatus.valueOf(dto.paymentStatus) }
                .getOrDefault(PaymentStatus.FAILED),
            transactionDate = Instant.parse(dto.transactionDate)
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