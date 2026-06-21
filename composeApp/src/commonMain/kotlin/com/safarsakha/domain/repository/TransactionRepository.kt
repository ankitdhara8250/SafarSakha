package com.safarsakha.domain.repository

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun createTransaction(transaction: Transaction): Resource<Transaction>
    suspend fun getTransactionsByUserId(userId: String): Resource<List<Transaction>>
    fun observeTransactionsByUserId(userId: String): Flow<Resource<List<Transaction>>>
    suspend fun getTransactionsByBookingId(bookingId: String): Resource<List<Transaction>>
}