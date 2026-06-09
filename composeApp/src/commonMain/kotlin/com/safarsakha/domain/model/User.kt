package com.safarsakha.domain.model

import kotlinx.datetime.Instant

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val role: UserRole,
    val createdAt: Instant,
    val updatedAt: Instant? = null
)

enum class UserRole {
    USER, ADMIN
}

