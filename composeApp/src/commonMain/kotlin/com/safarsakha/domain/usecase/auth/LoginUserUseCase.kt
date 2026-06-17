package com.safarsakha.domain.usecase.auth

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.User
import com.safarsakha.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class LoginUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                authRepository.loginUser(email, password)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Login failed")
            }
        }
    }
}

