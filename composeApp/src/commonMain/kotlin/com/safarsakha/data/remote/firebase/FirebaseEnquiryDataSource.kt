package com.safarsakha.data.remote.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class EnquiryDTO(
    val enquiryId: String = "",
    val tourPackageId: String = "",
    val tourPackageName: String = "",
    val userId: String = "",
    val userName: String = "",
    val enquiryMessage: String = "",
    val adminReply: String? = null,
    val enquiryStatus: String = "PENDING",
    val createdAt: String = Clock.System.now().toString(),
    val repliedAt: String? = null
)

class FirebaseEnquiryDataSource {

    private val firestore = Firebase.firestore
    private val collection = "enquiries"

    suspend fun submitEnquiry(dto: EnquiryDTO): String {
        return try {
            val docRef = firestore.collection(collection).add(dto)
            val id = docRef.id
            firestore.collection(collection).document(id).set(dto.copy(enquiryId = id))
            id
        } catch (e: Exception) {
            throw Exception("Failed to submit enquiry: ${e.message}")
        }
    }

    suspend fun getAllEnquiries(): List<EnquiryDTO> {
        return try {
            val snapshot = firestore.collection(collection).get()
            snapshot.documents.mapNotNull { doc ->
                try { doc.data<EnquiryDTO>() } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch enquiries: ${e.message}")
        }
    }

    fun observeAllEnquiries(): Flow<List<EnquiryDTO>> {
        return firestore.collection(collection)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try { doc.data<EnquiryDTO>() } catch (e: Exception) { null }
                }
            }
    }

    suspend fun getEnquiriesByUserId(userId: String): List<EnquiryDTO> {
        return try {
            val snapshot = firestore.collection(collection)
                .where { "userId" equalTo userId }
                .get()
            snapshot.documents.mapNotNull { doc ->
                try { doc.data<EnquiryDTO>() } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch user enquiries: ${e.message}")
        }
    }

    fun observeEnquiriesByUserId(userId: String): Flow<List<EnquiryDTO>> {
        return firestore.collection(collection)
            .where { "userId" equalTo userId }
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try { doc.data<EnquiryDTO>() } catch (e: Exception) { null }
                }
            }
    }

    suspend fun replyToEnquiry(enquiryId: String, adminReply: String) {
        try {
            firestore.collection(collection).document(enquiryId).update(
                mapOf(
                    "adminReply" to adminReply,
                    "enquiryStatus" to "REPLIED",
                    "repliedAt" to Clock.System.now().toString()
                )
            )
        } catch (e: Exception) {
            throw Exception("Failed to send reply: ${e.message}")
        }
    }
}