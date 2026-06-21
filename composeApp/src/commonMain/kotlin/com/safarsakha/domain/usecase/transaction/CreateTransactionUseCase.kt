package com.safarsakha.domain.usecase.transaction

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.Transaction
import com.safarsakha.domain.repository.TransactionRepository

class CreateTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Resource<Transaction> {
        if (transaction.userId.isBlank()) {
            return Resource.Error("You must be logged in to record a transaction")
        }
        if (transaction.amount <= 0) {
            return Resource.Error("Invalid transaction amount")
        }

        return try {
            repository.createTransaction(transaction)
        } catch (e: Exception) {
            Resource.Error("Failed to record transaction: ${e.message}")
        }
    }
}