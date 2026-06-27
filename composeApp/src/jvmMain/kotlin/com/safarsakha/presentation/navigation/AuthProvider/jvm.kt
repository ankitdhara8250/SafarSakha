package com.safarsakha.presentation.navigation

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.User
import com.safarsakha.domain.repository.AuthRepository

actual fun provideAuthRepository(): AuthRepository {
    return object : AuthRepository {
        override suspend fun loginUser(email: String, password: String): Resource<User> {
            return Resource.Error("Firebase Auth is not available on JVM")
        }

        override suspend fun registerUser(
            name: String,
            email: String,
            phoneNumber: String,
            password: String
        ): Resource<User> {
            return Resource.Error("Firebase Auth is not available on JVM")
        }

        override suspend fun loginAdmin(
            email: String,
            password: String
        ): Result<Unit> {
            return Result.failure(
                Exception("Firebase Auth is not available on JVM")
            )
        }

        override suspend fun logout(): Resource<Unit> {
            return Resource.Error("Firebase Auth is not available on JVM")
        }

        override fun getCurrentUser(): User? {
            return null
        }

        override suspend fun isUserLoggedIn(): Boolean {
            return false
        }

        override suspend fun getUserProfile(uid: String): Resource<User> {
            return Resource.Error("Firebase Auth is not available on JVM")
        }
    }
}
