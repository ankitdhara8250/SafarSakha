package com.safarsakha.data.remote.firebase

actual suspend fun platformSpecificUploadImage(
    storage: Any,
    storagePath: String,
    imageBytes: ByteArray,
    fileName: String
): String {
    // For JVM/Desktop, Firebase Storage is not fully implemented
    // This is a placeholder that throws an error
    throw NotImplementedError("Firebase Storage upload is not implemented for JVM/Desktop target")
}

