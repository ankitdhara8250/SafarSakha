package com.safarsakha.data.remote.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.storage
import platform.Foundation.NSData
import platform.Foundation.NSMutableData

actual suspend fun platformSpecificUploadImage(
    storage: Any,
    storagePath: String,
    imageBytes: ByteArray,
    fileName: String
): String {
    return try {
        // Convert ByteArray to NSData for iOS
        val nsData = NSMutableData(capacity = imageBytes.size.toULong())
        for (byte in imageBytes) {
            // NSData requires UByte
            nsData?.appendBytes(byteArrayOf(byte), length = 1UL)
        }
        
        val storageRef = Firebase.storage.reference.child("$storagePath/$fileName")
        storageRef.putData(nsData as NSData)
        storageRef.getDownloadUrl()
    } catch (e: Exception) {
        throw Exception("Failed to upload image: ${e.message}")
    }
}

