package com.safarsakha.data.repository

import com.safarsakha.core.utils.Resource
import com.safarsakha.data.remote.firebase.auth.FirebaseAuthDataSource
import com.safarsakha.domain.model.User
import com.safarsakha.domain.model.UserRole
import com.safarsakha.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class FirebaseAuthRepositoryImpl(
    private val authDataSource: FirebaseAuthDataSource = FirebaseAuthDataSource()
) : AuthRepository {

    override suspend fun loginUser(email: String, password: String): Resource<User> =
        withContext(Dispatchers.IO) {
            try {
                val result = authDataSource.loginUser(email, password)
                if (result.isSuccess) {
                    val firebaseUser = result.getOrThrow()
                    val userResult = authDataSource.getUserFromFirestore(firebaseUser.uid)

                    if (userResult.isSuccess) {
                        Resource.Success(userResult.getOrThrow())
                    } else {
                        Resource.Error("Failed to fetch user data")
                    }
                } else {
                    Resource.Error(result.exceptionOrNull()?.message ?: "Login failed")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred during login")
            }
        }

    override suspend fun registerUser(
        name: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val result = authDataSource.registerUser(email, password, name, phoneNumber)
            if (result.isSuccess) {
                val firebaseUser = result.getOrThrow()
                val userResult = authDataSource.getUserFromFirestore(firebaseUser.uid)

                if (userResult.isSuccess) {
                    Resource.Success(userResult.getOrThrow())
                } else {
                    Resource.Error("Failed to fetch user data after registration")
                }
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred during registration")
        }
    }

    override suspend fun logout(): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = authDataSource.logout()
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Logout failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred during logout")
        }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = authDataSource.getCurrentFirebaseUser() ?: return null
        return User(
            uid = firebaseUser.uid,
            name = "",
            email = firebaseUser.email ?: "",
            phoneNumber = "",
            role = UserRole.USER,
            createdAt = Clock.System.now(),
            updatedAt = null
        )
    }

    override suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        authDataSource.getCurrentFirebaseUser() != null
    }

    override suspend fun getUserProfile(uid: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val userResult = authDataSource.getUserFromFirestore(uid)
            if (userResult.isSuccess) {
                Resource.Success(userResult.getOrThrow())
            } else {
                Resource.Error(userResult.exceptionOrNull()?.message ?: "Failed to fetch user profile")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred while fetching the profile")
        }
    }

    override suspend fun loginAdmin(email: String, password: String): Result<Unit> {
        return try {
            val result = authDataSource.loginUser(email, password)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}