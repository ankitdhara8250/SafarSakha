package com.safarsakha.domain.usecase.transaction

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.Transaction
import com.safarsakha.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetUserTransactionsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(userId: String): Resource<List<Transaction>> {
        return repository.getTransactionsByUserId(userId)
    }

    fun observe(userId: String): Flow<Resource<List<Transaction>>> {
        return repository.observeTransactionsByUserId(userId)
    }
}