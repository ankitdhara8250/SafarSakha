package com.safarsakha

import com.safarsakha.data.remote.firebase.auth.FirebaseAuthDataSource
import com.safarsakha.domain.repository.AuthRepository

actual fun provideAuthRepository(): AuthRepository {
    val authDataSource = FirebaseAuthDataSource()
    return AuthRepositoryImpl(authDataSource)
}

