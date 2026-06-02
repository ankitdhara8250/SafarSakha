package com.safarsakha.presentation.navigation

import com.safarsakha.domain.repository.AuthRepository

actual fun provideAuthRepository(): AuthRepository {
    return object : AuthRepository {
        override suspend fun loginAdmin(
            email: String,
            password: String
        ): Result<Unit> {
            return Result.failure(
                Exception("Firebase Auth is not implemented for iOS yet")
            )
        }
    }
}