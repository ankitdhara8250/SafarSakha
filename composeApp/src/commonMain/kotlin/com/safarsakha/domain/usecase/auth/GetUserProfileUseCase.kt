package com.safarsakha.domain.usecase.auth

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.User
import com.safarsakha.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Fetches the full profile (name, email, phone number, etc.) for the
 * currently logged-in user from the existing user data source
 * (Firebase Authentication uid + Firestore "users" collection).
 *
 * Reuses [AuthRepository.getCurrentUser] to resolve the logged-in uid and
 * [AuthRepository.getUserProfile] to load the complete profile that was
 * saved during registration.
 */
class GetUserProfileUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = authRepository.getCurrentUser()
                    ?: return@withContext Resource.Error("No logged-in user found")

                authRepository.getUserProfile(currentUser.uid)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to load profile")
            }
        }
    }
}