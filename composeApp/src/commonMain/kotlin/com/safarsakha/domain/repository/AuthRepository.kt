package com.safarsakha.domain.repository

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.User

interface AuthRepository {
    suspend fun loginUser(email: String, password: String): Resource<User>
    suspend fun registerUser(
        name: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Resource<User>
    suspend fun loginAdmin(
        email: String,
        password: String
    ): Result<Unit>
    suspend fun logout(): Resource<Unit>
    fun getCurrentUser(): User?
    suspend fun isUserLoggedIn(): Boolean
}