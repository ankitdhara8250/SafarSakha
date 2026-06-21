package com.safarsakha.data.repository.impl

import com.safarsakha.core.utils.Resource
import com.safarsakha.data.mapper.TransactionMapper
import com.safarsakha.data.remote.firebase.FirebaseTransactionDataSource
import com.safarsakha.domain.model.Transaction
import com.safarsakha.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val dataSource: FirebaseTransactionDataSource
) : TransactionRepository {

    override suspend fun createTransaction(transaction: Transaction): Resource<Transaction> {
        return try {
            val dto = TransactionMapper.toDTO(transaction)
            val id = dataSource.createTransaction(dto)
            Resource.Success(transaction.copy(transactionId = id))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to record transaction")
        }
    }

    override suspend fun getTransactionsByUserId(userId: String): Resource<List<Transaction>> {
        return try {
            val dtos = dataSource.getTransactionsByUserId(userId)
            Resource.Success(dtos.map { TransactionMapper.toDomain(it) })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch your transactions")
        }
    }

    override fun observeTransactionsByUserId(userId: String): Flow<Resource<List<Transaction>>> {
        return dataSource.observeTransactionsByUserId(userId).map { dtos ->
            try {
                Resource.Success(dtos.map { TransactionMapper.toDomain(it) })
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Mapping error occurred")
            }
        }.catch { e ->
            emit(Resource.Error(e.message ?: "Firestore observer error"))
        }
    }

    override suspend fun getTransactionsByBookingId(bookingId: String): Resource<List<Transaction>> {
        return try {
            val dtos = dataSource.getTransactionsByBookingId(bookingId)
            Resource.Success(dtos.map { TransactionMapper.toDomain(it) })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch transactions for this booking")
        }
    }
}