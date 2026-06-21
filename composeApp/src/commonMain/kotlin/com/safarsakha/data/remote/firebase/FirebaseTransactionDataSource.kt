package com.safarsakha.data.remote.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDTO(
    val transactionId: String = "",
    val bookingId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "Card",
    val paymentStatus: String = "FAILED",
    val transactionDate: String = Clock.System.now().toString()
)

/**
 * Service layer for the "transactions" Firestore collection.
 * UI -> ViewModel -> Repository -> Service (this class) -> Firebase
 */
class FirebaseTransactionDataSource {

    private val firestore = Firebase.firestore
    private val collectionName = "transactions"

    suspend fun createTransaction(transactionDTO: TransactionDTO): String {
        return try {
            val docRef = firestore.collection(collectionName).add(transactionDTO)
            val id = docRef.id
            // Persist the generated id back inside the document (mirrors EnquiryDataSource pattern).
            firestore.collection(collectionName).document(id).set(transactionDTO.copy(transactionId = id))
            id
        } catch (e: Exception) {
            throw Exception("Failed to create transaction: ${e.message}")
        }
    }

    suspend fun getTransactionsByUserId(userId: String): List<TransactionDTO> {
        return try {
            val snapshot = firestore.collection(collectionName)
                .where { "userId" equalTo userId }
                .get()
            snapshot.documents.mapNotNull { doc ->
                try { doc.data<TransactionDTO>() } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch transactions: ${e.message}")
        }
    }

    fun observeTransactionsByUserId(userId: String): Flow<List<TransactionDTO>> {
        return firestore.collection(collectionName)
            .where { "userId" equalTo userId }
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try { doc.data<TransactionDTO>() } catch (e: Exception) { null }
                }
            }
    }

    suspend fun getTransactionsByBookingId(bookingId: String): List<TransactionDTO> {
        return try {
            val snapshot = firestore.collection(collectionName)
                .where { "bookingId" equalTo bookingId }
                .get()
            snapshot.documents.mapNotNull { doc ->
                try { doc.data<TransactionDTO>() } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch transactions for booking: ${e.message}")
        }
    }
}