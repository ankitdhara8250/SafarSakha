package com.safarsakha.data.remote.firebase.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.firestore
import com.safarsakha.domain.model.User
import com.safarsakha.domain.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "USER",
    val createdAt: Long = 0,
    val updatedAt: Long? = null
)

class FirebaseAuthDataSource {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val result = auth.signInWithEmailAndPassword(email, password)
                result.user?.let { user ->
                    Result.success(user)
                } ?: Result.failure(Exception("Login failed"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        phoneNumber: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password)
            val firebaseUser = result.user ?: throw Exception("User creation failed")

            val userDto = UserDTO(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                phoneNumber = phoneNumber,
                role = "USER",
                createdAt = Clock.System.now().toEpochMilliseconds(),
                updatedAt = null
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(UserDTO.serializer(), userDto)

            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentFirebaseUser(): FirebaseUser? = auth.currentUser

    suspend fun getUserFromFirestore(uid: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val document = firestore.collection("users").document(uid).get()
                if (!document.exists) {
                    return@withContext Result.failure(Exception("User not found"))
                }
                val userDto = document.data(UserDTO.serializer())

                val user = User(
                    uid = userDto.uid,
                    name = userDto.name,
                    email = userDto.email,
                    phoneNumber = userDto.phoneNumber,
                    role = if (userDto.role == "ADMIN") UserRole.ADMIN else UserRole.USER,
                    createdAt = Instant.fromEpochMilliseconds(userDto.createdAt),
                    updatedAt = userDto.updatedAt?.let {
                        Instant.fromEpochMilliseconds(it)
                    }
                )
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
