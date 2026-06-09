package com.safarsakha.data.remote.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
@Serializable
data class TourPackageDTO(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val duration: String = "",
    val price: Double = 0.0,
    val imageUrl: String? = null,
    val includedServices: List<String> = emptyList(),
    val createdAt: String = Clock.System.now().toString(),
    val updatedAt: String = Clock.System.now().toString(),
    val isActive: Boolean = true
)
class FirebaseTourPackageDataSource {
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage
    private val collectionName = "tour_packages"
    private val storagePath = "tour_packages_images"

    suspend fun getAllTourPackages(): List<TourPackageDTO> {
        return try {
            val snapshot = firestore.collection(collectionName).get()
            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data<TourPackageDTO>().copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch packages: ${e.message}")
        }
    }

    fun observeTourPackages(): Flow<List<TourPackageDTO>> {
        return firestore.collection(collectionName)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data<TourPackageDTO>().copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
    }

    suspend fun getTourPackageById(id: String): TourPackageDTO? {
        return try {
            val document = firestore.collection(collectionName).document(id).get()
            if (document.exists) {
                document.data<TourPackageDTO>().copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch package: ${e.message}")
        }
    }

    suspend fun createTourPackage(tourPackageDTO: TourPackageDTO): String {
        return try {
            val docRef = firestore.collection(collectionName).add(tourPackageDTO)
            docRef.id
        } catch (e: Exception) {
            throw Exception("Failed to create package: ${e.message}")
        }
    }

    suspend fun updateTourPackage(id: String, tourPackageDTO: TourPackageDTO) {
        try {
            firestore.collection(collectionName).document(id).set(tourPackageDTO)
        } catch (e: Exception) {
            throw Exception("Failed to update package: ${e.message}")
        }
    }

    suspend fun deleteTourPackage(id: String) {
        try {
            firestore.collection(collectionName).document(id).delete()
        } catch (e: Exception) {
            throw Exception("Failed to delete package: ${e.message}")
        }
    }

    suspend fun uploadImage(imageBytes: ByteArray, fileName: String): String {
        return platformSpecificUploadImage(storage, storagePath, imageBytes, fileName)
    }
}

expect suspend fun platformSpecificUploadImage(
    storage: Any,
    storagePath: String,
    imageBytes: ByteArray,
    fileName: String
): String
