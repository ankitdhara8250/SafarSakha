package com.safarsakha.domain.usecase.auth

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Use case responsible for logging the current user out.
 *
 * Delegates to [AuthRepository.logout], which calls Firebase Auth's sign-out
 * method and clears any cached session data held by the data layer.
 *
 * The repository already exists and already has a [AuthRepository.logout]
 * implementation — this use case simply wraps it in the domain layer so
 * the ViewModel never depends directly on the repository.
 */
class LogoutUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                authRepository.logout()
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Logout failed")
            }
        }
    }
}