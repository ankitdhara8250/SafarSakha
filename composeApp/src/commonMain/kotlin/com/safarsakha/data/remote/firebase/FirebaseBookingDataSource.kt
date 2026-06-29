package com.safarsakha.data.remote.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class BookingDTO(
    val bookingId: String = "",
    val userId: String = "",
    val userName: String = "",
    val packageId: String = "",
    val packageName: String = "",
    val packagePrice: Double = 0.0,
    val startDate: String = "",
    val endDate: String = "",
    val bookingDate: String = Clock.System.now().toString(),
    val bookingStatus: String = "UPCOMING",
    val paymentStatus: String = "FAILED",
    val totalAmount: Double = 0.0,
    val cancellationDate: String? = null
)

class FirebaseBookingDataSource {

    private val firestore = Firebase.firestore
    private val collectionName = "bookings"

    suspend fun createBooking(bookingDTO: BookingDTO): String {
        return try {
            // FIX: Use a single atomic write instead of add() + set().
            // The original two-step approach (add → then set with id) created a race condition:
            // the live snapshot listener could fire between the two writes and emit a document
            // with bookingId = "". That caused duplicate-key crashes in LazyColumn when multiple
            // such transient documents appeared simultaneously.
            // Solution: generate the ID from a new document reference first, then write once.
            val docRef = firestore.collection(collectionName).document
            val id = docRef.id
            docRef.set(bookingDTO.copy(bookingId = id))
            id
        } catch (e: Exception) {
            throw Exception("Failed to create booking: ${e.message}")
        }
    }

    suspend fun getBookingsByUserId(userId: String): List<BookingDTO> {
        return try {
            val snapshot = firestore.collection(collectionName)
                .where { "userId" equalTo userId }
                .get()
            snapshot.documents.mapNotNull { doc ->
                try { doc.data<BookingDTO>() } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch bookings: ${e.message}")
        }
    }

    fun observeBookingsByUserId(userId: String): Flow<List<BookingDTO>> {
        return firestore.collection(collectionName)
            .where { "userId" equalTo userId }
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try { doc.data<BookingDTO>() } catch (e: Exception) { null }
                }
            }
    }

    suspend fun getAllBookings(): List<BookingDTO> {
        return try {
            val snapshot = firestore.collection(collectionName).get()
            snapshot.documents.mapNotNull { doc ->
                try { doc.data<BookingDTO>() } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch bookings: ${e.message}")
        }
    }

    fun observeAllBookings(): Flow<List<BookingDTO>> {
        return firestore.collection(collectionName)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try { doc.data<BookingDTO>() } catch (e: Exception) { null }
                }
            }
    }

    suspend fun getBookingById(bookingId: String): BookingDTO? {
        return try {
            val document = firestore.collection(collectionName).document(bookingId).get()
            if (document.exists) document.data<BookingDTO>() else null
        } catch (e: Exception) {
            throw Exception("Failed to fetch booking: ${e.message}")
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String, cancellationDate: String) {
        try {
            firestore.collection(collectionName).document(bookingId).update(
                mapOf(
                    "bookingStatus" to status,
                    "cancellationDate" to cancellationDate
                )
            )
        } catch (e: Exception) {
            throw Exception("Failed to update booking: ${e.message}")
        }
    }
}