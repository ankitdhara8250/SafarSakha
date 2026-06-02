package com.safarsakha.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.safarsakha.domain.repository.AuthRepository
//import com.safarsakha.presentation.navigation.provideAuthRepository
import kotlinx.coroutines.tasks.await
class FirebaseAuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {
    override suspend fun loginAdmin(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

//actual fun provideAuthRepository(): AuthRepository {
//    return FirebaseAuthRepositoryImpl()
//}