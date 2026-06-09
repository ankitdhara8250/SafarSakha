package com.safarsakha.data.remote.firebase

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

actual suspend fun platformSpecificUploadImage(
    storage: Any,
    storagePath: String,
    imageBytes: ByteArray,
    fileName: String
): String {
    return try {
        val firebaseStorage = FirebaseStorage.getInstance()
        val storageRef = firebaseStorage.reference.child("$storagePath/$fileName")
        storageRef.putBytes(imageBytes).await()
        storageRef.downloadUrl.await().toString()
    } catch (e: Exception) {
        throw Exception("Failed to upload image: ${e.message}")
    }
}

