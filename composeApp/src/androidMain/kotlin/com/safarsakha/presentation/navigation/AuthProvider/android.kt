package com.safarsakha.presentation.navigation

import com.safarsakha.data.repository.FirebaseAuthRepositoryImpl
import com.safarsakha.domain.repository.AuthRepository

actual fun provideAuthRepository(): AuthRepository {
    return FirebaseAuthRepositoryImpl()
}